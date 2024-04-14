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
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.finalproject.Models.Ingredient;
import com.example.finalproject.Models.Recipe;
import com.example.finalproject.R;
import com.example.finalproject.Utils.DatabaseUtils;
import com.example.finalproject.Utils.MyUtils;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UpdateRecipeActivity extends AppCompatActivity {

    private ImageView menuIcon;

    private String existingImageUrl;

    private TextView textAcronyms;
    private TextView recipeNameTextView;
    private ChipGroup tagsChipGroup;
    private LinearLayout ingredientsLayout;
    private Button addIngredientButton;
    private LinearLayout stepsLayout;
    private Button addStepButton;
    private Button uploadImageButton;
    private Button updateRecipe;
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


    private String recipeName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.update_recipe_activity);
        findViews();

        recipeName = getIntent().getStringExtra("recipeName");
        textAcronyms.setText(getIntent().getStringExtra("textAcronyms"));
        myUtils = MyUtils.getInstance(this);
        selectedImageView.setVisibility(View.VISIBLE);
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
                Intent uploadRecipeIntent = new Intent(UpdateRecipeActivity.this, PersonalDetails.class);
                uploadRecipeIntent.putExtra("email", userEmail);
                uploadRecipeIntent.putExtra("textAcronyms", textAcronyms.getText());
                startActivity(uploadRecipeIntent);
                finish();
            }
        });

        fetchRecipeDetails(recipeName);
    }

    private void fetchRecipeDetails(String recipeName) {
        DatabaseUtils.getInstance().getDatabaseReference().child("Recipes").child(recipeName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Fetch data from dataSnapshot and assign it to variables
                    ArrayList<String> recipeTags = (ArrayList<String>) dataSnapshot.child("tags").getValue();
                    ArrayList<String> recipeSteps = (ArrayList<String>) dataSnapshot.child("steps").getValue();
                    int recipeCookingTime = dataSnapshot.child("cookingTimeMinutes").getValue(Integer.class);
                    String imageUrl = dataSnapshot.child("imageUrl").getValue(String.class);
                    existingImageUrl = dataSnapshot.child("imageUrl").getValue(String.class);
                    ArrayList<HashMap<String, Object>> ingredientsList = (ArrayList<HashMap<String, Object>>) dataSnapshot.child("ingredients").getValue();


                    // update values:
                    cookingTimeSeekBar.setProgress(recipeCookingTime);
                    recipeNameTextView.setText("Recipe name: " + recipeName);
                    for (String tag : recipeTags) {
                        for (int chipId : chipsIds) {
                            Chip chip = findViewById(chipId);
                            if (chip.getText().toString().equals(tag)) {
                                chip.setChecked(true);
                            }
                        }
                    }

                    for (HashMap<String, Object> ingredientMap : ingredientsList) {
                        String name = (String) ingredientMap.get("name");
                        String quantity = (String) ingredientMap.get("quantity").toString();
                        String unit = (String) ingredientMap.get("unit");
                        addIngredientField(name, String.valueOf(quantity), unit);
                    }
                    for (String step : recipeSteps) {
                        addStepField(step);
                    }
                    // Load the recipe image using the provided URL
                    Glide.with(UpdateRecipeActivity.this)
                            .load(imageUrl)
                            .placeholder(R.drawable.carrot)  // Placeholder image while loading
                            .error(R.drawable.carrot)  // Error image if unable to load
                            .into(selectedImageView);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle potential errors
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
        startActivity(new Intent(UpdateRecipeActivity.this, LoginActivity.class));
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
                    Intent mainIntent = new Intent(UpdateRecipeActivity.this, MainFeed.class);
                    mainIntent.putExtra("email", userEmail);
                    mainIntent.putExtra("textAcronyms", textAcronyms.getText());
                    startActivity(mainIntent);
                    finish();
                    return true;
                } else if (id == R.id.logOutUser) {
                    showLogoutDialog();
                    return true;
                }
                else if (id == R.id.uploadARecipe) {
                    Intent uploadIntent = new Intent(UpdateRecipeActivity.this, UploadRecipe.class);
                    uploadIntent.putExtra("email", userEmail);
                    uploadIntent.putExtra("textAcronyms", textAcronyms.getText());
                    startActivity(uploadIntent);
                    finish();
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
        updateRecipe.setOnClickListener(v -> onSubmitClicked());
    }


    private void onSubmitClicked() {
        // Check if all required inputs are provided
        if (!areTagsSelected() || !areIngredientsAdded()
                || !areStepsAdded() || !isImageUploaded() || !isCookingTimeSelected()) {
            // Show a toast indicating missing inputs
            myUtils.showToast("Please fill in all required fields.");
        } else {
            List<String> steps = getSteps();
            List<Ingredient> ingredients = getIngredients();

            // Validate that steps and ingredients are not empty
            if (validateStepsNotEmpty(steps) && validateIngredientsNotEmpty(ingredients)) {
                // All inputs are provided and non-empty, update the recipe in the Firebase database
                List<String> selectedTags = getSelectedTags();
                int cookingTime = cookingTimeSeekBar.getProgress();

                // Update the image if it has been changed
                Bitmap imageBitmap = getImage();
                if (imageBitmap != null) {
                    updateRecipeImage(imageBitmap, recipeName, selectedTags, steps, ingredients, cookingTime);
                } else {
                    updateRecipeData(recipeName, selectedTags, steps, ingredients, cookingTime, existingImageUrl);
                }
            }
        }
    }

    private String getExistingImageUrl(String recipeName) {
        // Fetch the existing image URL from Firebase Database based on the recipe name
         DatabaseUtils.getInstance().getDatabaseReference().child("Recipes").child(recipeName).addListenerForSingleValueEvent(new ValueEventListener() {
             @Override
             public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                 if (dataSnapshot.exists()) {
                     String existingImageUrl = dataSnapshot.child("imageUrl").getValue(String.class);
                 }
             }

             @Override
             public void onCancelled(@NonNull DatabaseError databaseError) {
                 // Handle potential errors
             }
         });
        // Return the existing image URL
        return ""; // Placeholder, replace with actual implementation
    }

    private void updateRecipeImage(Bitmap imageBitmap, String recipeName, List<String> selectedTags,
                                   List<String> steps, List<Ingredient> ingredients, int cookingTime) {
        // Get the Firebase Storage reference
        String imageFileName = "recipe_" + recipeName + ".png";
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("images/" + imageFileName);

        // Convert the image bitmap to a byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageData = baos.toByteArray();

        // Upload the image to Firebase Storage
        UploadTask uploadTask = storageRef.putBytes(imageData);
        uploadTask.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Image uploaded successfully, get the download URL
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    // Update the image URL in the recipe data
                    String imageUrl = uri.toString();
                    updateRecipeData(recipeName, selectedTags, steps, ingredients, cookingTime, imageUrl);
                }).addOnFailureListener(e -> {
                    // Handle any errors that occurred while getting the download URL
                    myUtils.showToast("Failed to get image URL: " + e.getMessage());
                });
            } else {
                // Handle image upload failure
                myUtils.showToast("Failed to upload image: " + task.getException().getMessage());
            }
        });
    }

    private void updateRecipeData(String recipeName, List<String> selectedTags,
                                  List<String> steps, List<Ingredient> ingredients, int cookingTime, String imageUrl) {
        // Create a map to hold the updated recipe data
        HashMap<String, Object> updatedRecipeData = new HashMap<>();
        updatedRecipeData.put("tags", selectedTags);
        updatedRecipeData.put("ingredients", ingredients);
        updatedRecipeData.put("steps", steps);
        updatedRecipeData.put("cookingTimeMinutes", cookingTime);
        updatedRecipeData.put("imageUrl", imageUrl); // Update the image URL

        // Update the recipe data in the Firebase database
        DatabaseUtils.getInstance().getDatabaseReference().child("Recipes").child(recipeName).updateChildren(updatedRecipeData)
                .addOnSuccessListener(aVoid -> {
                    // Recipe data updated successfully
                    myUtils.showToast("Recipe updated successfully!");
                    Intent intent = new Intent(UpdateRecipeActivity.this, MainFeed.class);
                    startActivity(intent);
                    finish(); // Finish the activity after updating
                })
                .addOnFailureListener(e -> {
                    // Handle any errors that occurred during database update
                    myUtils.showToast("Failed to update recipe: " + e.getMessage());
                });
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
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            return currentUser.getUid();
        } else {
            // Return a default user ID or handle the situation as needed
            return "default_user_id";
        }
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
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        labelParams.setMargins(0, 150, 0, 16); // Set bottom margin to 16dp
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
        ingredientNameEditText.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        ingredientNameEditText.setHint("Enter ingredient name");
        ingredientNameEditText.setHintTextColor(getResources().getColor(android.R.color.black));
        ingredientNameEditText.setTextColor(-16777216);
        ingredientsLayout.addView(ingredientNameEditText);

        // EditText for ingredient quantity
        EditText ingredientQuantityEditText = new EditText(this);
        ingredientQuantityEditText.setId(ingredientsIDCounter);
        ingredientEditTextIds.add(ingredientsIDCounter);
        ingredientsIDCounter++;
        ingredientQuantityEditText.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
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

        // Set text color to black
        removeButton.setTextColor(Color.BLACK);
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

    private void addIngredientField(String name, String quantity, String unit) {
        // TextView for ingredient label
        TextView label = new TextView(this);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        labelParams.setMargins(0, 150, 0, 16); // Set bottom margin to 16dp
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
        ingredientNameEditText.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        ingredientNameEditText.setHint("Enter ingredient name");
        ingredientNameEditText.setHintTextColor(getResources().getColor(android.R.color.black));
        ingredientNameEditText.setTextColor(-16777216);
        ingredientNameEditText.setText(name); // Set the name provided
        ingredientsLayout.addView(ingredientNameEditText);

        // EditText for ingredient quantity
        EditText ingredientQuantityEditText = new EditText(this);
        ingredientQuantityEditText.setId(ingredientsIDCounter);
        ingredientEditTextIds.add(ingredientsIDCounter);
        ingredientsIDCounter++;
        ingredientQuantityEditText.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        ingredientQuantityEditText.setHint("Enter quantity");
        ingredientQuantityEditText.setTextColor(-16777216);
        ingredientQuantityEditText.setHintTextColor(getResources().getColor(android.R.color.black));
        ingredientQuantityEditText.setText(quantity); // Set the quantity provided
        ingredientsLayout.addView(ingredientQuantityEditText);

        // Spinner for selecting unit
        Spinner unitSpinner = new Spinner(this);
        String unitSpinnerId = "UnitSpinner" + ingredientCounter;
        unitSpinner.setId(ingredientsIDCounter);
        ingredientEditTextIds.add(ingredientsIDCounter);
        ingredientsIDCounter++;
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
        unitSpinner.setSelection(adapter.getPosition(unit)); // Set the unit provided
        ingredientsLayout.addView(unitSpinner);

        // Button to remove the ingredient field
        Button removeButton = new Button(this);
        removeButton.setText("Remove Ingredient #" + (ingredientCounter-1));
        removeButton.setBackgroundResource(R.drawable.red_button_bg);

        // Set text color to black
        removeButton.setTextColor(Color.BLACK);
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
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        labelParams.setMargins(0, 150, 0, 16); // Set bottom margin to 16dp
        label.setLayoutParams(labelParams);
        label.setText("Step #" + stepCounter++);
        label.setTextColor(getResources().getColor(android.R.color.black));
        label.setTextSize(20); // Adjust font size as needed
        label.setTypeface(null, Typeface.BOLD);
        stepsLayout.addView(label);

        // EditText for step description
        EditText stepEditText = new EditText(this);
        String stepEditTextId = "StepNum" + stepCounter;
        stepEditText.setId(stepsIDCounter);
        stepEditText.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        stepEditText.setHint("Enter step");
        stepEditText.setHintTextColor(-16777216);
        stepEditText.setTextColor(-16777216);
        stepsLayout.addView(stepEditText);

        // Add the ID of the step EditText to the list
        stepEditTextIds.add(stepsIDCounter);
        stepsIDCounter++;

        // Button to remove the step field
        Button removeButton = new Button(this);
        removeButton.setText("Remove Step #" + (stepCounter-1));
        removeButton.setBackgroundResource(R.drawable.red_button_bg);

        // Set text color to black
        removeButton.setTextColor(Color.BLACK);
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

    private void addStepField(String existingStep) {
        // TextView for step label
        TextView label = new TextView(this);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        labelParams.setMargins(0, 150, 0, 16); // Set bottom margin to 16dp
        label.setLayoutParams(labelParams);
        label.setText("Step #" + stepCounter++);
        label.setTextColor(getResources().getColor(android.R.color.black));
        label.setTextSize(20); // Adjust font size as needed
        label.setTypeface(null, Typeface.BOLD);
        stepsLayout.addView(label);

        // EditText for step description
        EditText stepEditText = new EditText(this);
        String stepEditTextId = "StepNum" + stepCounter;
        stepEditText.setId(stepsIDCounter);
        stepEditText.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        stepEditText.setHint(existingStep);
        stepEditText.setHintTextColor(-16777216);
        stepEditText.setTextColor(-16777216);
        stepEditText.setText(existingStep);
        stepsLayout.addView(stepEditText);

        // Add the ID of the step EditText to the list
        stepEditTextIds.add(stepsIDCounter);
        stepsIDCounter++;

        // Button to remove the step field
        Button removeButton = new Button(this);
        removeButton.setText("Remove Step #" + (stepCounter-1));
        removeButton.setBackgroundResource(R.drawable.red_button_bg);

        // Set text color to black
        removeButton.setTextColor(Color.BLACK);
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
        recipeNameTextView = findViewById(R.id.recipeNameTextView);
        tagsChipGroup = findViewById(R.id.tagsChipGroup);
        ingredientsLayout = findViewById(R.id.ingredientsLayout);
        addIngredientButton = findViewById(R.id.addIngredientButton);
        stepsLayout = findViewById(R.id.stepsLayout);
        addStepButton = findViewById(R.id.addStepButton);
        uploadImageButton = findViewById(R.id.uploadImageButton);
        updateRecipe = findViewById(R.id.updateRecipeButton);
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
        Intent mainIntent = new Intent(UpdateRecipeActivity.this, MainFeed.class);
        mainIntent.putExtra("email", userEmail);
        mainIntent.putExtra("textAcronyms", textAcronyms.getText());
        startActivity(mainIntent);
        finish();
    }


}
