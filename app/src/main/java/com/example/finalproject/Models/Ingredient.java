package com.example.finalproject.Models;

public class Ingredient {
    private String name;
    private double quantity;
    private String unit;

    // Constructors, getters, and setters

    public Ingredient() {
        // Default constructor required for Firebase
    }

    public Ingredient(String name, double quantity, String unit) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    // Getters and setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Override
    public String toString() {
        return "Ingredient{" +
                "name='" + name + '\'' +
                ", quantity=" + quantity +
                ", unit='" + unit + '\'' +
                '}';
    }
}
