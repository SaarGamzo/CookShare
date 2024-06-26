package com.example.finalproject.Utils;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.finalproject.Models.Recipe;
import com.example.finalproject.R;
import com.example.finalproject.UI.RecipePageActivity;
import com.google.firebase.database.DatabaseReference;

import java.util.Map;

/**
 * Adapter class to populate the RecyclerView with recipe data.
 */
public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder>  {

    private Map<String, Recipe> recipeMap; // Map to store recipes
    private Map<String, Boolean> likedRecipeNamesMap; // Map to store liked recipe names
    private DatabaseReference likedRecipesRef; // Firebase reference
    private RecipeClickListener recipeClickListener;

    /**
     * Interface for handling recipe click events.
     */
    public interface RecipeClickListener {
        void onRecipeClicked(Recipe recipe);
    }

    /**
     * Setter for recipe click listener.
     * @param listener RecipeClickListener instance
     */
    public void setRecipeClickListener(RecipeClickListener listener) {
        this.recipeClickListener = listener;
    }

    /**
     * Constructor for RecipeAdapter.
     * @param recipeMap Map containing recipes
     * @param likedRecipeNamesMap Map containing liked recipe names
     * @param likedRecipesRef Firebase reference for liked recipes
     */
    public RecipeAdapter(Map<String, Recipe> recipeMap, Map<String, Boolean> likedRecipeNamesMap, DatabaseReference likedRecipesRef) {
        this.recipeMap = recipeMap;
        this.likedRecipeNamesMap = likedRecipeNamesMap;
        this.likedRecipesRef = likedRecipesRef;
    }


    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        String recipeKey = (String) recipeMap.keySet().toArray()[position];
        Recipe recipe = recipeMap.get(recipeKey);
        boolean isLiked = holder.isRecipeLiked(recipeKey);
        holder.bindRecipe(recipe, isLiked);
        holder.imageRecipe.setOnClickListener(v -> {
            // Create an intent to navigate to RecipePageActivity
            Intent intent = new Intent(v.getContext(), RecipePageActivity.class);
            intent.putExtra("recipeName", recipe.getRecipeName());
            v.getContext().startActivity(intent);
        });

        // Click listener for the recipe name text
        holder.textRecipeName.setOnClickListener(v -> {
            // Create an intent to navigate to RecipePageActivity
            Intent intent = new Intent(v.getContext(), RecipePageActivity.class);
            intent.putExtra("recipeName", recipe.getRecipeName());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return recipeMap.size();
    }

    /**
     * ViewHolder class for recipes.
     */
    public class RecipeViewHolder extends RecyclerView.ViewHolder {

        private TextView textRecipeName;
        private TextView textSteps;
        private TextView textIngredients;
        private TextView textPreparationTime;
        private ImageView imageRecipe;
        private ImageView likeButton;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            textRecipeName = itemView.findViewById(R.id.textRecipeName);
            textSteps = itemView.findViewById(R.id.textSteps);
            textIngredients = itemView.findViewById(R.id.textIngredients);
            textPreparationTime = itemView.findViewById(R.id.textPreparationTime);
            imageRecipe = itemView.findViewById(R.id.imageRecipe);
            likeButton = itemView.findViewById(R.id.like_img);
            // Set click listener for the item view
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && recipeClickListener != null) {
                        Recipe recipe = recipeMap.get(recipeMap.keySet().toArray()[position]);
                        recipeClickListener.onRecipeClicked(recipe);
                    }
                }
            });
        }

        /**
         * Bind recipe data to ViewHolder.
         * @param recipe Recipe object
         * @param isLiked Flag indicating if recipe is liked
         */
        public void bindRecipe(Recipe recipe, boolean isLiked) {
            textRecipeName.setText(recipe.getRecipeName());
            textSteps.setText("Steps: " + recipe.getSteps().size());
            textIngredients.setText("Ingredients: " + recipe.getIngredients().size());
            textPreparationTime.setText("Cooking Time: " + recipe.getCookingTimeMinutes() + " min");
            // Load recipe image using Glide
            Glide.with(itemView.getContext())
                    .load(recipe.getImageUrl())
                    .into(imageRecipe);

            // Set like button state
            likeButton.setSelected(isLiked);
            if (isLiked) {
                likeButton.setImageResource(R.drawable.heart);
            }

            // Set click listener for like button
            likeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleLike(recipe); // Pass the recipe directly
                }
            });
        }

        /**
         * Check if recipe is liked.
         * @param recipeKey Key of the recipe
         * @return True if recipe is liked, false otherwise
         */
        private boolean isRecipeLiked(String recipeKey) {
            return likedRecipeNamesMap != null && likedRecipeNamesMap.containsKey(recipeKey) && likedRecipeNamesMap.get(recipeKey);
        }

        /**
         * Toggle like status for a recipe.
         * @param recipe Recipe object
         */
        private void toggleLike(Recipe recipe) {
            String recipeName = recipe.getRecipeName();
            boolean isRecipeLiked = isRecipeLiked(recipeName);
            if (isRecipeLiked) {
                likedRecipeNamesMap.put(recipeName, false); // Recipe already liked, so mark as unliked
                likeButton.setImageResource(R.drawable.empty_heart); // Change image to empty heart
                // Update Firebase database to remove the liked recipe
                likedRecipesRef.child(recipeName).removeValue();
            } else {
                likedRecipeNamesMap.put(recipeName, true); // Recipe not liked, so mark as liked
                likeButton.setImageResource(R.drawable.heart); // Change image to filled heart
                // Update Firebase database to add the liked recipe
                likedRecipesRef.child(recipeName).setValue(true);
            }
            notifyDataSetChanged(); // Notify adapter of data change to update UI
        }
    }
}
