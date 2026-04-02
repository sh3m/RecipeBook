package com.sh3m.recipebook;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AddRecipeActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "recipe_id";

    private EditText etName, etDescription;
    private ImageView imgPreview;
    private LinearLayout imagePlaceholder;
    private Button btnChangePhoto;
    private LinearLayout ingredientsList, stepsList;

    private String imagePath;
    private Recipe existingRecipe;
    private RecipeDatabaseHelper dbHelper;

    private File cameraImageFile;

    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) saveImageFromUri(uri);
                }
            }
    );

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && cameraImageFile != null) {
                    imagePath = cameraImageFile.getAbsolutePath();
                    showImagePreview();
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.apply(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        dbHelper = new RecipeDatabaseHelper(this);

        etName = findViewById(R.id.etName);
        etDescription = findViewById(R.id.etDescription);
        imgPreview = findViewById(R.id.imgPreview);
        imagePlaceholder = findViewById(R.id.imagePlaceholder);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);
        ingredientsList = findViewById(R.id.ingredientsList);
        stepsList = findViewById(R.id.stepsList);

        FrameLayout imageContainer = findViewById(R.id.imageContainer);
        imageContainer.setOnClickListener(v -> showImageOptions());
        btnChangePhoto.setOnClickListener(v -> showImageOptions());

        findViewById(R.id.btnAddIngredient).setOnClickListener(v -> addIngredientRow("", ""));
        findViewById(R.id.btnAddStep).setOnClickListener(v -> addStepRow(""));

        Button btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> saveRecipe());

        long recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
        if (recipeId != -1) {
            existingRecipe = dbHelper.getRecipe(recipeId);
            if (existingRecipe != null) populateForm();
            btnSave.setText(R.string.save_changes);
            if (getSupportActionBar() != null)
                getSupportActionBar().setTitle(R.string.edit_recipe);
        }
    }

    private void populateForm() {
        etName.setText(existingRecipe.name);
        etDescription.setText(existingRecipe.description);

        if (existingRecipe.imagePath != null && !existingRecipe.imagePath.isEmpty()) {
            imagePath = existingRecipe.imagePath;
            showImagePreview();
        }

        for (Ingredient ing : existingRecipe.ingredients) {
            addIngredientRow(ing.ingredient, ing.amount);
        }
        for (Step step : existingRecipe.steps) {
            addStepRow(step.text);
        }
    }

    private void showImageOptions() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.photo_source_title)
                .setItems(new CharSequence[]{
                        getString(R.string.camera),
                        getString(R.string.gallery)
                }, (dialog, which) -> {
                    if (which == 0) launchCamera();
                    else launchGallery();
                })
                .show();
    }

    private void launchGallery() {
        String permission = Build.VERSION.SDK_INT >= 33
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, 101);
            return;
        }
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private void launchCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 102);
            return;
        }
        try {
            File dir = getExternalCacheDir() != null ? getExternalCacheDir() : getCacheDir();
            cameraImageFile = new File(dir, "camera_" + System.currentTimeMillis() + ".jpg");
            Uri uri = FileProvider.getUriForFile(this,
                    getPackageName() + ".fileprovider", cameraImageFile);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            cameraLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Camera unavailable", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveImageFromUri(Uri uri) {
        try {
            File dir = getCacheDir();
            File dest = new File(dir, "recipe_" + System.currentTimeMillis() + ".jpg");
            try (InputStream in = getContentResolver().openInputStream(uri);
                 FileOutputStream out = new FileOutputStream(dest)) {
                byte[] buf = new byte[8192];
                int len;
                assert in != null;
                while ((len = in.read(buf)) != -1) out.write(buf, 0, len);
            }
            imagePath = dest.getAbsolutePath();
            showImagePreview();
        } catch (IOException e) {
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
        }
    }

    private void showImagePreview() {
        imgPreview.setVisibility(View.VISIBLE);
        imagePlaceholder.setVisibility(View.GONE);
        btnChangePhoto.setVisibility(View.VISIBLE);
        Glide.with(this).load(new File(imagePath)).centerCrop().into(imgPreview);
    }

    // ---- Ingredient rows ----

    private void addIngredientRow(String ingredient, String amount) {
        View row = LayoutInflater.from(this).inflate(R.layout.item_ingredient, ingredientsList, false);
        EditText etIng = row.findViewById(R.id.etIngredient);
        EditText etAmt = row.findViewById(R.id.etAmount);
        ImageButton btnRemove = row.findViewById(R.id.btnRemove);

        int idx = ingredientsList.getChildCount() + 1;
        etIng.setHint("Ingredient " + idx);
        etIng.setText(ingredient);
        etAmt.setText(amount);
        btnRemove.setOnClickListener(v -> {
            ingredientsList.removeView(row);
            renumberIngredients();
        });

        ingredientsList.addView(row);
    }

    private void renumberIngredients() {
        for (int i = 0; i < ingredientsList.getChildCount(); i++) {
            View row = ingredientsList.getChildAt(i);
            EditText etIng = row.findViewById(R.id.etIngredient);
            if (TextUtils.isEmpty(etIng.getText())) {
                etIng.setHint("Ingredient " + (i + 1));
            }
        }
    }

    // ---- Step rows ----

    private void addStepRow(String text) {
        View row = LayoutInflater.from(this).inflate(R.layout.item_step, stepsList, false);
        TextView tvNum = row.findViewById(R.id.tvStepNumber);
        EditText etStep = row.findViewById(R.id.etStep);
        ImageButton btnRemove = row.findViewById(R.id.btnRemove);

        int num = stepsList.getChildCount() + 1;
        tvNum.setText(String.valueOf(num));
        etStep.setHint("Step " + num);
        etStep.setText(text);
        btnRemove.setOnClickListener(v -> {
            stepsList.removeView(row);
            renumberSteps();
        });

        stepsList.addView(row);
    }

    private void renumberSteps() {
        for (int i = 0; i < stepsList.getChildCount(); i++) {
            View row = stepsList.getChildAt(i);
            TextView tvNum = row.findViewById(R.id.tvStepNumber);
            tvNum.setText(String.valueOf(i + 1));
        }
    }

    // ---- Save ----

    private void saveRecipe() {
        String name = etName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) {
            etName.setError("Recipe name is required");
            etName.requestFocus();
            return;
        }

        Recipe recipe = existingRecipe != null ? existingRecipe : new Recipe();
        recipe.name = name;
        recipe.description = etDescription.getText().toString().trim();
        recipe.imagePath = imagePath;

        // Collect ingredients
        recipe.ingredients = new ArrayList<>();
        for (int i = 0; i < ingredientsList.getChildCount(); i++) {
            View row = ingredientsList.getChildAt(i);
            String ing = ((EditText) row.findViewById(R.id.etIngredient)).getText().toString().trim();
            String amt = ((EditText) row.findViewById(R.id.etAmount)).getText().toString().trim();
            if (!ing.isEmpty() || !amt.isEmpty()) {
                recipe.ingredients.add(new Ingredient(recipe.id, ing, amt, i));
            }
        }

        // Collect steps
        recipe.steps = new ArrayList<>();
        int stepNum = 1;
        for (int i = 0; i < stepsList.getChildCount(); i++) {
            View row = stepsList.getChildAt(i);
            String text = ((EditText) row.findViewById(R.id.etStep)).getText().toString().trim();
            if (!text.isEmpty()) {
                recipe.steps.add(new Step(recipe.id, text, stepNum++));
            }
        }

        if (existingRecipe != null) {
            dbHelper.updateRecipe(recipe);
        } else {
            dbHelper.insertRecipe(recipe);
        }

        finish();
    }
}
