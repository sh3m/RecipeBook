package com.sh3m.recipebook;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecipeAdapter adapter;
    private RecipeDatabaseHelper dbHelper;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.apply(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new RecipeDatabaseHelper(this);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        tvEmpty = findViewById(R.id.tvEmpty);
        ImageButton btnAdd = findViewById(R.id.btnAdd);
        ImageButton btnNightMode = findViewById(R.id.btnNightMode);

        adapter = new RecipeAdapter(this, new ArrayList<>(), recipe -> {
            Intent intent = new Intent(this, RecipeDetailActivity.class);
            intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.id);
            startActivity(intent);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        btnAdd.setOnClickListener(v ->
                startActivity(new Intent(this, AddRecipeActivity.class)));

        btnNightMode.setImageResource(ThemeManager.isDark(this)
                ? R.drawable.ic_sun : R.drawable.ic_moon);

        btnNightMode.setOnClickListener(v -> {
            ThemeManager.toggle(this);
            recreate();
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
