package com.example.finalproject.Models;

import java.util.List;

public class Recipe {
    private String recipeName;
    private List<String> tags;
    private List<Ingredient> ingredients;
    private List<String> steps;
    private int cookingTimeMinutes;
    private long createdTimestamp;
    private String createdBy;
    private String imageUrl; // Added imageUrl field

    // Default no-argument constructor required by Firebase
    public Recipe() {
        // Default constructor required for Firebase
    }

    // Constructor with imageUrl parameter
    public Recipe(String recipeName, List<String> tags, List<Ingredient> ingredients, List<String> steps, int cookingTimeMinutes, long createdTimestamp, String createdBy, String imageUrl) {
        this.recipeName = recipeName;
        this.tags = tags;
        this.ingredients = ingredients;
        this.steps = steps;
        this.cookingTimeMinutes = cookingTimeMinutes;
        this.createdTimestamp = createdTimestamp;
        this.createdBy = createdBy;
        this.imageUrl = imageUrl; // Assigning the imageUrl parameter
    }

    // Getters and setters
    public String getRecipeName() {
        return recipeName;
    }

    public void setRecipeName(String recipeName) {
        this.recipeName = recipeName;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public List<String> getSteps() {
        return steps;
    }

    public void setSteps(List<String> steps) {
        this.steps = steps;
    }

    public int getCookingTimeMinutes() {
        return cookingTimeMinutes;
    }

    public void setCookingTimeMinutes(int cookingTimeMinutes) {
        this.cookingTimeMinutes = cookingTimeMinutes;
    }

    public long getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(long createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
