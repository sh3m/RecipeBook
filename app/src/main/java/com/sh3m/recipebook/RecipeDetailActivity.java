package com.sh3m.recipebook;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;

public class RecipeDetailActivity extends Activity {

    public static final String EXTRA_RECIPE_ID = "recipe_id";

    private RecipeDatabaseHelper dbHelper;
    private long recipeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.apply(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        dbHelper = new RecipeDatabaseHelper(this);
        recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);

        Button btnEdit = (Button) findViewById(R.id.btnEdit);
        Button btnDelete = (Button) findViewById(R.id.btnDelete);

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddRecipeActivity.class);
            intent.putExtra(AddRecipeActivity.EXTRA_RECIPE_ID, recipeId);
            startActivity(intent);
        });
        btnDelete.setOnClickListener(v -> confirmDelete());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecipe();
    }

    private void loadRecipe() {
        Recipe recipe = dbHelper.getRecipe(recipeId);
        if (recipe == null) { finish(); return; }

        ((TextView) findViewById(R.id.tvSectionTitle)).setText(recipe.name.toUpperCase());

        ImageView imgHero = (ImageView) findViewById(R.id.imgHero);
        LinearLayout heroPlaceholder = (LinearLayout) findViewById(R.id.heroPlaceholder);
        if (recipe.imagePath != null && !recipe.imagePath.isEmpty() && new File(recipe.imagePath).exists()) {
            android.graphics.Bitmap bmp = BitmapFactory.decodeFile(recipe.imagePath);
            if (bmp != null) {
                imgHero.setVisibility(View.VISIBLE);
                heroPlaceholder.setVisibility(View.GONE);
                imgHero.setImageBitmap(bmp);
            } else {
                imgHero.setVisibility(View.GONE);
                heroPlaceholder.setVisibility(View.VISIBLE);
            }
        } else {
            imgHero.setVisibility(View.GONE);
            heroPlaceholder.setVisibility(View.VISIBLE);
        }

        TextView tvDescription = (TextView) findViewById(R.id.tvDescription);
        if (!TextUtils.isEmpty(recipe.description)) {
            tvDescription.setText(recipe.description);
            tvDescription.setVisibility(View.VISIBLE);
        } else {
            tvDescription.setVisibility(View.GONE);
        }

        LinearLayout sectionIngredients = (LinearLayout) findViewById(R.id.sectionIngredients);
        LinearLayout ingredientsList = (LinearLayout) findViewById(R.id.ingredientsList);
        if (!recipe.ingredients.isEmpty()) {
            sectionIngredients.setVisibility(View.VISIBLE);
            ingredientsList.removeAllViews();
            for (int i = 0; i < recipe.ingredients.size(); i++) {
                Ingredient ing = recipe.ingredients.get(i);
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                row.setPadding(0, dpToPx(10), 0, dpToPx(10));

                TextView tvIng = new TextView(this);
                tvIng.setText(ing.ingredient);
                tvIng.setTextSize(15);
                tvIng.setTextColor(getResources().getColor(R.color.dark_text));
                tvIng.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

                TextView tvAmt = new TextView(this);
                tvAmt.setText(ing.amount);
                tvAmt.setTextSize(14);
                tvAmt.setTextColor(getResources().getColor(R.color.accent));
                tvAmt.setTypeface(null, Typeface.BOLD);

                row.addView(tvIng);
                row.addView(tvAmt);
                ingredientsList.addView(row);

                if (i < recipe.ingredients.size() - 1) {
                    View divider = new View(this);
                    LinearLayout.LayoutParams dp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1);
                    divider.setLayoutParams(dp);
                    divider.setBackgroundColor(getResources().getColor(R.color.dark_border));
                    ingredientsList.addView(divider);
                }
            }
        } else {
            sectionIngredients.setVisibility(View.GONE);
        }

        LinearLayout sectionInstructions = (LinearLayout) findViewById(R.id.sectionInstructions);
        LinearLayout stepsList = (LinearLayout) findViewById(R.id.stepsList);
        if (!recipe.steps.isEmpty()) {
            sectionInstructions.setVisibility(View.VISIBLE);
            stepsList.removeAllViews();
            for (Step step : recipe.steps) {
                LinearLayout row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                rp.setMargins(0, 0, 0, dpToPx(14));
                row.setLayoutParams(rp);

                TextView badge = new TextView(this);
                badge.setText(String.valueOf(step.stepNumber));
                badge.setTextSize(12);
                badge.setTextColor(getResources().getColor(R.color.white));
                badge.setTypeface(null, Typeface.BOLD);
                badge.setGravity(Gravity.CENTER);
                badge.setBackgroundColor(getResources().getColor(R.color.accent));
                int size = dpToPx(26);
                LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(size, size);
                bp.setMargins(0, dpToPx(1), dpToPx(12), 0);
                badge.setLayoutParams(bp);

                TextView tvStep = new TextView(this);
                tvStep.setText(step.text);
                tvStep.setTextSize(15);
                tvStep.setTextColor(getResources().getColor(R.color.dark_text));
                tvStep.setLineSpacing(0, 1.4f);
                tvStep.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

                row.addView(badge);
                row.addView(tvStep);
                stepsList.addView(row);
            }
        } else {
            sectionInstructions.setVisibility(View.GONE);
        }
    }

    private void confirmDelete() {
        Recipe recipe = dbHelper.getRecipe(recipeId);
        if (recipe == null) return;
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_title)
                .setMessage(getString(R.string.delete_message, recipe.name))
                .setPositiveButton(R.string.delete, (d, w) -> { dbHelper.deleteRecipe(recipeId); finish(); })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}
