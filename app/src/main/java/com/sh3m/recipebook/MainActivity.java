package com.sh3m.recipebook;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private RecipeAdapter adapter;
    private RecipeDatabaseHelper dbHelper;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.apply(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new RecipeDatabaseHelper(this);

        ListView listView = (ListView) findViewById(R.id.recyclerView);
        tvEmpty = (TextView) findViewById(R.id.tvEmpty);
        ImageButton btnAdd = (ImageButton) findViewById(R.id.btnAdd);
        ImageButton btnNightMode = (ImageButton) findViewById(R.id.btnNightMode);

        adapter = new RecipeAdapter(this, new ArrayList<Recipe>(), new RecipeAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Recipe recipe) {
                Intent intent = new Intent(MainActivity.this, RecipeDetailActivity.class);
                intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.id);
                startActivity(intent);
            }
        });

        listView.setAdapter(adapter);

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AddRecipeActivity.class));
            }
        });

        btnNightMode.setImageResource(ThemeManager.isDark(this) ? R.drawable.ic_sun : R.drawable.ic_moon);

        final ImageButton finalBtnNightMode = btnNightMode;
        btnNightMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ThemeManager.toggle(MainActivity.this);
                finalBtnNightMode.setImageResource(ThemeManager.isDark(MainActivity.this) ? R.drawable.ic_sun : R.drawable.ic_moon);
                recreate();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecipes();
    }

    private void loadRecipes() {
        List<Recipe> recipes = dbHelper.getAllRecipes();
        adapter.setRecipes(recipes);
        tvEmpty.setVisibility(recipes.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
