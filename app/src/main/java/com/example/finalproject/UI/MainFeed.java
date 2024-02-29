package com.example.finalproject.UI;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalproject.Models.User;
import com.example.finalproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainFeed extends AppCompatActivity {

    private TextView userEmailTextView;
    private TextView fullNameTextView;
    private TextView dateOfBirthTextView;
    private Button uploadRecipeButton; // Add this line

    private String userEmail;
    private DatabaseReference usersRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_feed_activity);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        userEmailTextView = findViewById(R.id.userEmailTextView);
        fullNameTextView = findViewById(R.id.fullNameTextView);
        dateOfBirthTextView = findViewById(R.id.dateOfBirthTextView);
        uploadRecipeButton = findViewById(R.id.uploadRecipeButton); // Initialize the button

        // Set OnClickListener for uploadRecipeButton
        uploadRecipeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainFeed.this, UploadRecipe.class));
            }
        });

        // Get user email from intent extras
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey("email")) {
            userEmail = extras.getString("email");
            // Fetch user information from Firebase Database
            fetchUserInformation(userEmail);
        }
    }

    private void fetchUserInformation(String email) {
        usersRef.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        User user = userSnapshot.getValue(User.class);
                        if (user != null) {
                            userEmailTextView.setText("Email: " + user.getEmail());
                            fullNameTextView.setText("Full Name: " + user.getFullName());
                            dateOfBirthTextView.setText("Date of Birth: " + user.getDateOfBirth());
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
}
