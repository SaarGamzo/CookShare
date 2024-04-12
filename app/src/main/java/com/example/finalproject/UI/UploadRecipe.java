package com.example.finalproject.UI;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finalproject.Models.Ingredient;
import com.example.finalproject.Models.Recipe;
import com.example.finalproject.R;
import com.example.finalproject.Utils.DatabaseUtils;
import com.example.finalproject.Utils.MyUtils;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UploadRecipe extends AppCompatActivity {
    private ImageView menuIcon;
    private TextView headlineTextView;
    private EditText recipeNameEditText;
    private ChipGroup tagsChipGroup;
    private LinearLayout ingredientsLayout;
    private Button addIngredientButton;
    private LinearLayout stepsLayout;
    private Button addStepButton;
    private Button uploadImageButton;
    private Button uploadRecipeButton;
    private ImageView selectedImageView;

    private int ingredientCounter = 1;
    private int stepCounter = 1;

    private static final int REQUEST_IMAGE_PICK = 2;

    private static final int CAMERA_REQUEST = 1;
    private SeekBar cookingTimeSeekBar;
    private TextView selectedCookingTimeText;

    private MyUtils myUtils;

    private List<Integer> stepEditTextIds = new ArrayList<>();

    private List<Integer> chipsIds = new ArrayList<>();

    private int ingredientsIDCounter = 0;

    private int stepsIDCounter = 100;

    private int chipsIDCounter = 1000;

    // Declare a list to store the IDs of ingredient EditTexts
    private List<Integer> ingredientEditTextIds = new ArrayList<>();

    private String userEmail;

    private TextView textAcronyms;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upload_recipe_activity);
        findViews();
        if (getIntent().hasExtra("email")) {
            userEmail = getIntent().getStringExtra("email");
            textAcronyms.setText(getIntent().getStringExtra("textAcronyms"));
        }
        myUtils = MyUtils.getInstance(this);
        selectedImageView.setVisibility(View.GONE);
        setListeners();
        populateTags();
        cookingTimeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Update the selected cooking time text view
                selectedCookingTimeText.setText("Selected Cooking Time: " + progress + " minutes");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Do nothing
            }
        });
        // Set OnClickListener for menuIcon
        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenuOptions();
            }
        });

        // Set OnClickListener for Acronyms TextView
        textAcronyms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent uploadRecipeIntent = new Intent(UploadRecipe.this, PersonalDetails.class);
                uploadRecipeIntent.putExtra("email", userEmail);
                uploadRecipeIntent.putExtra("textAcronyms", textAcronyms.getText());
                startActivity(uploadRecipeIntent);
                finish();
            }
        });
    }

    private void showLogoutDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        logoutUser();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void logoutUser() {
        // Perform logout action
        DatabaseUtils.getInstance().signOutUser();
        startActivity(new Intent(UploadRecipe.this, LoginActivity.class));
        finish(); // Close this activity
    }

    private void showMenuOptions() {
        PopupMenu popupMenu = new PopupMenu(this, menuIcon);
        popupMenu.getMenuInflater().inflate(R.menu.update_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.mainFeed) {
                    // Handle upload recipe action
                    Intent mainIntent = new Intent(UploadRecipe.this, MainFeed.class);
                    mainIntent.putExtra("email", userEmail);
                    mainIntent.putExtra("textAcronyms", textAcronyms.getText());
                    startActivity(mainIntent);
                    finish();
                    return true;
                } else if (id == R.id.logOutUser) {
                    showLogoutDialog();
                    return true;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            if (extras != null && extras.containsKey("data")) {
                Bitmap photo = (Bitmap) extras.get("data");
                if (photo != null) {
                    selectedImageView.setImageBitmap(photo);
                    selectedImageView.setVisibility(View.VISIBLE);
                }
            }
        } else if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                    selectedImageView.setImageBitmap(bitmap);
                    selectedImageView.setVisibility(View.VISIBLE);
                } catch (IOException e) {
                    e.printStackTrace();
                    myUtils.showToast("Error: " + e.getMessage());
                }
            }
        }
    }

    public void setListeners() {
        addIngredientButton.setOnClickListener(v -> addIngredientField());
        addStepButton.setOnClickListener(v -> addStepField());
        uploadImageButton.setOnClickListener(v -> uploadImage());
        uploadRecipeButton.setOnClickListener(v -> onSubmitClicked());
    }

    private void onSubmitClicked() {
        // Check if all required inputs are provided
        List<Ingredient> ingredients = getIngredients();
        List<String> steps = getSteps();
        if (!isRecipeNameValid() || !areTagsSelected() || !areIngredientsAdded()
                || !areStepsAdded() || !isImageUploaded() || !isCookingTimeSelected() || !validateStepsNotEmpty(steps)
                || validateIngredientsNotEmpty(ingredients)) {
            // Show a toast indicating missing inputs
            myUtils.showToast("Please verify the recipe details.");
        } else {
            // All inputs are provided, add the recipe to the Firebase database
            addRecipeToDatabase();
        }
    }

    private boolean validateStepsNotEmpty(List<String> steps) {
        for (String step : steps) {
            if (step.trim().isEmpty()) {
                return false; // At least one step is empty
            }
        }
        return true; // All steps are not empty
    }

    private boolean validateIngredientsNotEmpty(List<Ingredient> ingredients) {
        for (Ingredient ingredient : ingredients) {
            if (ingredient.getName().trim().isEmpty() || ingredient.getQuantity() <= 0) {
                return false; // At least one ingredient is empty or has invalid data
            }
        }
        return true; // All ingredients are not empty and have valid data
    }

    private void addRecipeToDatabase() {
        // Validate inputs
        String recipeName = recipeNameEditText.getText().toString();
        List<String> tags = getSelectedTags();
        List<Ingredient> ingredients = getIngredients();
        List<String> steps = getSteps();
        int cookingTimeMinutes = cookingTimeSeekBar.getProgress();
        long createdTimestamp = System.currentTimeMillis(); // Current timestamp
        String createdBy = getCurrentUserId(); // Get current user ID or username

        // Get the bitmap from the selected image view
        Bitmap bitmap = getImage();
        if (bitmap == null) {
            myUtils.showToast("Please select an image for the recipe.");
            return;
        }

        // Logging: Check if the bitmap is retrieved successfully
        Log.d("UploadRecipe", "Bitmap retrieved successfully");

        // Upload the image to Firebase Storage
        String imageFileName = "recipe_" + recipeName + ".png";
        StorageReference storageRef = DatabaseUtils.getInstance().getFirebaseStorage().getReference().child("images/" + imageFileName);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        UploadTask uploadTask = storageRef.putBytes(data);

        // Logging: Indicate when the image upload starts
        Log.d("UploadRecipe", "Image upload started");

        // Listen for upload success or failure
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            // Image uploaded successfully, get the download URL
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String imageUrl = uri.toString();

                // Create a new Recipe object
                Recipe recipe = new Recipe(recipeName, tags, ingredients, steps, cookingTimeMinutes, createdTimestamp, createdBy, imageUrl);

                // Get a reference to the "Recipes" node in the database
                DatabaseUtils.getInstance().getDatabaseReference().child("Recipes")
                        .child(recipeName)
                        .setValue(recipe)
                        .addOnSuccessListener(aVoid -> {
                            // Recipe added successfully
                            myUtils.showToast("Recipe added to database!");

                            // Logging: Indicate when the recipe is added successfully
                            Log.d("UploadRecipe", "Recipe added to database");
                            finish();
                            // Navigate to MainFeed activity
                            Intent mainFeedIntent = new Intent(UploadRecipe.this, MainFeed.class);
                            mainFeedIntent.putExtra("email", userEmail); // Assuming userEmail is the variable holding the email
                            mainFeedIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(mainFeedIntent);
                        })
                        .addOnFailureListener(e -> {
                            // Error adding recipe to database
                            myUtils.showToast("Failed to add recipe to database: " + e.getMessage());

                            // Logging: Log the error message if adding recipe fails
                            Log.e("UploadRecipe", "Failed to add recipe to database", e);
                        });
            });
        }).addOnFailureListener(e -> {
            // Error uploading image
            myUtils.showToast("Failed to upload image: " + e.getMessage());

            // Logging: Log the error message if image upload fails
            Log.e("UploadRecipe", "Failed to upload image", e);
        });
    }


    private List<String> getSelectedTags() {
        List<String> selectedTags = new ArrayList<>();
        for (int chipId : chipsIds) {
            Chip chip = findViewById(chipId);
            if (chip.isChecked()) {
                selectedTags.add(chip.getText().toString());
            }
        }
        return selectedTags;
    }

    private List<Ingredient> getIngredients() {
        List<Ingredient> ingredients = new ArrayList<>();

        // Loop through each set of ingredient EditTexts
        for (int i = 0; i < ingredientEditTextIds.size(); i += 3) {
            // Get the IDs of the EditTexts for ingredient name, quantity, and unit
            int ingredientNameEditTextId = ingredientEditTextIds.get(i);
            int ingredientQuantityEditTextId = ingredientEditTextIds.get(i + 1);
            int unitSpinnerId = ingredientEditTextIds.get(i + 2);

            // Find the EditTexts using their IDs
            EditText ingredientNameEditText = findViewById(ingredientNameEditTextId);
            EditText ingredientQuantityEditText = findViewById(ingredientQuantityEditTextId);
            Spinner unitSpinner = findViewById(unitSpinnerId);

            // Get the content from the EditTexts
            String ingredientName = ingredientNameEditText.getText().toString().trim();
            String ingredientQuantityStr = ingredientQuantityEditText.getText().toString().trim();
            double ingredientQuantity = Double.parseDouble(ingredientQuantityStr);
            String unit = unitSpinner.getSelectedItem().toString();

            // Create a new Ingredient object and add it to the list
            Ingredient ingredient = new Ingredient(ingredientName, ingredientQuantity, unit);
            ingredients.add(ingredient);
        }

        return ingredients;
    }


    private List<String> getSteps() {
        List<String> steps = new ArrayList<>();

        // Loop through each step EditText
        for (int i = 0; i < stepEditTextIds.size(); i++) {
            int stepEditTextId = stepEditTextIds.get(i);
            // Find the step EditText using its ID
            EditText stepEditText = findViewById(stepEditTextId);

            // Get the content from the step EditText and add it to the list
            String step = stepEditText.getText().toString().trim();
            steps.add(step);
        }

        return steps;
    }


    private Bitmap getImage() {
        BitmapDrawable drawable = (BitmapDrawable) selectedImageView.getDrawable();
        if (drawable != null) {
            return drawable.getBitmap();
        } else {
            return null;
        }
    }

    private String getCurrentUserId() {
        FirebaseUser currentUser = DatabaseUtils.getInstance().getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid();
        } else {
            // Return a default user ID or handle the situation as needed
            return "default_user_id";
        }
    }


    private boolean isRecipeNameValid() {
        String recipeName = recipeNameEditText.getText().toString().trim();
        return !recipeName.isEmpty();
    }

    private boolean areTagsSelected() {
        return tagsChipGroup.getCheckedChipIds().size() > 0;
    }

    private boolean areIngredientsAdded() {
        return ingredientsLayout.getChildCount() > 0;
    }

    private boolean areStepsAdded() {
        return stepsLayout.getChildCount() > 0;
    }

    private boolean isImageUploaded() {
        return selectedImageView.getVisibility() == View.VISIBLE;
    }

    private boolean isCookingTimeSelected() {
        return cookingTimeSeekBar.getProgress() > 0;
    }

    private void uploadImage() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Image Source");
        builder.setItems(new CharSequence[]{"Take a Picture", "Choose from Library"}, (dialog, which) -> {
            switch (which) {
                case 0:
                    takePicture();
                    break;
                case 1:
                    // Choose an image from the device's library
                    chooseFromLibrary();
                    break;
            }
        });
        builder.show();
    }

    private void takePicture() {
        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    private void chooseFromLibrary() {
        Intent pickPhotoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickPhotoIntent.setType("image/*");
        startActivityForResult(pickPhotoIntent, REQUEST_IMAGE_PICK);
    }


    private void addIngredientField() {
        // TextView for ingredient label
        TextView label = new TextView(this);
        // Set margin for the label
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        labelParams.setMargins(0, 150, 0, 16); // Left, top, right, bottom margins
        label.setLayoutParams(labelParams);
        label.setText("Ingredient #" + ingredientCounter++);
        label.setTextColor(getResources().getColor(android.R.color.black));
        label.setTextSize(20); // Adjust font size as needed
        label.setTypeface(null, Typeface.BOLD);
        ingredientsLayout.addView(label);

        // EditText for ingredient name
        EditText ingredientNameEditText = new EditText(this);
        ingredientNameEditText.setId(ingredientsIDCounter);
        ingredientEditTextIds.add(ingredientsIDCounter);
        ingredientsIDCounter++;
        LinearLayout.LayoutParams nameEditTextParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        nameEditTextParams.setMargins(0, 0, 0, 16); // Left, top, right, bottom margins
        ingredientNameEditText.setLayoutParams(nameEditTextParams);
        ingredientNameEditText.setHint("Enter ingredient name");
        ingredientNameEditText.setHintTextColor(getResources().getColor(android.R.color.black));
        ingredientNameEditText.setTextColor(-16777216);
        ingredientsLayout.addView(ingredientNameEditText);

        // EditText for ingredient quantity
        EditText ingredientQuantityEditText = new EditText(this);
        ingredientQuantityEditText.setId(ingredientsIDCounter);
        ingredientEditTextIds.add(ingredientsIDCounter);
        ingredientsIDCounter++;
        LinearLayout.LayoutParams quantityEditTextParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        quantityEditTextParams.setMargins(0, 0, 0, 16); // Left, top, right, bottom margins
        ingredientQuantityEditText.setLayoutParams(quantityEditTextParams);
        ingredientQuantityEditText.setHint("Enter quantity");
        ingredientQuantityEditText.setTextColor(-16777216);
        ingredientQuantityEditText.setHintTextColor(getResources().getColor(android.R.color.black));
        ingredientsLayout.addView(ingredientQuantityEditText);

        // Spinner for selecting unit
        Spinner unitSpinner = new Spinner(this);
        String unitSpinnerId = "UnitSpinner" + ingredientCounter;
        unitSpinner.setId(ingredientsIDCounter);
        ingredientEditTextIds.add(ingredientsIDCounter);
        ingredientsIDCounter++;
        LinearLayout.LayoutParams spinnerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        spinnerParams.setMargins(0, 0, 0, 16); // Left, top, right, bottom margins
        unitSpinner.setLayoutParams(spinnerParams);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.units_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unitSpinner.setAdapter(adapter);

        // Set the text color for dropdown items
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                ((TextView) selectedItemView).setTextColor(Color.BLACK);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });
        ingredientsLayout.addView(unitSpinner);

        // Button to remove the ingredient field
        Button removeButton = new Button(this);
        removeButton.setText("Remove Ingredient #" + (ingredientCounter-1));
        removeButton.setBackgroundResource(R.drawable.red_button_bg);
        removeButton.setTextColor(Color.BLACK);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        buttonParams.setMargins(0, 0, 0, 16); // Left, top, right, bottom margins
        removeButton.setLayoutParams(buttonParams);
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Remove the views associated with this ingredient field
                ingredientsLayout.removeView(label);
                ingredientsLayout.removeView(ingredientNameEditText);
                ingredientsLayout.removeView(ingredientQuantityEditText);
                ingredientsLayout.removeView(unitSpinner);
                ingredientsLayout.removeView(removeButton);
                // Remove the IDs from the list
                ingredientEditTextIds.remove(Integer.valueOf(ingredientNameEditText.getId()));
                ingredientEditTextIds.remove(Integer.valueOf(ingredientQuantityEditText.getId()));
                ingredientEditTextIds.remove(Integer.valueOf(unitSpinner.getId()));
                // Update the ingredient counter
                ingredientCounter--;
            }
        });
        ingredientsLayout.addView(removeButton);
    }





    private void addStepField() {
        // TextView for step label
        TextView label = new TextView(this);
        // Set margin for the label
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        labelParams.setMargins(0, 150, 0, 16); // Left, top, right, bottom margins
        label.setLayoutParams(labelParams);
        label.setText("Step #" + stepCounter++);
        label.setTextColor(getResources().getColor(android.R.color.black));
        label.setTextSize(20); // Adjust font size as needed
        label.setTypeface(null, Typeface.BOLD);
        stepsLayout.addView(label);

        // EditText for step description
        EditText stepEditText = new EditText(this);
        stepEditText.setId(stepsIDCounter);
        LinearLayout.LayoutParams stepEditTextParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        stepEditTextParams.setMargins(0, 0, 0, 16); // Left, top, right, bottom margins
        stepEditText.setLayoutParams(stepEditTextParams);
        stepEditText.setHint("Enter step");
        stepEditText.setHintTextColor(-16777216);
        stepEditText.setTextColor(-16777216);
        stepsLayout.addView(stepEditText);

        // Add the ID of the step EditText to the list
        stepEditTextIds.add(stepsIDCounter);
        stepsIDCounter++;

        // Button to remove the step field
        Button removeButton = new Button(this);
        removeButton.setText("Remove Step #" + (stepCounter - 1));
        removeButton.setBackgroundResource(R.drawable.red_button_bg);
        removeButton.setTextColor(Color.BLACK);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        buttonParams.setMargins(0, 0, 0, 16); // Left, top, right, bottom margins
        removeButton.setLayoutParams(buttonParams);
        removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Remove the views associated with this step field
                stepsLayout.removeView(label);
                stepsLayout.removeView(stepEditText);
                stepsLayout.removeView(removeButton);
                // Remove the ID from the list
                stepEditTextIds.remove(Integer.valueOf(stepEditText.getId()));
                // Update the step counter
                stepCounter--;
            }
        });
        stepsLayout.addView(removeButton);
    }

    private void findViews() {
        headlineTextView = findViewById(R.id.headlineTextView);
        recipeNameEditText = findViewById(R.id.recipeNameEditText);
        tagsChipGroup = findViewById(R.id.tagsChipGroup);
        ingredientsLayout = findViewById(R.id.ingredientsLayout);
        addIngredientButton = findViewById(R.id.addIngredientButton);
        stepsLayout = findViewById(R.id.stepsLayout);
        addStepButton = findViewById(R.id.addStepButton);
        uploadImageButton = findViewById(R.id.uploadImageButton);
        uploadRecipeButton = findViewById(R.id.uploadRecipeButton);
        selectedImageView = findViewById(R.id.selectedImageView);
        cookingTimeSeekBar = findViewById(R.id.cookingTimeSeekBar);
        selectedCookingTimeText = findViewById(R.id.selectedCookingTimeText);
        menuIcon = findViewById(R.id.menuIcon);
        textAcronyms = findViewById(R.id.textAcronyms);
    }


    private void populateTags() {
        String[] tags = {"Vegetarian", "Burger", "Sushi", "Pizza", "Pasta", "Salad", "Soup", "Dessert", "Seafood", "Chicken", "Beef", "Vegan", "Gluten-free", "Low carb", "Quick"};
        for (String tag : tags) {
            Chip chip = new Chip(this);
            chip.setId(chipsIDCounter);
            chipsIds.add(chipsIDCounter);
            chipsIDCounter++;
            chip.setText(tag);
            chip.setTextColor(-16777216);
            chip.setCheckable(true);
            chip.setChipBackgroundColorResource(R.color.lightgrey);
            chip.setOnCheckedChangeListener((compoundButton, isChecked) -> {
                if (isChecked) {
                    chip.setChipBackgroundColorResource(R.color.lightRed); // Set background color for checked state
                } else {
                    chip.setChipBackgroundColorResource(R.color.lightgrey); // Set background color for unchecked state
                }
            });
            tagsChipGroup.addView(chip);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed(); // Call the superclass method for default back button behavior (optional)
        Intent mainIntent = new Intent(UploadRecipe.this, MainFeed.class);
        mainIntent.putExtra("email", userEmail);
        mainIntent.putExtra("textAcronyms", textAcronyms.getText());
        startActivity(mainIntent);
        finish();
    }


}
