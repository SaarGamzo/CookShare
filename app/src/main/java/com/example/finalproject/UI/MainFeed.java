package com.example.finalproject.UI;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.Models.Recipe;
import com.example.finalproject.Models.User;
import com.example.finalproject.R;
import com.example.finalproject.Utils.DatabaseUtils;
import com.example.finalproject.Utils.RecipeAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainFeed extends AppCompatActivity implements RecipeAdapter.RecipeClickListener {

    private ImageView menuIcon;
    private TextView textAcronyms;
    private String userEmail;
    private DatabaseReference likedRecipesRef;
    private RecyclerView recyclerViewRecipes;
    private RecipeAdapter recipeAdapter;
    private Map<String, Recipe> recipeMap; // Use map to hold all recipes
    private EditText searchField;
    private Button searchButton;
    private Button clearButton;

    private String[] tags = {"Vegetarian", "Burger", "Sushi", "Pizza", "Pasta", "Salad", "Soup", "Dessert", "Seafood", "Chicken", "Beef", "Vegan", "Gluten-free", "Low carb", "Quick"};
    private LinearLayout tagsLayout;
    private Map<String, Boolean> likedRecipeNames; // List to store liked recipe names

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_feed_activity);
        getViews();
        // Initialize RecyclerView and recipeList
        recipeMap = new HashMap<>();
        likedRecipeNames = new HashMap<>();

        // Get user email from intent extras
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey("email")) {
            userEmail = extras.getString("email");
            // Fetch user information from Firebase Database
            userEmail = userEmail.replace("!", ".");
            fetchUserInformation(userEmail);
            userEmail = userEmail.replace(".", "!");
            fetchUserLikedRecipes(userEmail);
        }

        // Initialize Firebase database reference for liked recipes
        likedRecipesRef = DatabaseUtils.getInstance().getDatabaseReference().child("Users").child(userEmail.replace(".", "!")).child("liked_recipes");
        fetchAllRecipes();
        recipeAdapter = new RecipeAdapter(recipeMap, likedRecipeNames, likedRecipesRef);
        recipeAdapter.setRecipeClickListener(this);

        // Set layout manager and adapter for RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewRecipes.setLayoutManager(layoutManager);
        recyclerViewRecipes.setAdapter(recipeAdapter);

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
                Intent uploadRecipeIntent = new Intent(MainFeed.this, PersonalDetails.class);
                uploadRecipeIntent.putExtra("email", userEmail);
                uploadRecipeIntent.putExtra("textAcronyms", textAcronyms.getText());
                startActivity(uploadRecipeIntent);
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                performSearch();
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearSearch();
            }
        });
    }

    private void fetchAllRecipes() {
        DatabaseReference recipesRef = FirebaseDatabase.getInstance().getReference().child("Recipes");
        recipesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot recipeSnapshot : snapshot.getChildren()) {
                    String recipeName = recipeSnapshot.getKey();
                    Recipe recipe = recipeSnapshot.getValue(Recipe.class);
                    if (recipe != null) {
                        recipeMap.put(recipeName, recipe);
                    }
                }
                // Set the adapter after fetching recipes
                recipeAdapter = new RecipeAdapter(recipeMap, likedRecipeNames, likedRecipesRef);
                recyclerViewRecipes.setAdapter(recipeAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
                Log.e("RecipeFetch", "Error fetching recipes: " + error.getMessage());
            }
        });
    }


    public void getViews() {
        textAcronyms = findViewById(R.id.textAcronyms);
        menuIcon = findViewById(R.id.menuIcon);
        recyclerViewRecipes = findViewById(R.id.recyclerViewRecipes);
        searchField = findViewById(R.id.searchField);
        searchButton = findViewById(R.id.searchButton);
        clearButton = findViewById(R.id.clearButton);
        tagsLayout = findViewById(R.id.tagsLayout);
        setTags();
    }

    private void fetchUserLikedRecipes(String email) {
        DatabaseUtils.getInstance().getDatabaseReference().child("Users").child(email.replace(".", "!")).child("liked_recipes").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot recipeSnapshot : snapshot.getChildren()) {
                        String recipeName = recipeSnapshot.getKey();
                        Boolean isLiked = recipeSnapshot.getValue(Boolean.class);
                        if (isLiked != null && isLiked) {
                            likedRecipeNames.put(recipeName, true);
                        }
                    }
                }
                recipeAdapter.notifyDataSetChanged(); // Notify adapter of data change to update UI
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
                Log.e("UserFetch", "Error fetching user liked recipes: " + error.getMessage());
            }
        });
    }

    public void setTags() {
        for (String tag : tags) {
            Button tagButton = new Button(this);
            tagButton.setText(tag);
            tagButton.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            tagButton.setBackgroundResource(R.drawable.tag_button_background); // Set the selector drawable
            tagButton.setTextColor(Color.BLACK); // Set text color
            tagButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14); // Set text size
            tagButton.setPadding(10, 8, 16, 8); // Set padding
            tagButton.setAllCaps(false); // Disable all caps
            tagButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clearTags(); // Clear all tags background
                    // Handle tag button click
                    filterRecipesByTag(tag);

                    // Change background color for clicked state
                    tagButton.setBackgroundResource(R.drawable.clicked_background);
                }
            });
            tagsLayout.addView(tagButton);
        }
    }

    public void clearTags() {
        for (int i = 0; i < tagsLayout.getChildCount(); i++) {
            View child = tagsLayout.getChildAt(i);
            if (child instanceof Button) {
                Button tagButton = (Button) child;
                tagButton.setBackgroundResource(R.drawable.normal_background); // Set the normal background drawable
            }
        }
    }


    private void filterRecipesByTag(String tag) {
        Map<String, Recipe> filteredMap = new HashMap<>();
        for (Map.Entry<String, Recipe> entry : recipeMap.entrySet()) {
            Recipe recipe = entry.getValue();
            if (recipe.getTags().contains(tag)) {
                filteredMap.put(entry.getKey(), recipe);
            }
        }
        recipeAdapter = new RecipeAdapter(filteredMap, likedRecipeNames, likedRecipesRef);
        recyclerViewRecipes.setAdapter(recipeAdapter);
    }


    private void clearSearch() {
        clearTags();
        searchField.setText("");
        recipeAdapter = new RecipeAdapter(recipeMap, likedRecipeNames, likedRecipesRef);
        recyclerViewRecipes.setAdapter(recipeAdapter);
    }

    private void showLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(MainFeed.this, LoginActivity.class));
        finish(); // Close this activity
    }


    private void fetchUserInformation(String email) {
          DatabaseUtils.getInstance().getDatabaseReference().child("Users").orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener(){
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


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.uploadRecipe) {
            // Handle upload recipe action
            Intent uploadRecipeIntent = new Intent(MainFeed.this, UploadRecipe.class);
            uploadRecipeIntent.putExtra("email", userEmail);
            uploadRecipeIntent.putExtra("textAcronyms", textAcronyms.getText());
            startActivity(uploadRecipeIntent);
            finish();
            return true;
        } else if (id == R.id.myRecipes) {
            // Show recipes created by the user
            showMyRecipes();
            return true;
        } else if (id == R.id.likedRecipes) {
            // Show recipes liked by the user
            showLikedRecipes();
            return true;
        }
        else if (id == R.id.logOutUser) {
            showLogoutDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showMyRecipes() {
        clearSearch();
        String currentUserId = getCurrentUserId();
        Map<String, Recipe> myRecipes = new HashMap<>();
        for (Map.Entry<String, Recipe> entry : recipeMap.entrySet()) {
            Recipe recipe = entry.getValue();
            // Check if the recipe is created by the current user
            if (recipe.getCreatedBy().equals(currentUserId)) {
                myRecipes.put(entry.getKey(), recipe);
            }
        }
        // Update the adapter with myRecipes
        recipeAdapter = new RecipeAdapter(myRecipes, likedRecipeNames, likedRecipesRef);
        recyclerViewRecipes.setAdapter(recipeAdapter);
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



    private void showLikedRecipes() {
        clearSearch();
        Map<String, Recipe> likedRecipes = new HashMap<>();
        for (Map.Entry<String, Boolean> entry : likedRecipeNames.entrySet()) {
            String recipeName = entry.getKey();
            Boolean isLiked = entry.getValue();
            if (isLiked) {
                // Check if the liked recipe exists in the recipeMap
                Recipe likedRecipe = recipeMap.get(recipeName);
                if (likedRecipe != null) {
                    likedRecipes.put(recipeName, likedRecipe);
                }
            }
        }
        // Update the adapter with likedRecipes
        recipeAdapter = new RecipeAdapter(likedRecipes, likedRecipeNames, likedRecipesRef);
        recyclerViewRecipes.setAdapter(recipeAdapter);
    }

    private void showMenuOptions() {
        PopupMenu popupMenu = new PopupMenu(this, menuIcon);
        popupMenu.getMenuInflater().inflate(R.menu.menu_main, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.uploadRecipe) {
                    // Handle upload recipe action
                    Intent uploadRecipeIntent = new Intent(MainFeed.this, UploadRecipe.class);
                    uploadRecipeIntent.putExtra("email", userEmail);
                    uploadRecipeIntent.putExtra("textAcronyms", textAcronyms.getText());
                    startActivity(uploadRecipeIntent);
                    finish();
                    return true;
                } else if (id == R.id.myRecipes) {
                    showMyRecipes();
                    return true;
                } else if (id == R.id.likedRecipes) {
                    showLikedRecipes();
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

    private void performSearch() {
        clearTags();
        String query = searchField.getText().toString().trim().toLowerCase();
        Map<String, Recipe> searchedMap = new HashMap<>();
        for (Map.Entry<String, Recipe> entry : recipeMap.entrySet()) {
            Recipe recipe = entry.getValue();
            if (recipe.getRecipeName().toLowerCase().contains(query)) {
                searchedMap.put(entry.getKey(), recipe);
            }
        }
        recipeAdapter = new RecipeAdapter(searchedMap, likedRecipeNames, likedRecipesRef);
        recyclerViewRecipes.setAdapter(recipeAdapter);
    }

    @Override
    public void onRecipeClicked(Recipe recipe) {
        // Handle the recipe click here
        Intent intent = new Intent(MainFeed.this, RecipePageActivity.class);
        intent.putExtra("recipeName", recipe.getRecipeName());
        intent.putExtra("textAcronyms", textAcronyms.getText());
        startActivity(intent);
    }
}