package com.example.finalproject.UI;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalproject.R;

public class RecipePageActivity extends AppCompatActivity {

    private TextView recipeHeadline, tags, ingredients, steps, cookingTime;
    private ImageView recipeImage, cookingTimeImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recipe_page_activity);
        findViews();
        // Get intent extras from previous activity
        Intent intent = getIntent();
        if (intent != null) {
            String recipeName = intent.getStringExtra("recipeName");
//            String recipeTags = intent.getStringExtra("recipeTags");
//            String recipeIngredients = intent.getStringExtra("recipeIngredients");
//            String recipeSteps = intent.getStringExtra("recipeSteps");
//            String recipeCookingTime = intent.getStringExtra("recipeCookingTime");

            // Set the retrieved data to TextViews and ImageView
            recipeHeadline.setText(recipeName);
//            tags.setText(recipeTags);
//            ingredients.setText(recipeIngredients);
//            steps.setText(recipeSteps);
//            cookingTime.setText(recipeCookingTime);
            recipeImage.setImageResource(R.drawable.carrot);  // Set your recipe image here
            cookingTimeImg.setImageResource(R.drawable.clock);  // Set your cooking time image here
        }
    }

    private void findViews(){
        // Initialize TextViews
        recipeHeadline = findViewById(R.id.recipeHeadline);
//        tags = findViewById(R.id.tags);
//        ingredients = findViewById(R.id.ingredients);
//        steps = findViewById(R.id.steps);
//        cookingTime = findViewById(R.id.cookingTime);

        // Initialize ImageView
        recipeImage = findViewById(R.id.recipeImage);
        cookingTimeImg = findViewById(R.id.cookingTimeImg);
    }
}
