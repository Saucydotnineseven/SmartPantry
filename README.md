# Smart Pantry Manager

A Java Android app that helps reduce food waste. The user tracks the ingredients they already have at home (their "pantry"), and the app suggests recipes they can cook using **strictly** those ingredients, with no shopping trip needed.

Built for **Mobile App Development 700** at Richfield Graduate Institute of Technology.

- **Student:** [Mphoentle Thomas]
- **Student number:** [402411462]

## Features

- **Pantry management:** add, edit and delete ingredients (name, quantity, unit, optional expiry date), with input validation on the form.
- **Pantry list:** a RecyclerView with a custom adapter, bound to the database.
- **Recipe collection:** 20 recipes pre-loaded into the database on first run.
- **Suggested Recipes:** shows only recipes the user can make right now.
- **Recipe Detail:** full ingredient list and numbered method.
- **Settings:** expiring-soon alert toggle and a units preference, saved with SharedPreferences.
- **Navigation:** bottom navigation bar (Pantry, Recipes, Settings).
- **Empty states:** a friendly message when the pantry is empty or when no recipes match.

## The strict-matching rule

A recipe is suggested **only if every ingredient it needs is in the pantry, in at least the required quantity**. If a recipe needs 5 ingredients and the pantry has 4, it is not shown.

The matching logic is in `RecipeMatcher.java`. It also copes with simple real-world differences:

- upper and lower case ("Eggs" and "eggs")
- simple plurals ("tomato" and "tomatoes")
- unit differences within the same family (for example 0.05 kg counts as 50 g, and 2 tbsp counts as 30 ml)

## Database choice: SQLite

The app uses **SQLite** through `SQLiteOpenHelper`. I chose it because:

- it runs locally on the device, so there is no account, server or internet connection to set up;
- data persists after the app is closed and reopened;
- the data is simple and relational (pantry items, recipes, and the ingredients of each recipe), which suits SQL tables;
- it is the approach covered in the module's persistent data chapter.

Tables: `pantry`, `recipes`, `recipe_ingredients` (one recipe has many ingredients).

## Screens

1. Pantry List (`MainActivity`)
2. Add / Edit Ingredient (`AddEditActivity`)
3. Suggested Recipes (`SuggestedRecipesActivity`)
4. Recipe Detail (`RecipeDetailActivity`)
5. Settings (`SettingsActivity`)

## Setup and run instructions

**Requirements**

- Android Studio (current version)
- JDK 17 (the JetBrains Runtime bundled with Android Studio works)
- An Android phone with USB debugging enabled, or an Android emulator (API 34 recommended)

**Steps**

1. Clone the repository:
   ```
   git clone https://github.com/Saucydotnineseven/SmartPantry.git
   ```
2. Open Android Studio and choose **File, Open**, then select the `SmartPantry` folder.
3. Wait for the Gradle sync to finish (the first sync needs internet).
4. Select your device or emulator in the device dropdown at the top.
5. Click **Run**.

On first launch the database is created and the 20 recipes are loaded automatically. Add a few ingredients, then open **Recipes** to see what you can cook. For example, add Eggs (3 pcs), Butter (10 g) and Salt (2 g) to see "Scrambled Eggs".

## Project structure

| File | Purpose |
|---|---|
| `DatabaseHelper.java` | Creates the database, seeds recipes, and provides CRUD methods |
| `PantryItem.java`, `Recipe.java`, `RecipeIngredient.java` | Data models |
| `RecipeMatcher.java` | Strict-matching logic |
| `PantryAdapter.java`, `RecipeAdapter.java` | RecyclerView adapters |
| `MainActivity.java` | Pantry list, bottom navigation, expiry alert |
| `AddEditActivity.java` | Add and edit form with validation |
| `SuggestedRecipesActivity.java` | Strictly matched recipe list |
| `RecipeDetailActivity.java` | Recipe ingredients and method |
| `SettingsActivity.java` | User preferences |

## Out of scope (by assignment design)

No Google Maps, mapping SDKs or GPS/location features are used.