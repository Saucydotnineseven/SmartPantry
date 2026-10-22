package com.example.smartpantry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// Decides which recipes the user can make RIGHT NOW.
// STRICT RULE: a recipe is suggested only if EVERY ingredient it needs is in
// the pantry in at least the required quantity. One missing ingredient = excluded.
public class RecipeMatcher {

    // Main method: returns only the recipes the user can fully make.
    public static List<Recipe> getSuggestedRecipes(List<Recipe> allRecipes,
                                                   List<PantryItem> pantry) {
        // Step 1: total up what the pantry holds, e.g. "egg|pcs" -> 3.0
        Map<String, Double> available = buildPantryTotals(pantry);

        // Step 2: keep a recipe only if canMake() says yes
        List<Recipe> suggested = new ArrayList<>();
        for (Recipe recipe : allRecipes) {
            if (canMake(recipe, available)) {
                suggested.add(recipe);
            }
        }
        return suggested;
    }

    // Builds a lookup map: key = "normalizedName|baseUnit", value = total amount.
    // If the user has the same item twice (e.g. two "egg" rows), amounts are added.
    private static Map<String, Double> buildPantryTotals(List<PantryItem> pantry) {
        Map<String, Double> totals = new HashMap<>();
        for (PantryItem item : pantry) {
            String key = makeKey(item.getName(), item.getUnit());
            double amount = toBaseAmount(item.getQuantity(), item.getUnit());
            Double current = totals.get(key);
            totals.put(key, (current == null ? 0 : current) + amount);
        }
        return totals;
    }

    // The strict check: EVERY ingredient must be present in enough quantity.
    private static boolean canMake(Recipe recipe, Map<String, Double> available) {
        if (recipe.getIngredients().isEmpty()) {
            return false; // a recipe with no ingredients is never suggested
        }
        for (RecipeIngredient ing : recipe.getIngredients()) {
            String key = makeKey(ing.getName(), ing.getUnit());
            double needed = toBaseAmount(ing.getQuantity(), ing.getUnit());
            Double have = available.get(key);

            // Missing entirely, or not enough -> the WHOLE recipe fails
            if (have == null || have + 0.0001 < needed) {
                return false;
            }
        }
        return true; // every ingredient passed
    }

    // ---------- Handling real-world messiness ----------

    // "Tomatoes " and "tomato" must count as the same ingredient.
    private static String makeKey(String name, String unit) {
        return normalizeName(name) + "|" + baseUnit(unit);
    }

    // Lowercase, trim, and make simple plurals singular.
    static String normalizeName(String raw) {
        String n = raw.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
        if (n.endsWith("ies") && n.length() > 4) {
            return n.substring(0, n.length() - 3) + "y";  // berries -> berry
        }
        if (n.endsWith("oes") && n.length() > 4) {
            return n.substring(0, n.length() - 2);        // tomatoes -> tomato
        }
        if (n.endsWith("s") && !n.endsWith("ss") && !n.endsWith("us") && n.length() > 3) {
            return n.substring(0, n.length() - 1);        // eggs -> egg
        }
        return n;
    }

    // Groups units into three families so they can be compared:
    // weight -> "g", volume -> "ml", counted items -> "pcs".
    static String baseUnit(String unit) {
        switch (unit.trim().toLowerCase(Locale.ROOT)) {
            case "g": case "kg": case "mg":
                return "g";
            case "ml": case "l": case "tsp": case "tbsp": case "cup":
                return "ml";
            case "pcs": case "pc": case "piece": case "pieces": case "whole":
                return "pcs";
            default:
                return unit.trim().toLowerCase(Locale.ROOT); // unknown unit: compare as-is
        }
    }

    // Converts a quantity into its base unit, e.g. 0.05 kg -> 50 g, 2 tbsp -> 30 ml.
    static double toBaseAmount(double qty, String unit) {
        switch (unit.trim().toLowerCase(Locale.ROOT)) {
            case "kg":   return qty * 1000;
            case "mg":   return qty / 1000;
            case "l":    return qty * 1000;
            case "tsp":  return qty * 5;
            case "tbsp": return qty * 15;
            case "cup":  return qty * 240;
            default:     return qty; // g, ml, pcs are already base units
        }
    }
}