package com.example.smartpantry;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// Shows one recipe in full. The recipe id arrives inside the Intent.
public class RecipeDetailActivity extends AppCompatActivity {

    // Key used to pass the recipe id between screens
    public static final String EXTRA_RECIPE_ID = "recipe_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        // Read the id sent by SuggestedRecipesActivity (-1 = nothing was sent)
        int recipeId = getIntent().getIntExtra(EXTRA_RECIPE_ID, -1);
        Recipe recipe = new DatabaseHelper(this).getRecipe(recipeId);

        // Safety net: if the recipe can't be found, close instead of crashing
        if (recipe == null) {
            Toast.makeText(this, "Recipe not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ((TextView) findViewById(R.id.txtDetailName)).setText(recipe.getName());

        // Build "- 3 pcs egg" style lines, one per ingredient
        StringBuilder ingredientText = new StringBuilder();
        for (RecipeIngredient ing : recipe.getIngredients()) {
            ingredientText.append("\u2022 ")
                    .append(formatQuantity(ing.getQuantity())).append(" ")
                    .append(ing.getUnit()).append(" ")
                    .append(ing.getName()).append("\n");
        }
        ((TextView) findViewById(R.id.txtDetailIngredients)).setText(ingredientText.toString().trim());

        // The steps are stored separated by new lines; number them 1., 2., 3. ...
        String[] steps = recipe.getSteps().split("\n");
        StringBuilder stepText = new StringBuilder();
        for (int i = 0; i < steps.length; i++) {
            stepText.append(i + 1).append(". ").append(steps[i]).append("\n\n");
        }
        ((TextView) findViewById(R.id.txtDetailSteps)).setText(stepText.toString().trim());

        Button btnBack = findViewById(R.id.btnDetailBack);
        btnBack.setOnClickListener(view -> finish());
    }

    // Shows 3 instead of 3.0, but keeps 0.5 as 0.5
    private String formatQuantity(double q) {
        return q == Math.floor(q) ? String.valueOf((long) q) : String.valueOf(q);
    }
}