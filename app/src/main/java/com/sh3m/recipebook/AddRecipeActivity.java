package com.sh3m.recipebook;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class AddRecipeActivity extends Activity {

    public static final String EXTRA_RECIPE_ID = "recipe_id";

    private static final int REQUEST_GALLERY = 1;
    private static final int REQUEST_CAMERA = 2;
    private static final int REQUEST_PERM_GALLERY = 101;
    private static final int REQUEST_PERM_CAMERA = 102;

    private EditText etName, etDescription;
    private ImageView imgPreview;
    private LinearLayout imagePlaceholder;
    private Button btnChangePhoto;
    private LinearLayout ingredientsList, stepsList;

    private String imagePath;
    private Recipe existingRecipe;
    private RecipeDatabaseHelper dbHelper;
    private File cameraImageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ThemeManager.apply(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        dbHelper = new RecipeDatabaseHelper(this);

        etName = (EditText) findViewById(R.id.etName);
        etDescription = (EditText) findViewById(R.id.etDescription);
        imgPreview = (ImageView) findViewById(R.id.imgPreview);
        imagePlaceholder = (LinearLayout) findViewById(R.id.imagePlaceholder);
        btnChangePhoto = (Button) findViewById(R.id.btnChangePhoto);
        ingredientsList = (LinearLayout) findViewById(R.id.ingredientsList);
        stepsList = (LinearLayout) findViewById(R.id.stepsList);

        FrameLayout imageContainer = (FrameLayout) findViewById(R.id.imageContainer);
        imageContainer.setOnClickListener(v -> showImageOptions());
        btnChangePhoto.setOnClickListener(v -> showImageOptions());

        findViewById(R.id.btnAddIngredient).setOnClickListener(v -> addIngredientRow("", ""));
        findViewById(R.id.btnAddStep).setOnClickListener(v -> addStepRow(""));

        Button btnSave = (Button) findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> saveRecipe());

        long recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
        if (recipeId != -1) {
            existingRecipe = dbHelper.getRecipe(recipeId);
            if (existingRecipe != null) populateForm();
            btnSave.setText(R.string.save_changes);
        }
    }

    private void populateForm() {
        etName.setText(existingRecipe.name);
        etDescription.setText(existingRecipe.description);
        if (existingRecipe.imagePath != null && !existingRecipe.imagePath.isEmpty()) {
            imagePath = existingRecipe.imagePath;
            showImagePreview();
        }
        for (Ingredient ing : existingRecipe.ingredients) addIngredientRow(ing.ingredient, ing.amount);
        for (Step step : existingRecipe.steps) addStepRow(step.text);
    }

    private void showImageOptions() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.photo_source_title)
                .setItems(new CharSequence[]{getString(R.string.camera), getString(R.string.gallery)},
                        (dialog, which) -> { if (which == 0) launchCamera(); else launchGallery(); })
                .show();
    }

    private void launchGallery() {
        String permission = Build.VERSION.SDK_INT >= 33
                ? "android.permission.READ_MEDIA_IMAGES"
                : Manifest.permission.READ_EXTERNAL_STORAGE;
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{permission}, REQUEST_PERM_GALLERY);
            return;
        }
        startActivityForResult(new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI), REQUEST_GALLERY);
    }

    private void launchCamera() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_PERM_CAMERA);
            return;
        }
        try {
            if (Build.VERSION.SDK_INT >= 24) {
                StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().build());
            }
            File dir = getExternalCacheDir() != null ? getExternalCacheDir() : getCacheDir();
            cameraImageFile = new File(dir, "camera_" + System.currentTimeMillis() + ".jpg");
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cameraImageFile));
            startActivityForResult(intent, REQUEST_CAMERA);
        } catch (Exception e) {
            Toast.makeText(this, "Camera unavailable", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;
        if (requestCode == REQUEST_GALLERY && data != null) {
            Uri uri = data.getData();
            if (uri != null) saveImageFromUri(uri);
        } else if (requestCode == REQUEST_CAMERA && cameraImageFile != null) {
            imagePath = cameraImageFile.getAbsolutePath();
            showImagePreview();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == REQUEST_PERM_GALLERY) launchGallery();
            else if (requestCode == REQUEST_PERM_CAMERA) launchCamera();
        }
    }

    private void saveImageFromUri(Uri uri) {
        try {
            File dest = new File(getCacheDir(), "recipe_" + System.currentTimeMillis() + ".jpg");
            try (InputStream in = getContentResolver().openInputStream(uri);
                 FileOutputStream out = new FileOutputStream(dest)) {
                byte[] buf = new byte[8192]; int len;
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
        if (imagePath != null) {
            android.graphics.Bitmap bmp = BitmapFactory.decodeFile(imagePath);
            if (bmp != null) imgPreview.setImageBitmap(bmp);
        }
    }

    private void addIngredientRow(String ingredient, String amount) {
        View row = LayoutInflater.from(this).inflate(R.layout.item_ingredient, ingredientsList, false);
        EditText etIng = (EditText) row.findViewById(R.id.etIngredient);
        EditText etAmt = (EditText) row.findViewById(R.id.etAmount);
        ImageButton btnRemove = (ImageButton) row.findViewById(R.id.btnRemove);
        int idx = ingredientsList.getChildCount() + 1;
        etIng.setHint("Ingredient " + idx);
        etIng.setText(ingredient);
        etAmt.setText(amount);
        btnRemove.setOnClickListener(v -> { ingredientsList.removeView(row); renumberIngredients(); });
        ingredientsList.addView(row);
    }

    private void renumberIngredients() {
        for (int i = 0; i < ingredientsList.getChildCount(); i++) {
            View row = ingredientsList.getChildAt(i);
            EditText etIng = (EditText) row.findViewById(R.id.etIngredient);
            if (TextUtils.isEmpty(etIng.getText())) etIng.setHint("Ingredient " + (i + 1));
        }
    }

    private void addStepRow(String text) {
        View row = LayoutInflater.from(this).inflate(R.layout.item_step, stepsList, false);
        TextView tvNum = (TextView) row.findViewById(R.id.tvStepNumber);
        EditText etStep = (EditText) row.findViewById(R.id.etStep);
        ImageButton btnRemove = (ImageButton) row.findViewById(R.id.btnRemove);
        int num = stepsList.getChildCount() + 1;
        tvNum.setText(String.valueOf(num));
        etStep.setHint("Step " + num);
        etStep.setText(text);
        btnRemove.setOnClickListener(v -> { stepsList.removeView(row); renumberSteps(); });
        stepsList.addView(row);
    }

    private void renumberSteps() {
        for (int i = 0; i < stepsList.getChildCount(); i++) {
            View row = stepsList.getChildAt(i);
            ((TextView) row.findViewById(R.id.tvStepNumber)).setText(String.valueOf(i + 1));
        }
    }

    private void saveRecipe() {
        String name = etName.getText().toString().trim();
        if (TextUtils.isEmpty(name)) { etName.setError("Recipe name is required"); etName.requestFocus(); return; }

        Recipe recipe = existingRecipe != null ? existingRecipe : new Recipe();
        recipe.name = name;
        recipe.description = etDescription.getText().toString().trim();
        recipe.imagePath = imagePath;

        recipe.ingredients = new ArrayList<>();
        for (int i = 0; i < ingredientsList.getChildCount(); i++) {
            View row = ingredientsList.getChildAt(i);
            String ing = ((EditText) row.findViewById(R.id.etIngredient)).getText().toString().trim();
            String amt = ((EditText) row.findViewById(R.id.etAmount)).getText().toString().trim();
            if (!ing.isEmpty() || !amt.isEmpty()) recipe.ingredients.add(new Ingredient(recipe.id, ing, amt, i));
        }

        recipe.steps = new ArrayList<>();
        int stepNum = 1;
        for (int i = 0; i < stepsList.getChildCount(); i++) {
            View row = stepsList.getChildAt(i);
            String t = ((EditText) row.findViewById(R.id.etStep)).getText().toString().trim();
            if (!t.isEmpty()) recipe.steps.add(new Step(recipe.id, t, stepNum++));
        }

        if (existingRecipe != null) dbHelper.updateRecipe(recipe); else dbHelper.insertRecipe(recipe);
        finish();
    }
}
