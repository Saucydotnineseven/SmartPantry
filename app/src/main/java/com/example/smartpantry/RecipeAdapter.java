package com.example.smartpantry;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

// Same idea as PantryAdapter: connects a List<Recipe> to the RecyclerView rows.
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    // Lets the Activity decide what happens when a recipe is tapped
    public interface Listener {
        void onRecipeClick(Recipe recipe);
    }

    private List<Recipe> recipes;
    private final Listener listener;

    public RecipeAdapter(List<Recipe> recipes, Listener listener) {
        this.recipes = recipes;
        this.listener = listener;
    }

    public void setRecipes(List<Recipe> newRecipes) {
        this.recipes = newRecipes;
        notifyDataSetChanged(); // redraw the list
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.txtName.setText(recipe.getName());

        // Build a short summary like "Needs: egg, butter, salt"
        StringBuilder summary = new StringBuilder("Needs: ");
        List<RecipeIngredient> ingredients = recipe.getIngredients();
        for (int i = 0; i < ingredients.size(); i++) {
            summary.append(ingredients.get(i).getName());
            if (i < ingredients.size() - 1) {
                summary.append(", ");
            }
        }
        holder.txtIngredients.setText(summary.toString());

        holder.itemView.setOnClickListener(view -> listener.onRecipeClick(recipe));
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    // Holds references to the row's views so they are not looked up again on every scroll
    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        TextView txtName, txtIngredients;

        RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtRecipeName);
            txtIngredients = itemView.findViewById(R.id.txtRecipeIngredients);
        }
    }
}