package com.example.finalproject.UI;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.finalproject.Models.User;
import com.example.finalproject.R;
import com.example.finalproject.Utils.DatabaseUtils;
import com.example.finalproject.Utils.MyUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * The RecipePageActivity class represents the activity where users can view details of a recipe.
 * It displays information such as recipe name, cooking time, ingredients, steps, and allows users
 * to perform actions like updating or deleting the recipe based on their permissions.
 * The activity includes functionality for fetching recipe details from Firebase Realtime Database,
 * displaying the details in a user-friendly manner, and providing options for user interactions
 * such as updating or deleting the recipe.
 */
public class RecipePageActivity extends AppCompatActivity {

    private TextView recipeHeadline, cookingTime , textAcronyms, tagsContent;
    private ImageView recipeImage, updateRecipeIcon, removeRecipeIcon, youtubeImage ,googleImage;
    private String userEmail;
    private String createdByUID;
    private ImageView menuIcon;
    private String recipeName;
    private MyUtils myUtils;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recipe_page_activity);
        findViews();
        currentUser = DatabaseUtils.getInstance().getCurrentUser();
        myUtils = MyUtils.getInstance(this);
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

        removeRecipeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDeleteConfirmationDialog(recipeName);
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

        youtubeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchOnYouTube(recipeName);
            }
        });

        googleImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchOnGoogle(recipeName);
            }
        });

        // Set OnClickListener for Acronyms TextView
        textAcronyms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent uploadRecipeIntent = new Intent(RecipePageActivity.this, PersonalDetails.class);
                uploadRecipeIntent.putExtra("email", userEmail);
                uploadRecipeIntent.putExtra("textAcronyms", textAcronyms.getText());
                startActivity(uploadRecipeIntent);
                finish();
            }
        });
    }

    private void searchOnGoogle(String recipeName) {
        String query = "How to make " + recipeName +"?";
        String searchQuery = "https://www.google.com/search?q=" + Uri.encode(query);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(searchQuery));
        startActivity(intent);
    }

    // function to search on youtube a similar recipe in case the steps are not fully understood.
    private void searchOnYouTube(String recipeName) {
        String searchQuery = "How to make " + recipeName + "?";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/results?search_query=" + Uri.encode(searchQuery)));
        startActivity(intent);
    }


    /**
     * showLogoutDialog method displays a confirmation dialog for user logout.
     * It prompts the user to confirm their decision to logout from the application.
     */
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

    /**
     * logoutUser method performs the logout action for the current user.
     * It signs out the user from the application and navigates back to the login screen.
     */
    private void logoutUser() {
        // Perform logout action
        DatabaseUtils.getInstance().signOutUser();
        startActivity(new Intent(RecipePageActivity.this, LoginActivity.class));
        finish(); // Close this activity
    }

    /**
     * showMenuOptions method displays a popup menu with options for the user.
     * It shows options like navigating to the main feed or uploading a new recipe.
     */
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
                } else if (id == R.id.logOutUser){
                    showLogoutDialog();
                    return true;
                }
                return false;
            }
        });
        popupMenu.show();
    }


    // fetch user information to set Acrobyms
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
        cookingTime = findViewById(R.id.cookingTime);

        // Initialize ImageView
        recipeImage = findViewById(R.id.recipeImage);
        menuIcon = findViewById(R.id.menuIcon);

        textAcronyms = findViewById(R.id.textAcronyms);
        updateRecipeIcon = findViewById(R.id.updateRecipeIcon);
        removeRecipeIcon = findViewById(R.id.removeRecipeIcon);

        tagsContent = findViewById(R.id.tagsContent);
        youtubeImage = findViewById(R.id.youtubeImage);
        googleImage = findViewById(R.id.googleImage);
    }

    // This function fetch recipe details by recipe name and updates UI accordingly
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
                        removeRecipeIcon.setVisibility(View.VISIBLE);
                    }

                    // Set the retrieved data to TextViews and ImageView
                    recipeHeadline.setText(recipeName);
                    updateTagsTextview(recipeTags);
                    updateStepsTable(recipeSteps);
                    cookingTime.setText(recipeCookingTime + " Minutes");
                    // Set ingredients as a single string
                    updateIngredientsTable(ingredientsList);
                    // Load the recipe image using the provided URL
                    Glide.with(RecipePageActivity.this)
                            .load(imageUrl)
                            .placeholder(R.drawable.imagenotfound)
                            .error(R.drawable.imagenotfound)
                            .into(recipeImage);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle potential errors
            }
        });
    }

    // This function shows confirmation dialog of deletion of recipe from firebase database
    private void showDeleteConfirmationDialog(String recipeName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Deletion");
        builder.setMessage("Are you sure you want to delete the recipe '" + recipeName + "'?");
        builder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // User confirmed deletion, call the function to delete the recipe
                deleteRecipe(recipeName);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // User canceled deletion, do nothing
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    // This function delete a recipe from the database
    public void deleteRecipe(String recipeName){
        // Remove the recipe from the database
        DatabaseUtils.getInstance().getDatabaseReference().child("Recipes").child(recipeName).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                // Recipe successfully deleted
                Log.d("DatabaseUtils", "Recipe: " + recipeName +" deleted successfully!");
                myUtils.showToast("Recipe removed successfully!");
                Intent uploadRecipeIntent = new Intent(RecipePageActivity.this, MainFeed.class);
                uploadRecipeIntent.putExtra("textAcronyms", textAcronyms.getText());
                startActivity(uploadRecipeIntent);
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Failed to delete the recipe
                Log.e("DatabaseUtils", "Error deleting recipe: " + e.getMessage());
            }
        });

    }


    // This function updates the text view of tags with data
    private void updateTagsTextview(ArrayList<String> tags) {
        StringBuilder st = new StringBuilder();
        for (String tag : tags) {
            st.append("#" + tag + ", ");
        }
        if (st.length() > 0) {
            st.deleteCharAt(st.length() - 2); // Remove the last comma and space
        }
        tagsContent.setText(st);
    }

    // This function updates the table of steps with data
    private void updateStepsTable(ArrayList<String> steps) {
        TableLayout stepsTableLayout = findViewById(R.id.stepsTableLayout);
        int id = 1;
        for (String step : steps) {
            // Split the step into substrings of 30 characters each
            List<String> subSteps = splitStep(step, 30);

            for (int i = 0; i < subSteps.size(); i++) {
                TableRow row = new TableRow(this);
                TextView idTextView = new TextView(this);
                idTextView.setText(i == 0 ? String.valueOf(id) : ""); // Show ID only for the first line
                idTextView.setTextColor(Color.BLACK);
                idTextView.setPadding(8, 0, 8, 0);
                idTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                row.addView(idTextView);

                TextView textView = new TextView(this);
                textView.setText(subSteps.get(i)); // Set the substring
                textView.setTextColor(Color.BLACK);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                row.addView(textView);
                stepsTableLayout.addView(row);
            }

            id++;
        }
    }

    private List<String> splitStep(String step, int chunkSize) {
        List<String> subSteps = new ArrayList<>();
        if (step.length() <= chunkSize) {
            subSteps.add(step);
        } else {
            int start = 0;
            int end;
            while (start < step.length()) {
                end = Math.min(start + chunkSize, step.length());
                String subStep = step.substring(start, end);
                if(end < step.length())
                    subSteps.add(subStep + "-");
                else
                    subSteps.add(subStep);
                start += chunkSize;
            }
        }
        return subSteps;
    }



    // This function updates the ingrredients table with data
    private void updateIngredientsTable(List<String> ingredientsList) {
        TableLayout ingredientsTableLayout = findViewById(R.id.ingredientsTableLayout);
        int id = 1;
        for (String ingredient : ingredientsList) {
            // Split the ingredient string into name, quantity, and unit
            String[] parts = ingredient.split(": ");
            if (parts.length == 2) {
                String name = parts[0];
                String quantityAndUnit = parts[1];

                // Split quantity and unit
                String[] quantityUnit = quantityAndUnit.split(" ");
                if (quantityUnit.length == 2) {
                    String quantity = quantityUnit[0];
                    String unit = quantityUnit[1];

                    TableRow row = new TableRow(this);

                    // Name column
                    TextView nameTextView = new TextView(this);
                    nameTextView.setText(name);
                    nameTextView.setTextColor(Color.BLACK);
                    nameTextView.setPadding(8, 0, 8, 0);
                    nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                    nameTextView.setSingleLine(true); // Set single line
                    nameTextView.setEllipsize(TextUtils.TruncateAt.END); // Set ellipsize
                    row.addView(nameTextView);

                    // Quantity column
                    TextView quantityTextView = new TextView(this);
                    quantityTextView.setText(quantity);
                    quantityTextView.setTextColor(Color.BLACK);
                    quantityTextView.setPadding(20, 0, 4, 0);
                    quantityTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                    row.addView(quantityTextView);

                    // Unit column
                    TextView unitTextView = new TextView(this);
                    unitTextView.setText(unit);
                    unitTextView.setTextColor(Color.BLACK);
                    unitTextView.setPadding(4, 0, 0, 0);
                    unitTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
                    row.addView(unitTextView);
                    ingredientsTableLayout.addView(row);
                    id++;
                }
            }
        }
    }

    // This function verify if the logged in user is the one the created this specific recipe
    private boolean verifySelfCreatedRecipe() {
        String currentUserId = DatabaseUtils.getInstance().getCurrentUser().getUid(); // logged in user ID
        return createdByUID.equals(currentUserId); // Make sure createdByUID is not null here
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed(); // Call the superclass method for default back button behavior (optional)
        Intent mainIntent = new Intent(RecipePageActivity.this, MainFeed.class);
        mainIntent.putExtra("email", userEmail);
        mainIntent.putExtra("textAcronyms", textAcronyms.getText());
        startActivity(mainIntent);
        finish();
    }
}
