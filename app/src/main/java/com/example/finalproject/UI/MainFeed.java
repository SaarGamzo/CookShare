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
import com.example.finalproject.Utils.RecipeAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainFeed extends AppCompatActivity {

    private ImageView menuIcon;
    private TextView textAcronyms;

    private String userEmail;
    private DatabaseReference usersRef;
    private FirebaseAuth mAuth;

    private RecyclerView recyclerViewRecipes;
    private RecipeAdapter recipeAdapter;
    private List<Recipe> recipeList;

    private List<Recipe> originalRecipeList;

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
        recipeList = new ArrayList<>();
        likedRecipeNames = new HashMap<>();

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        // Get user email from intent extras
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey("email")) {
            userEmail = extras.getString("email");
            // Fetch user information from Firebase Database
            fetchUserInformation(userEmail);
            userEmail = userEmail.replace(".", "!");
            fetchUserLikedRecipes(userEmail);
        }

        // Initialize Firebase database reference for liked recipes
        DatabaseReference likedRecipesRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userEmail.replace(".", "!")).child("liked_recipes");
        recipeAdapter = new RecipeAdapter(recipeList, likedRecipeNames, likedRecipesRef);

        // Set layout manager and adapter for RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewRecipes.setLayoutManager(layoutManager);
        recyclerViewRecipes.setAdapter(recipeAdapter);

        // Call fetchRecipes() method to fetch recipes from Firebase
        fetchRecipes();
        // Call initializeOriginalRecipeList after fetching recipes
        initializeOriginalRecipeList();

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
                showLogoutDialog();
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


    private void initializeOriginalRecipeList() {
        originalRecipeList = new ArrayList<>(recipeList);
    }

    public void getViews(){
        textAcronyms = findViewById(R.id.textAcronyms);
        menuIcon = findViewById(R.id.menuIcon);
        recyclerViewRecipes = findViewById(R.id.recyclerViewRecipes);
        searchField = findViewById(R.id.searchField);
        searchButton = findViewById(R.id.searchButton);
        clearButton = findViewById(R.id.clearButton);
        tagsLayout = findViewById(R.id.tagsLayout);
        setTags();
    }

    private void fetchUserLikedRecipes(String userEmail) {
        // Assuming you have a database reference for liked recipes
        DatabaseReference likedRecipesRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userEmail).child("liked_recipes");
        likedRecipesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                likedRecipeNames.clear();
                for (DataSnapshot recipeSnapshot : snapshot.getChildren()) {
                    String recipeName = recipeSnapshot.getKey(); // Assuming the key is the recipe name
                    Boolean isLiked = recipeSnapshot.getValue(Boolean.class); // Get liked status
                    likedRecipeNames.put(recipeName, isLiked);
                }
                // Notify the adapter of data change to update UI
                recipeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
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

        // Clear the current list
        recipeList.clear();

        // Fetch all recipes again from Firebase and apply tag filter
        DatabaseReference recipesRef = FirebaseDatabase.getInstance().getReference().child("Recipes");
        recipesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot recipeSnapshot : snapshot.getChildren()) {
                    Recipe recipe = recipeSnapshot.getValue(Recipe.class);
                    if (recipe != null && recipe.getTags() != null && recipe.getTags().contains(tag)) {
                        recipeList.add(recipe);
                    }
                }
                // Notify the adapter of the data change
                recipeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
            }
        });
    }





    private void clearSearch() {
        searchField.setText(""); // Clear the search field
        performSearch();
        clearTags();
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

    private void fetchRecipes() {
        DatabaseReference recipesRef = FirebaseDatabase.getInstance().getReference().child("Recipes");
        recipesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recipeList.clear();
                for (DataSnapshot recipeSnapshot : snapshot.getChildren()) {
                    Recipe recipe = recipeSnapshot.getValue(Recipe.class);
                    if (recipe != null) {
                        recipeList.add(recipe);
                    }
                }
                recipeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
            }
        });
    }

    private void fetchUserInformation(String email) {
        usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
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
//            startActivity(new Intent(MainActivity.this, UploadRecipeActivity.class));
            return true;
        } else if (id == R.id.updateRecipes) {
//            startActivity(new Intent(MainActivity.this, UpdateRecipesActivity.class));
            return true;
        } else if (id == R.id.myRecipes) {
//            startActivity(new Intent(MainActivity.this, MyRecipesActivity.class));
            return true;
        } else if (id == R.id.likedRecipes) {
//            startActivity(new Intent(MainActivity.this, LikedRecipesActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showMenuOptions() {
        PopupMenu popupMenu = new PopupMenu(this, menuIcon);
        popupMenu.getMenuInflater().inflate(R.menu.menu_main, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.uploadRecipe) {
                    startActivity(new Intent(MainFeed.this, UploadRecipe.class));
                    return true;
                } else if (id == R.id.updateRecipes) {
//                    startActivity(new Intent(MainFeed.this, UpdateRecipesActivity.class));
                    return true;
                } else if (id == R.id.myRecipes) {
//                    startActivity(new Intent(MainFeed.this, MyRecipesActivity.class));
                    return true;
                } else if (id == R.id.likedRecipes) {
//                    startActivity(new Intent(MainFeed.this, LikedRecipesActivity.class));
                    return true;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void performSearch() {
        String query = searchField.getText().toString().trim().toLowerCase();

        // Clear the current list
        recipeList.clear();

        // Fetch all recipes again from Firebase and apply search filter
        DatabaseReference recipesRef = FirebaseDatabase.getInstance().getReference().child("Recipes");
        recipesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot recipeSnapshot : snapshot.getChildren()) {
                    Recipe recipe = recipeSnapshot.getValue(Recipe.class);
                    if (recipe != null && recipe.getRecipeName().toLowerCase().contains(query)) {
                        recipeList.add(recipe);
                    }
                }
                // Notify the adapter of the data change
                recipeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
            }
        });
    }
}