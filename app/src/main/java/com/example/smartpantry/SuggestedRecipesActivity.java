package com.example.smartpantry;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

// Shows ONLY the recipes the user can make right now (strict matching).
public class SuggestedRecipesActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private RecipeAdapter adapter;
    private TextView txtNoMatch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested);

        db = new DatabaseHelper(this);
        txtNoMatch = findViewById(R.id.txtNoMatch);

        RecyclerView recycler = findViewById(R.id.recyclerRecipes);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        adapter = new RecipeAdapter(new ArrayList<>(), recipe -> {
            // Open the detail screen and pass the recipe's id in the Intent
            Intent intent = new Intent(SuggestedRecipesActivity.this, RecipeDetailActivity.class);
            intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
            startActivity(intent);
        });
        recycler.setAdapter(adapter);

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(view -> finish()); // close this screen
    }

    // Recalculated every time the screen is shown, so it always matches the current pantry
    @Override
    protected void onResume() {
        super.onResume();
        loadSuggestions();
    }

    private void loadSuggestions() {
        List<PantryItem> pantry = db.getAllPantryItems();
        List<Recipe> allRecipes = db.getAllRecipes();

        // THE CORE LOGIC: only recipes whose EVERY ingredient is in the pantry
        List<Recipe> suggested = RecipeMatcher.getSuggestedRecipes(allRecipes, pantry);

        adapter.setRecipes(suggested);

        // Friendly message instead of a blank screen when nothing matches
        txtNoMatch.setVisibility(suggested.isEmpty() ? View.VISIBLE : View.GONE);
    }
}