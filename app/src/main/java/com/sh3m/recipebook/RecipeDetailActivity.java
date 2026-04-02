package com.sh3m.recipebook;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

import java.io.File;

public class RecipeDetailActivity extends AppCompatActivity {

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

        Button btnEdit = findViewById(R.id.btnEdit);
        Button btnDelete = findViewById(R.id.btnDelete);

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

        if (getSupportActionBar() != null) getSupportActionBar().setTitle(recipe.name);

        // Recipe section: use name as wrapper title
        TextView tvSectionTitle = findViewById(R.id.tvSectionTitle);
        tvSectionTitle.setText(recipe.name.toUpperCase());

        // Image
        ImageView imgHero = findViewById(R.id.imgHero);
        LinearLayout heroPlaceholder = findViewById(R.id.heroPlaceholder);
        if (recipe.imagePath != null && !recipe.imagePath.isEmpty() && new File(recipe.imagePath).exists()) {
            imgHero.setVisibility(View.VISIBLE);
            heroPlaceholder.setVisibility(View.GONE);
            Glide.with(this).load(new File(recipe.imagePath)).centerCrop().into(imgHero);
        } else {
            imgHero.setVisibility(View.GONE);
            heroPlaceholder.setVisibility(View.VISIBLE);
        }

        // Description
        TextView tvDescription = findViewById(R.id.tvDescription);
        if (!TextUtils.isEmpty(recipe.description)) {
            tvDescription.setText(recipe.description);
            tvDescription.setVisibility(View.VISIBLE);
        } else {
            tvDescription.setVisibility(View.GONE);
        }

        // Ingredients
        LinearLayout sectionIngredients = findViewById(R.id.sectionIngredients);
        LinearLayout ingredientsList = findViewById(R.id.ingredientsList);
        if (!recipe.ingredients.isEmpty()) {
            sectionIngredients.setVisibility(View.VISIBLE);
            ingredientsList.removeAllViews();
            for (int i = 0; i < recipe.ingredients.size(); i++) {
                Ingredient ing = recipe.ingredients.get(i);
                LinearLayout rowLayout = new LinearLayout(this);
                rowLayout.setOrientation(LinearLayout.HORIZONTAL);
                rowLayout.setPadding(0, dpToPx(10), 0, dpToPx(10));

                TextView tvIng = new TextView(this);
                tvIng.setText(ing.ingredient);
                tvIng.setTextSize(15);
                tvIng.setTextColor(ContextCompat.getColor(this, R.color.dark_text));
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                tvIng.setLayoutParams(params);

                TextView tvAmt = new TextView(this);
                tvAmt.setText(ing.amount);
                tvAmt.setTextSize(14);
                tvAmt.setTextColor(ContextCompat.getColor(this, R.color.accent));
                tvAmt.setTypeface(null, android.graphics.Typeface.BOLD);

                rowLayout.addView(tvIng);
                rowLayout.addView(tvAmt);

                // Divider (except last)
                ingredientsList.addView(rowLayout);
                if (i < recipe.ingredients.size() - 1) {
                    View divider = new View(this);
                    LinearLayout.LayoutParams dp = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, 1);
                    divider.setLayoutParams(dp);
                    divider.setBackgroundColor(ContextCompat.getColor(this, R.color.dark_border));
                    ingredientsList.addView(divider);
                }
            }
        } else {
            sectionIngredients.setVisibility(View.GONE);
        }

        // Steps
        LinearLayout sectionInstructions = findViewById(R.id.sectionInstructions);
        LinearLayout stepsList = findViewById(R.id.stepsList);
        if (!recipe.steps.isEmpty()) {
            sectionInstructions.setVisibility(View.VISIBLE);
            stepsList.removeAllViews();
            for (Step step : recipe.steps) {
                LinearLayout rowLayout = new LinearLayout(this);
                rowLayout.setOrientation(LinearLayout.HORIZONTAL);
                int mb = dpToPx(14);
                LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                rp.setMargins(0, 0, 0, mb);
                rowLayout.setLayoutParams(rp);

                // Badge
                TextView badge = new TextView(this);
                badge.setText(String.valueOf(step.stepNumber));
                badge.setTextSize(12);
                badge.setTextColor(ContextCompat.getColor(this, R.color.white));
                badge.setTypeface(null, android.graphics.Typeface.BOLD);
                badge.setGravity(android.view.Gravity.CENTER);
                badge.setBackgroundColor(ContextCompat.getColor(this, R.color.accent));
                int size = dpToPx(26);
                LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(size, size);
                bp.setMargins(0, dpToPx(1), dpToPx(12), 0);
                badge.setLayoutParams(bp);

                // Step text
                TextView tvStep = new TextView(this);
                tvStep.setText(step.text);
                tvStep.setTextSize(15);
                tvStep.setTextColor(ContextCompat.getColor(this, R.color.dark_text));
                tvStep.setLineSpacing(0, 1.4f);
                LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(0,
                        LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                tvStep.setLayoutParams(tp);

                rowLayout.addView(badge);
                rowLayout.addView(tvStep);
                stepsList.addView(rowLayout);
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
                .setPositiveButton(R.string.delete, (d, w) -> {
                    dbHelper.deleteRecipe(recipeId);
                    finish();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
}
