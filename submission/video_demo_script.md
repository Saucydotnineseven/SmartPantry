# Smart Pantry Manager — Demo Video Script

**Target length:** about 6 minutes 20 seconds  
**Format:** one screen recording with your voice throughout

## Before you record

- Connect and mirror your phone to the PC.
- Open the GitHub repository and Android Studio before starting OBS.
- Run the app from Android Studio so Database Inspector can attach to it.
- Make sure the pantry contains Eggs (3 pcs), Butter (10 g), and Salt (2 g).
- In OBS, capture the screen that shows the phone mirror and the windows you will switch to. Record at 1080p if your PC supports it, use your microphone, and save one MP4 recording.
- The sample add/edit cycle below uses Milk (250 ml) with expiry date `2026-11-06`. Use the format `yyyy-MM-dd` and adjust the date if needed so it is within three days of your recording.

## 0:00–0:50 — Introduction and GitHub

**On screen:** Open the SmartPantry GitHub repository. Show the README and scroll through the commit history.

**Say:**

“Hello, this video demonstrates my Smart Pantry Manager app for Mobile App Development 700. It helps users keep track of ingredients they have and find recipes they can make without buying missing ingredients.

This is my GitHub repository. The commit history shows the project being built in steps, including the database, recipe matcher, screens, and report. The README explains how to open and run the app. I wrote the Android app in Java and chose SQLite to store data on the phone.”

## 0:50–3:35 — Live app demonstration

### Pantry and CRUD cycle

**On screen:** Switch to the mirrored phone and show the pantry list.

**Say:** “This is the pantry list. It shows each ingredient and its quantity and unit. I can add, view, edit, and delete pantry items.”

**On screen:** Tap **+**. Add Milk, quantity `250`, unit `ml`, expiry `2026-11-06`, then save. Show Milk in the list. Tap Milk, change the quantity to `300`, and save. Long-press Milk and confirm deletion.

**Say:** “I’m adding Milk to demonstrate creating a record. It appears in the list, so I can read it. I’ll edit its amount, save the change, and then delete it. The app asks me to confirm before deleting.”

### Suggested recipes and strict matching

**On screen:** Open **Recipes**. Show Scrambled Eggs, then tap it to open the recipe detail screen.

**Say:** “The app suggests Scrambled Eggs because my pantry has eggs, butter, and salt in the required amounts. The detail screen shows the full ingredient list and cooking steps.”

**On screen:** Return to the pantry, long-press Salt, and confirm deletion. Open **Recipes** again and show the no-match message. Return to the pantry and add Salt again: quantity `2`, unit `g`.

**Say:** “Now I’ve removed Salt, which the recipe needs. The app no longer suggests Scrambled Eggs. This demonstrates the strict rule: one missing ingredient is enough to exclude a recipe. I’ll add Salt back so the pantry is ready for the next part.”

### Persistence and database

**On screen:** Close the app from the recent-apps screen, reopen it, and show that the pantry data is still there. Switch to Android Studio. Open **View → Tool Windows → App Inspection → Database Inspector**. Select `com.example.smartpantry`, expand `smart_pantry.db`, and open the `pantry` table.

**Say:** “After closing and reopening the app, the pantry data is still present. This is the SQLite database stored on the phone. The pantry table contains my ingredients. The database also has recipe and recipe-ingredient tables.”

## 3:35–5:40 — Explain the code

**On screen:** In Android Studio, open `RecipeMatcher.java` and point to the loop that checks the recipe ingredients and compares quantities.

**Say:** “First, this is the strict-matching logic. The app checks each ingredient the recipe needs. If an ingredient is missing, or if I do not have enough, the method returns false. The recipe is suggested only after every ingredient passes. The matcher also handles simple plural names and supported unit conversions.”

**On screen:** Open `DatabaseHelper.java`. Show the table creation, recipe seeding, and a pantry CRUD method.

**Say:** “Second, DatabaseHelper manages the SQLite database. It creates the tables, adds the recipe collection when needed, and has methods to add, read, update, and delete pantry records. SQLite keeps these records on the device after the app closes.”

**On screen:** Open `MainActivity.java` and point to `onResume()` and `loadPantry()`. Then show `PantryAdapter.java`.

**Say:** “Third, Android calls onResume when the pantry screen becomes active again. This app reloads the pantry there, so changes made in the form show up when I return. PantryAdapter connects the pantry data to the RecyclerView rows on screen.”

## 5:40–6:20 — Why SQLite

**On screen:** Keep `DatabaseHelper.java` visible or return briefly to Database Inspector.

**Say:**

“I chose SQLite because the app stores a small amount of related information directly on the phone. Pantry items, recipes, and recipe ingredients fit naturally into tables. SQLite works offline, needs no account or server, and demonstrates persistent storage and CRUD operations for this assignment. Thank you for watching.”

## Final recording check

- Keep the full video between 5 and 7 minutes and narrate throughout.
- Make the phone text and Android Studio code readable in the recording.
- Save the recording as an MP4 and include it in the final submission ZIP.
- The recipe detail screenshot is not included in the report, but show the live recipe detail screen during this video demonstration.
