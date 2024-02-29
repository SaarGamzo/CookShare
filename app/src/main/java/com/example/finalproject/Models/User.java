package com.example.finalproject.Models;

public class User {
    private String fullName;
    private String dateOfBirth;
    private String email;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String fullName, String dateOfBirth, String email) {
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.email = email;
    }

    // Getters and setters for each property
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
