package com.example.smartpantry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

// SQLiteOpenHelper manages creating and upgrading the database file.
// Android stores the file on the device, so data persists after the app closes.
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "smart_pantry.db";
    private static final int DB_VERSION = 2; // bump this when the schema changes

    // ---- Table and column names (constants avoid typos) ----
    public static final String TABLE_PANTRY = "pantry";
    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_QTY = "quantity";
    public static final String COL_UNIT = "unit";
    public static final String COL_EXPIRY = "expiry_date";

    public static final String TABLE_RECIPES = "recipes";
    public static final String COL_STEPS = "steps";

    public static final String TABLE_RECIPE_ING = "recipe_ingredients";
    public static final String COL_RECIPE_ID = "recipe_id";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // Called ONCE, the first time the database is created.
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Pantry: what the user currently owns
        db.execSQL("CREATE TABLE " + TABLE_PANTRY + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_NAME + " TEXT NOT NULL, "
                + COL_QTY + " REAL NOT NULL, "
                + COL_UNIT + " TEXT NOT NULL, "
                + COL_EXPIRY + " TEXT)");

        // Recipes: one row per recipe
        db.execSQL("CREATE TABLE " + TABLE_RECIPES + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_NAME + " TEXT NOT NULL, "
                + COL_STEPS + " TEXT NOT NULL)");

        // Ingredients needed by each recipe (one-to-many: one recipe, many ingredients)
        db.execSQL("CREATE TABLE " + TABLE_RECIPE_ING + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_RECIPE_ID + " INTEGER NOT NULL, "
                + COL_NAME + " TEXT NOT NULL, "
                + COL_QTY + " REAL NOT NULL, "
                + COL_UNIT + " TEXT NOT NULL, "
                + "FOREIGN KEY(" + COL_RECIPE_ID + ") REFERENCES "
                + TABLE_RECIPES + "(" + COL_ID + "))");
    }

    // Called when DB_VERSION increases. Simple approach: drop and recreate.
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE_ING);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        onCreate(db);         seedRecipes(db);

    }

    // ===================== CREATE =====================
    public long addPantryItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues(); // column -> value map
        values.put(COL_NAME, item.getName());
        values.put(COL_QTY, item.getQuantity());
        values.put(COL_UNIT, item.getUnit());
        values.put(COL_EXPIRY, item.getExpiryDate());
        return db.insert(TABLE_PANTRY, null, values); // returns new row id (-1 on error)
    }

    // ===================== READ =====================
    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        // Cursor walks through the result rows one at a time
        Cursor cursor = db.query(TABLE_PANTRY, null, null, null, null, null,
                COL_NAME + " ASC"); // sorted alphabetically

        while (cursor.moveToNext()) {
            items.add(new PantryItem(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(COL_QTY)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_UNIT)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_EXPIRY))));
        }
        cursor.close(); // always close cursors to avoid leaks
        return items;
    }

    // Read ONE item (needed to pre-fill the Edit screen)
    public PantryItem getPantryItem(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_PANTRY, null, COL_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        PantryItem item = null;
        if (cursor.moveToFirst()) {
            item = new PantryItem(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)),
                    cursor.getDouble(cursor.getColumnIndexOrThrow(COL_QTY)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_UNIT)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COL_EXPIRY)));
        }
        cursor.close();
        return item;
    }

    // ===================== UPDATE =====================
    public int updatePantryItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, item.getName());
        values.put(COL_QTY, item.getQuantity());
        values.put(COL_UNIT, item.getUnit());
        values.put(COL_EXPIRY, item.getExpiryDate());
        // The "?" is filled by the array: prevents SQL injection
        return db.update(TABLE_PANTRY, values, COL_ID + "=?",
                new String[]{String.valueOf(item.getId())}); // rows affected
    }

    // ===================== DELETE =====================
    public int deletePantryItem(int id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_PANTRY, COL_ID + "=?",
                new String[]{String.valueOf(id)});
    }
    // ===================== SEED RECIPES =====================
    // Runs once when the database is created. Each ingredient is written as
    // "name,quantity,unit". Names are lowercase and singular on purpose; the
    // matcher in Step 3 will also handle plurals like "tomatoes".
    private void seedRecipes(SQLiteDatabase db) {
        addRecipe(db, "Scrambled Eggs",
                "Beat the eggs with salt.\nMelt butter in a pan.\nPour in the eggs and stir gently until set.",
                "egg,3,pcs", "butter,10,g", "salt,2,g");
        addRecipe(db, "Cheese Omelette",
                "Beat the eggs with salt.\nMelt butter in a pan and add the eggs.\nAdd cheese, fold over and cook until golden.",
                "egg,3,pcs", "cheese,40,g", "butter,10,g", "salt,2,g");
        addRecipe(db, "Tomato Pasta",
                "Boil the pasta until soft.\nFry onion and garlic in olive oil.\nAdd chopped tomato and salt and simmer 10 minutes.\nMix in the pasta.",
                "pasta,200,g", "tomato,3,pcs", "onion,1,pcs", "garlic,2,pcs", "olive oil,15,ml", "salt,2,g");
        addRecipe(db, "Egg Fried Rice",
                "Fry the onion in oil.\nAdd the eggs and scramble.\nAdd cooked rice and soy sauce.\nStir fry for 5 minutes.",
                "rice,200,g", "egg,2,pcs", "onion,1,pcs", "soy sauce,15,ml", "oil,15,ml");
        addRecipe(db, "Grilled Cheese Sandwich",
                "Butter the bread slices.\nPlace the cheese between the slices.\nToast in a pan until golden on both sides.",
                "bread,2,pcs", "cheese,40,g", "butter,10,g");
        addRecipe(db, "Pancakes",
                "Mix flour, egg, milk and sugar into a batter.\nMelt butter in a pan.\nCook spoonfuls of batter until golden on both sides.",
                "flour,150,g", "egg,1,pcs", "milk,200,ml", "sugar,20,g", "butter,15,g");
        addRecipe(db, "Mashed Potatoes",
                "Peel and boil the potatoes until soft.\nDrain and mash them.\nMix in butter, milk and salt.",
                "potato,4,pcs", "butter,30,g", "milk,60,ml", "salt,2,g");
        addRecipe(db, "Chicken Rice Bowl",
                "Cook the rice.\nFry the onion and chicken in oil with salt until cooked through.\nServe the chicken on the rice.",
                "chicken,200,g", "rice,150,g", "onion,1,pcs", "oil,15,ml", "salt,2,g");
        addRecipe(db, "Vegetable Soup",
                "Chop the carrots, potatoes and onion.\nFry the onion in oil.\nAdd the other vegetables, water and salt.\nSimmer for 25 minutes.",
                "carrot,2,pcs", "potato,2,pcs", "onion,1,pcs", "salt,2,g", "oil,15,ml");
        addRecipe(db, "Tuna Salad",
                "Drain the tuna.\nTear the lettuce.\nMix everything with mayonnaise.",
                "tuna,1,pcs", "mayonnaise,30,g", "lettuce,1,pcs");
        addRecipe(db, "French Toast",
                "Whisk the eggs, milk and sugar.\nDip the bread slices.\nFry in butter until golden on both sides.",
                "bread,4,pcs", "egg,2,pcs", "milk,100,ml", "butter,15,g", "sugar,10,g");
        addRecipe(db, "Garlic Bread",
                "Mix the butter with crushed garlic.\nSpread on the bread.\nBake at 200C for 10 minutes.",
                "bread,4,pcs", "butter,40,g", "garlic,2,pcs");
        addRecipe(db, "Creamy Cheese Pasta",
                "Boil the pasta.\nMelt butter and milk in a pan.\nStir in the cheese until smooth.\nMix in the pasta.",
                "pasta,200,g", "cheese,80,g", "butter,20,g", "milk,100,ml");
        addRecipe(db, "Baked Potatoes",
                "Wash and prick the potatoes.\nRub with olive oil and salt.\nBake at 200C for 50 minutes.",
                "potato,4,pcs", "olive oil,15,ml", "salt,3,g");
        addRecipe(db, "Egg Fried Noodles",
                "Boil the noodles and drain.\nFry the onion in oil and scramble in the eggs.\nAdd the noodles and soy sauce and toss.",
                "noodle,150,g", "egg,2,pcs", "soy sauce,15,ml", "oil,15,ml", "onion,1,pcs");
        addRecipe(db, "Banana Smoothie",
                "Peel the bananas.\nBlend with the cold milk until smooth.",
                "banana,2,pcs", "milk,250,ml");
        addRecipe(db, "Peanut Butter Sandwich",
                "Spread the peanut butter on one slice of bread.\nTop with the second slice.",
                "bread,2,pcs", "peanut butter,30,g");
        addRecipe(db, "Beef Stir Fry",
                "Slice the beef, onion and carrot.\nFry the beef in hot oil.\nAdd the vegetables and soy sauce.\nStir fry for 6 minutes.",
                "beef,200,g", "onion,1,pcs", "carrot,1,pcs", "soy sauce,15,ml", "oil,15,ml");
        addRecipe(db, "Tomato Soup",
                "Chop the tomatoes and onion.\nFry the onion in butter.\nAdd the tomatoes and salt and simmer 20 minutes.\nBlend until smooth.",
                "tomato,6,pcs", "onion,1,pcs", "butter,20,g", "salt,2,g");
        addRecipe(db, "Oatmeal Porridge",
                "Put the oats and milk in a pot.\nStir over low heat for 5 minutes.\nSweeten with sugar.",
                "oat,80,g", "milk,250,ml", "sugar,10,g");
    }

    // Inserts ONE recipe and all of its ingredients.
    // "String... ingredients" lets us pass any number of ingredient strings.
    private void addRecipe(SQLiteDatabase db, String name, String steps, String... ingredients) {
        ContentValues rv = new ContentValues();
        rv.put(COL_NAME, name);
        rv.put(COL_STEPS, steps);
        long recipeId = db.insert(TABLE_RECIPES, null, rv); // id of the new recipe

        for (String ing : ingredients) {
            String[] parts = ing.split(","); // "egg,3,pcs" -> ["egg","3","pcs"]
            ContentValues iv = new ContentValues();
            iv.put(COL_RECIPE_ID, recipeId); // links the ingredient to its recipe
            iv.put(COL_NAME, parts[0].trim());
            iv.put(COL_QTY, Double.parseDouble(parts[1].trim()));
            iv.put(COL_UNIT, parts[2].trim());
            db.insert(TABLE_RECIPE_ING, null, iv);
        }
    }

    // ===================== READ RECIPES =====================
    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_RECIPES, null, null, null, null, null, COL_NAME + " ASC");

        while (c.moveToNext()) {
            int id = c.getInt(c.getColumnIndexOrThrow(COL_ID));
            Recipe recipe = new Recipe(id,
                    c.getString(c.getColumnIndexOrThrow(COL_NAME)),
                    c.getString(c.getColumnIndexOrThrow(COL_STEPS)));
            recipe.setIngredients(getIngredientsForRecipe(db, id)); // load its ingredients
            recipes.add(recipe);
        }
        c.close();
        return recipes;
    }

    // Loads every ingredient row that belongs to one recipe.
    private List<RecipeIngredient> getIngredientsForRecipe(SQLiteDatabase db, int recipeId) {
        List<RecipeIngredient> list = new ArrayList<>();
        Cursor c = db.query(TABLE_RECIPE_ING, null, COL_RECIPE_ID + "=?",
                new String[]{String.valueOf(recipeId)}, null, null, null);
        while (c.moveToNext()) {
            list.add(new RecipeIngredient(
                    c.getString(c.getColumnIndexOrThrow(COL_NAME)),
                    c.getDouble(c.getColumnIndexOrThrow(COL_QTY)),
                    c.getString(c.getColumnIndexOrThrow(COL_UNIT))));
        }
        c.close();
        return list;
    }
    // Load ONE recipe (with its ingredients) by id, for the detail screen.
    public Recipe getRecipe(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_RECIPES, null, COL_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        Recipe recipe = null;
        if (c.moveToFirst()) {
            recipe = new Recipe(id,
                    c.getString(c.getColumnIndexOrThrow(COL_NAME)),
                    c.getString(c.getColumnIndexOrThrow(COL_STEPS)));
            recipe.setIngredients(getIngredientsForRecipe(db, id));
        }
        c.close();
        return recipe; // null if no recipe has this id
    }
}
