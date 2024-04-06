package com.example.finalproject.UI;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.finalproject.Models.User;
import com.example.finalproject.R;
import com.example.finalproject.Utils.DatabaseUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class RecipePageActivity extends AppCompatActivity {

    private TextView recipeHeadline, tags, ingredients, steps, cookingTime , textAcronyms;
    private ImageView recipeImage, updateRecipeIcon;

    private String userEmail;
    private String createdByUID;
    private ImageView cookingTimeImg;
    private ImageView menuIcon;
    private String recipeName;

    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recipe_page_activity);
        findViews();
        currentUser = DatabaseUtils.getInstance().getCurrentUser();

        // Get intent extras from previous activity
        Intent intent = getIntent();
        if (intent != null) {
            recipeName = intent.getStringExtra("recipeName");
            if (recipeName != null) {
                fetchRecipeDetails(recipeName);
                userEmail = currentUser.getEmail();
                fetchUserInformation(userEmail);
            }
        }

        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenuOptions();
            }
        });

        updateRecipeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent uploadRecipeIntent = new Intent(RecipePageActivity.this, UpdateRecipeActivity.class);
                uploadRecipeIntent.putExtra("recipeName", recipeName);
                uploadRecipeIntent.putExtra("textAcronyms", textAcronyms.getText());
                startActivity(uploadRecipeIntent);
                finish();
            }
        });

        // Set OnClickListener for Acronyms TextView
        textAcronyms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLogoutDialog();
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
        startActivity(new Intent(RecipePageActivity.this, LoginActivity.class));
        finish(); // Close this activity
    }

    private void showMenuOptions() {
        PopupMenu popupMenu = new PopupMenu(this, menuIcon);
        popupMenu.getMenuInflater().inflate(R.menu.personal_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.mainFeed) {
                    // Handle upload recipe action
                    Intent mainIntent = new Intent(RecipePageActivity.this, MainFeed.class);
                    mainIntent.putExtra("email", userEmail);
                    mainIntent.putExtra("textAcronyms", textAcronyms.getText());
                    startActivity(mainIntent);
                    finish();
                    return true;
                } else if (id == R.id.uploadARecipe) {
                    Intent uploadRecipeIntent = new Intent(RecipePageActivity.this, UploadRecipe.class);
                    uploadRecipeIntent.putExtra("email", userEmail);
                    uploadRecipeIntent.putExtra("textAcronyms", textAcronyms.getText());
                    startActivity(uploadRecipeIntent);
                    finish();
                    return true;
                }
                return false;
            }
        });
        popupMenu.show();
    }


    private void fetchUserInformation(String email) {
        DatabaseUtils.getInstance().getDatabaseReference().child("Users").orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        User user = userSnapshot.getValue(User.class);
                        textAcronyms.setText(user.getFullName());
                        if (user != null) {
                            String acronyms = generateAcronyms(user.getFullName());
                            textAcronyms.setText(acronyms);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
            }
        });
    }

    private String generateAcronyms(String fullName) {
        if (fullName == null || fullName.isEmpty()) {
            return "";
        }
        // Split the full name by space or dot (if dot is used in the name)
        String[] nameParts = fullName.split("[\\s\\.]");
        StringBuilder acronymsBuilder = new StringBuilder();
        for (String part : nameParts) {
            if (!part.isEmpty()) {
                acronymsBuilder.append(Character.toUpperCase(part.charAt(0))); // Convert the first character to uppercase
            }
        }
        return acronymsBuilder.toString();
    }


    private void findViews() {
        // Initialize TextViews
        recipeHeadline = findViewById(R.id.recipeHeadline);
        tags = findViewById(R.id.tags);
        ingredients = findViewById(R.id.ingredients);
        steps = findViewById(R.id.steps);
        cookingTime = findViewById(R.id.cookingTime);

        // Initialize ImageView
        recipeImage = findViewById(R.id.recipeImage);
        cookingTimeImg = findViewById(R.id.cookingTimeImg);
        menuIcon = findViewById(R.id.menuIcon);

        textAcronyms = findViewById(R.id.textAcronyms);
        updateRecipeIcon = findViewById(R.id.updateRecipeIcon);
    }

    private void fetchRecipeDetails(String recipeName) {
        DatabaseUtils.getInstance().getDatabaseReference().child("Recipes").child(recipeName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Fetch data from dataSnapshot and assign it to variables
                    ArrayList<String> recipeTags = (ArrayList<String>) dataSnapshot.child("tags").getValue();
                    ArrayList<String> recipeSteps = (ArrayList<String>) dataSnapshot.child("steps").getValue();
                    String recipeCookingTime = dataSnapshot.child("cookingTimeMinutes").getValue() + "";
                    String imageUrl = dataSnapshot.child("imageUrl").getValue(String.class);
                    createdByUID = dataSnapshot.child("createdBy").getValue(String.class);
                    List<String> ingredientsList = new ArrayList<>();
                    for (DataSnapshot ingredientSnapshot : dataSnapshot.child("ingredients").getChildren()) {
                        String name = ingredientSnapshot.child("name").getValue(String.class);
                        int quantity = ingredientSnapshot.child("quantity").getValue(Integer.class);
                        String unit = ingredientSnapshot.child("unit").getValue(String.class);
                        String ingredient = name + ": " + quantity + " " + unit;
                        ingredientsList.add(ingredient);
                    }

                    if (createdByUID != null && verifySelfCreatedRecipe()) {
                        updateRecipeIcon.setVisibility(View.VISIBLE);
                    }

                    // Set the retrieved data to TextViews and ImageView
                    recipeHeadline.setText(recipeName);
                    tags.setText(convertTagsToString(recipeTags));
                    steps.setText(convertTagsToString(recipeSteps));
                    cookingTime.setText(recipeCookingTime);
                    // Set ingredients as a single string
                    ingredients.setText(convertIngredientsListToString(ingredientsList));

                    // Load the recipe image using the provided URL
                    Glide.with(RecipePageActivity.this)
                            .load(imageUrl)
                            .placeholder(R.drawable.carrot)  // Placeholder image while loading
                            .error(R.drawable.carrot)  // Error image if unable to load
                            .into(recipeImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle potential errors
            }
        });
    }

    private String convertIngredientsListToString(DataSnapshot ingredientsSnapshot) {
        StringBuilder ingredientsBuilder = new StringBuilder();
        for (DataSnapshot ingredient : ingredientsSnapshot.getChildren()) {
            String name = ingredient.child("name").getValue(String.class);
            int quantity = ingredient.child("quantity").getValue(Integer.class);
            String unit = ingredient.child("unit").getValue(String.class);
            ingredientsBuilder.append(name).append(": ").append(quantity).append(" ").append(unit).append("\n");
        }
        return ingredientsBuilder.toString();
    }

    private String convertIngredientsListToString(List<String> ingredientsList) {
        StringBuilder ingredientsBuilder = new StringBuilder();
        int i = 1;
        for (String ingredient : ingredientsList) {
            ingredientsBuilder.append("#" + i + " : " + ingredient).append("\n");
            i++;
        }
        return ingredientsBuilder.toString();
    }

    public static String convertTagsToString(ArrayList<String> tags) {
        StringBuilder stringBuilder = new StringBuilder();
        int i = 1;
        for (String tag : tags) {
            stringBuilder.append("#" + i + " - " + tag).append("\n");  // Add each tag followed by a comma and space
            i++;
        }
        return stringBuilder.toString();
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

    private boolean verifySelfCreatedRecipe() {
        String currentUserId = getCurrentUserId();
        Log.d("RecipePageActivity", "Going to compare createdByUID: " + createdByUID + ", to currentUserId: " + currentUserId);
        return createdByUID.equals(currentUserId); // Make sure createdByUID is not null here
    }
}
