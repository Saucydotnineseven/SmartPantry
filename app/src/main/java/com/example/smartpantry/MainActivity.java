package com.example.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.activity.EdgeToEdge;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

// Pantry List screen: shows every ingredient stored in the database.
public class MainActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private PantryAdapter adapter;
    private TextView txtEmpty;
    private BottomNavigationView bottomNav;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = new DatabaseHelper(this);
        txtEmpty = findViewById(R.id.txtEmpty);

        // Set up the RecyclerView: a vertical list with divider lines
        RecyclerView recycler = findViewById(R.id.recyclerPantry);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        adapter = new PantryAdapter(new ArrayList<>(), new PantryAdapter.Listener() {
            @Override
            public void onItemClick(PantryItem item) {
                // Open the same screen in EDIT mode: pass the item's id in the Intent
                Intent intent = new Intent(MainActivity.this, AddEditActivity.class);
                intent.putExtra(AddEditActivity.EXTRA_ITEM_ID, item.getId());
                startActivity(intent);
            }
            @Override
            public void onItemLongClick(PantryItem item) {
                confirmDelete(item);
            }
        });
        recycler.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(view ->
                startActivity(new Intent(MainActivity.this, AddEditActivity.class)));
        // Open the Suggested Recipes screen
        findViewById(R.id.btnSuggest).setOnClickListener(view ->
                startActivity(new Intent(MainActivity.this, SuggestedRecipesActivity.class)));         // Bottom navigation: Pantry (this screen), Recipes, Settings
        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id == R.id.nav_recipes) {
                startActivity(new Intent(MainActivity.this, SuggestedRecipesActivity.class));
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
            return true;
        });}

    // onResume runs every time this screen becomes visible again,
    // so the list refreshes after adding/editing on another screen.
    @Override
    protected void onResume() {
        super.onResume();
        loadPantry();
        bottomNav.setSelectedItemId(R.id.nav_pantry); // highlight "Pantry" when we return
        showExpiryAlert();
    }

    // If the setting is ON, warn about items that are expired or expire within 3 days
    private void showExpiryAlert() {
        SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS, MODE_PRIVATE);
        if (!prefs.getBoolean(SettingsActivity.KEY_ALERTS, false)) {
            return; // alerts are switched off
        }
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        long limit = System.currentTimeMillis() + 3L * 24 * 60 * 60 * 1000; // now + 3 days
        int count = 0;
        for (PantryItem item : db.getAllPantryItems()) {
            String expiry = item.getExpiryDate();
            if (expiry == null || expiry.isEmpty()) continue; // no date = skip
            try {
                Date date = fmt.parse(expiry);
                if (date != null && date.getTime() <= limit) count++;
            } catch (ParseException e) {
                // ignore unreadable dates
            }
        }
        if (count > 0) {
            Toast.makeText(this, count + " item(s) expired or expiring soon",
                    Toast.LENGTH_LONG).show();
        }
    }

    // Read from the database, give the data to the adapter, show/hide the empty message
    private void loadPantry() {
        List<PantryItem> items = db.getAllPantryItems();
        adapter.setItems(items);
        txtEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // DELETE: ask first, then remove from the database and refresh
    private void confirmDelete(PantryItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Delete ingredient?")
                .setMessage("Remove " + item.getName() + " from your pantry?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.deletePantryItem(item.getId());
                    loadPantry();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}