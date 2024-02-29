package com.example.finalproject.Utils;

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
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private List<Recipe> recipeList;
    private Map<String, Boolean> likedRecipeNamesMap; // Map to store liked recipe names

    private DatabaseReference likedRecipesRef; // Firebase reference

    public RecipeAdapter(List<Recipe> recipeList, Map<String, Boolean> likedRecipeNamesMap, DatabaseReference likedRecipesRef) {
        this.recipeList = recipeList;
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
        Recipe recipe = recipeList.get(position);
        boolean isLiked = isRecipeLiked(recipe.getRecipeName());
        holder.bindRecipe(recipe, isLiked);
    }

    private boolean isRecipeLiked(String recipeName) {
        return this.likedRecipeNamesMap != null && this.likedRecipeNamesMap.containsKey(recipeName) && this.likedRecipeNamesMap.get(recipeName);
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

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
            likeButton = itemView.findViewById(R.id.like_img); // Assuming you have a like button in your card layout
        }

        public void bindRecipe(Recipe recipe, boolean isLiked) {
            textRecipeName.setText(recipe.getRecipeName());
            textSteps.setText("Steps: " + recipe.getSteps().size()); // Example, replace with actual steps count
            textIngredients.setText("Ingredients: " + recipe.getIngredients().size()); // Example, replace with actual ingredients count
            textPreparationTime.setText("Time: " + recipe.getCookingTimeMinutes() + " min");
            // Load recipe image using Glide
            Glide.with(itemView.getContext())
                    .load(recipe.getImageUrl())
                    .into(imageRecipe);

            // Set like button state
            likeButton.setSelected(isLiked);
            if(isLiked){
                likeButton.setImageResource(R.drawable.heart);
            }

            // Set click listener for like button
            likeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleLike(recipeList.get(getAdapterPosition())); // Use getAdapterPosition() to get the clicked recipe
                }
            });
        }

        private boolean isRecipeLiked(String recipeName) {
            return likedRecipeNamesMap != null && likedRecipeNamesMap.containsKey(recipeName) && likedRecipeNamesMap.get(recipeName);
        }

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
