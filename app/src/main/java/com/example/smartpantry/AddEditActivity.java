package com.example.smartpantry;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

// One screen used for BOTH adding and editing an ingredient.
// MainActivity opens it with an Intent. If the Intent carries an item id,
// we are in EDIT mode (form pre-filled). If not, we are in ADD mode.
public class AddEditActivity extends AppCompatActivity {

    // Key used to pass the item's id between screens inside the Intent
    public static final String EXTRA_ITEM_ID = "item_id";

    // Units the user can choose. These all match what RecipeMatcher understands.
    private static final String[] UNITS = {"g", "kg", "ml", "l", "pcs", "tsp", "tbsp", "cup"};

    private DatabaseHelper db;
    private EditText edtName, edtQuantity, edtExpiry;
    private Spinner spinUnit;
    private PantryItem editingItem = null; // stays null in ADD mode

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit);

        db = new DatabaseHelper(this);

        // Connect the Java variables to the views in the XML
        edtName = findViewById(R.id.edtName);
        edtQuantity = findViewById(R.id.edtQuantity);
        edtExpiry = findViewById(R.id.edtExpiry);
        spinUnit = findViewById(R.id.spinUnit);
        TextView txtTitle = findViewById(R.id.txtFormTitle);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnCancel = findViewById(R.id.btnCancel);

        // Fill the unit drop-down
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, UNITS);
        spinUnit.setAdapter(unitAdapter);

        // Read the Intent that started this screen.
        // -1 is the default meaning "no id was sent" = ADD mode.
        int itemId = getIntent().getIntExtra(EXTRA_ITEM_ID, -1);
        if (itemId != -1) {
            editingItem = db.getPantryItem(itemId); // load the row from the database
            if (editingItem != null) {
                txtTitle.setText("Edit Ingredient");
                fillForm(editingItem);
            }
        }

        btnSave.setOnClickListener(view -> saveIngredient());
        btnCancel.setOnClickListener(view -> finish()); // close this screen, go back
    }

    // EDIT mode: show the existing values in the form
    private void fillForm(PantryItem item) {
        edtName.setText(item.getName());

        // Show 3 instead of 3.0
        double q = item.getQuantity();
        edtQuantity.setText(q == Math.floor(q) ? String.valueOf((long) q) : String.valueOf(q));

        edtExpiry.setText(item.getExpiryDate());

        // Select the saved unit in the drop-down
        for (int i = 0; i < UNITS.length; i++) {
            if (UNITS[i].equalsIgnoreCase(item.getUnit())) {
                spinUnit.setSelection(i);
                break;
            }
        }
    }

    // Validate the input, then CREATE (add) or UPDATE (edit) in the database
    private void saveIngredient() {
        String name = edtName.getText().toString().trim();
        String qtyText = edtQuantity.getText().toString().trim().replace(',', '.');
        String expiry = edtExpiry.getText().toString().trim();
        String unit = spinUnit.getSelectedItem().toString();

        // ---------- INPUT VALIDATION ----------
        boolean valid = true;

        // 1) Name: required, and must contain at least one letter
        if (name.isEmpty()) {
            edtName.setError("Ingredient name is required");
            valid = false;
        } else if (!name.matches(".*[A-Za-z].*")) {
            edtName.setError("Name must contain letters");
            valid = false;
        }

        // 2) Quantity: required, must be a number, and must be greater than 0
        double quantity = 0;
        if (qtyText.isEmpty()) {
            edtQuantity.setError("Quantity is required");
            valid = false;
        } else {
            try {
                quantity = Double.parseDouble(qtyText);
                if (quantity <= 0) {
                    edtQuantity.setError("Quantity must be greater than 0");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                edtQuantity.setError("Enter a valid number");
                valid = false;
            }
        }

        // 3) Expiry: optional, but if typed it must be a real date like 2026-12-31
        if (!expiry.isEmpty() && !isValidDate(expiry)) {
            edtExpiry.setError("Use the format yyyy-MM-dd (e.g. 2026-12-31)");
            valid = false;
        }

        if (!valid) {
            return; // stop here: nothing is saved while any field is wrong
        }

        // ---------- SAVE ----------
        if (editingItem == null) {
            // ADD mode -> CREATE a new row
            db.addPantryItem(new PantryItem(name, quantity, unit, expiry));
            Toast.makeText(this, "Ingredient added", Toast.LENGTH_SHORT).show();
        } else {
            // EDIT mode -> UPDATE the existing row (same id, new values)
            editingItem.setName(name);
            editingItem.setQuantity(quantity);
            editingItem.setUnit(unit);
            editingItem.setExpiryDate(expiry);
            db.updatePantryItem(editingItem);
            Toast.makeText(this, "Ingredient updated", Toast.LENGTH_SHORT).show();
        }

        finish(); // go back; MainActivity.onResume() reloads the list
    }

    // Checks the text is a REAL date. Lenient=false rejects things like 2026-13-45.
    private boolean isValidDate(String text) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        format.setLenient(false);
        try {
            format.parse(text);
            return text.length() == 10; // also rejects odd short forms like 2026-1-5
        } catch (ParseException e) {
            return false;
        }
    }
}