package com.sh3m.recipebook;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class RecipeDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "recipebook.db";
    private static final int DB_VERSION = 1;

    // Tables
    static final String TABLE_RECIPES = "recipes";
    static final String TABLE_INGREDIENTS = "ingredients";
    static final String TABLE_STEPS = "steps";

    // Recipe columns
    static final String COL_ID = "_id";
    static final String COL_NAME = "name";
    static final String COL_DESCRIPTION = "description";
    static final String COL_IMAGE_PATH = "image_path";
    static final String COL_CREATED_AT = "created_at";
    static final String COL_UPDATED_AT = "updated_at";

    // Ingredient columns
    static final String COL_RECIPE_ID = "recipe_id";
    static final String COL_INGREDIENT = "ingredient";
    static final String COL_AMOUNT = "amount";
    static final String COL_SORT_ORDER = "sort_order";

    // Step columns
    static final String COL_STEP_TEXT = "text";
    static final String COL_STEP_NUMBER = "step_number";

    public RecipeDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_RECIPES + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_NAME + " TEXT NOT NULL, " +
                COL_DESCRIPTION + " TEXT, " +
                COL_IMAGE_PATH + " TEXT, " +
                COL_CREATED_AT + " INTEGER, " +
                COL_UPDATED_AT + " INTEGER)");

        db.execSQL("CREATE TABLE " + TABLE_INGREDIENTS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RECIPE_ID + " INTEGER NOT NULL, " +
                COL_INGREDIENT + " TEXT, " +
                COL_AMOUNT + " TEXT, " +
                COL_SORT_ORDER + " INTEGER, " +
                "FOREIGN KEY(" + COL_RECIPE_ID + ") REFERENCES " + TABLE_RECIPES + "(" + COL_ID + ") ON DELETE CASCADE)");

        db.execSQL("CREATE TABLE " + TABLE_STEPS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_RECIPE_ID + " INTEGER NOT NULL, " +
                COL_STEP_TEXT + " TEXT, " +
                COL_STEP_NUMBER + " INTEGER, " +
                "FOREIGN KEY(" + COL_RECIPE_ID + ") REFERENCES " + TABLE_RECIPES + "(" + COL_ID + ") ON DELETE CASCADE)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STEPS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_INGREDIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        db.setForeignKeyConstraintsEnabled(true);
    }

    // ---- CRUD ----

    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_RECIPES, null, null, null, null, null,
                COL_CREATED_AT + " DESC");
        while (c.moveToNext()) {
            Recipe r = cursorToRecipe(c);
            r.ingredients = getIngredients(db, r.id);
            r.steps = getSteps(db, r.id);
            recipes.add(r);
        }
        c.close();
        return recipes;
    }

    public Recipe getRecipe(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_RECIPES, null, COL_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null);
        Recipe r = null;
        if (c.moveToFirst()) {
            r = cursorToRecipe(c);
            r.ingredients = getIngredients(db, r.id);
            r.steps = getSteps(db, r.id);
        }
        c.close();
        return r;
    }

    public long insertRecipe(Recipe recipe) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues cv = new ContentValues();
            cv.put(COL_NAME, recipe.name);
            cv.put(COL_DESCRIPTION, recipe.description);
            cv.put(COL_IMAGE_PATH, recipe.imagePath);
            cv.put(COL_CREATED_AT, recipe.createdAt);
            cv.put(COL_UPDATED_AT, recipe.updatedAt);
            long recipeId = db.insert(TABLE_RECIPES, null, cv);

            insertIngredients(db, recipeId, recipe.ingredients);
            insertSteps(db, recipeId, recipe.steps);

            db.setTransactionSuccessful();
            return recipeId;
        } finally {
            db.endTransaction();
        }
    }

    public void updateRecipe(Recipe recipe) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues cv = new ContentValues();
            cv.put(COL_NAME, recipe.name);
            cv.put(COL_DESCRIPTION, recipe.description);
            cv.put(COL_IMAGE_PATH, recipe.imagePath);
            cv.put(COL_UPDATED_AT, System.currentTimeMillis());
            db.update(TABLE_RECIPES, cv, COL_ID + "=?",
                    new String[]{String.valueOf(recipe.id)});

            db.delete(TABLE_INGREDIENTS, COL_RECIPE_ID + "=?",
                    new String[]{String.valueOf(recipe.id)});
            db.delete(TABLE_STEPS, COL_RECIPE_ID + "=?",
                    new String[]{String.valueOf(recipe.id)});

            insertIngredients(db, recipe.id, recipe.ingredients);
            insertSteps(db, recipe.id, recipe.steps);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void deleteRecipe(long id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_RECIPES, COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    // ---- Helpers ----

    private void insertIngredients(SQLiteDatabase db, long recipeId, List<Ingredient> ingredients) {
        for (int i = 0; i < ingredients.size(); i++) {
            Ingredient ing = ingredients.get(i);
            if (ing.ingredient == null || ing.ingredient.trim().isEmpty()) continue;
            ContentValues cv = new ContentValues();
            cv.put(COL_RECIPE_ID, recipeId);
            cv.put(COL_INGREDIENT, ing.ingredient.trim());
            cv.put(COL_AMOUNT, ing.amount != null ? ing.amount.trim() : "");
            cv.put(COL_SORT_ORDER, i);
            db.insert(TABLE_INGREDIENTS, null, cv);
        }
    }

    private void insertSteps(SQLiteDatabase db, long recipeId, List<Step> steps) {
        int num = 1;
        for (Step step : steps) {
            if (step.text == null || step.text.trim().isEmpty()) continue;
            ContentValues cv = new ContentValues();
            cv.put(COL_RECIPE_ID, recipeId);
            cv.put(COL_STEP_TEXT, step.text.trim());
            cv.put(COL_STEP_NUMBER, num++);
            db.insert(TABLE_STEPS, null, cv);
        }
    }

    private List<Ingredient> getIngredients(SQLiteDatabase db, long recipeId) {
        List<Ingredient> list = new ArrayList<>();
        Cursor c = db.query(TABLE_INGREDIENTS, null,
                COL_RECIPE_ID + "=?", new String[]{String.valueOf(recipeId)},
                null, null, COL_SORT_ORDER + " ASC");
        while (c.moveToNext()) {
            Ingredient ing = new Ingredient();
            ing.id = c.getLong(c.getColumnIndexOrThrow(COL_ID));
            ing.recipeId = recipeId;
            ing.ingredient = c.getString(c.getColumnIndexOrThrow(COL_INGREDIENT));
            ing.amount = c.getString(c.getColumnIndexOrThrow(COL_AMOUNT));
            ing.sortOrder = c.getInt(c.getColumnIndexOrThrow(COL_SORT_ORDER));
            list.add(ing);
        }
        c.close();
        return list;
    }

    private List<Step> getSteps(SQLiteDatabase db, long recipeId) {
        List<Step> list = new ArrayList<>();
        Cursor c = db.query(TABLE_STEPS, null,
                COL_RECIPE_ID + "=?", new String[]{String.valueOf(recipeId)},
                null, null, COL_STEP_NUMBER + " ASC");
        while (c.moveToNext()) {
            Step step = new Step();
            step.id = c.getLong(c.getColumnIndexOrThrow(COL_ID));
            step.recipeId = recipeId;
            step.text = c.getString(c.getColumnIndexOrThrow(COL_STEP_TEXT));
            step.stepNumber = c.getInt(c.getColumnIndexOrThrow(COL_STEP_NUMBER));
            list.add(step);
        }
        c.close();
        return list;
    }

    private Recipe cursorToRecipe(Cursor c) {
        Recipe r = new Recipe();
        r.id = c.getLong(c.getColumnIndexOrThrow(COL_ID));
        r.name = c.getString(c.getColumnIndexOrThrow(COL_NAME));
        r.description = c.getString(c.getColumnIndexOrThrow(COL_DESCRIPTION));
        r.imagePath = c.getString(c.getColumnIndexOrThrow(COL_IMAGE_PATH));
        r.createdAt = c.getLong(c.getColumnIndexOrThrow(COL_CREATED_AT));
        r.updatedAt = c.getLong(c.getColumnIndexOrThrow(COL_UPDATED_AT));
        return r;
    }
}
