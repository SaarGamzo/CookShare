package com.example.finalproject.UI;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalproject.Models.User;
import com.example.finalproject.R;
import com.example.finalproject.Utils.DatabaseUtils;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

public class PersonalDetails extends AppCompatActivity {

    private TextView fullNameTextView;
    private TextView dateOfBirthTextView;
    private TextView emailTextView;
    private ImageView menuIcon;
    private TextView textAcronyms;
    private String userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.personal_details_activity);
        findViews();
        userEmail = getIntent().getStringExtra("email");
        textAcronyms.setText(getIntent().getStringExtra("textAcronyms"));

        // Get the currently logged-in user
        FirebaseUser user = DatabaseUtils.getInstance().getCurrentUser();
        if (user != null) {
            userEmail = userEmail.replace(".","!");
            // Query the database to fetch user data
            DatabaseUtils.getInstance().getDatabaseReference().child("Users").child(userEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // Retrieve user data
                        User currentUser = dataSnapshot.getValue(User.class);
                        if (currentUser != null) {
                            // Set user data to TextViews
                            fullNameTextView.setText(currentUser.getFullName());
                            dateOfBirthTextView.setText(currentUser.getDateOfBirth());
                            emailTextView.setText(currentUser.getEmail());
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle any errors
                }
            });
        }

        // Set OnClickListener for menuIcon
        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenuOptions();
            }
        });

    }

    private void showMenuOptions() {
        PopupMenu popupMenu = new PopupMenu(this, menuIcon);
        popupMenu.getMenuInflater().inflate(R.menu.personal_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.mainFeed) {
                    // Handle upload recipe action
                    Intent mainIntent = new Intent(PersonalDetails.this, MainFeed.class);
                    mainIntent.putExtra("email", userEmail);
                    mainIntent.putExtra("textAcronyms", textAcronyms.getText());
                    startActivity(mainIntent);
                    finish();
                    return true;
                } else if (id == R.id.uploadARecipe) {
                    Intent uploadRecipeIntent = new Intent(PersonalDetails.this, UploadRecipe.class);
                    uploadRecipeIntent.putExtra("email", userEmail);
                    uploadRecipeIntent.putExtra("textAcronyms", textAcronyms.getText());
                    startActivity(uploadRecipeIntent);
                    finish();
                    return true;
                }
                else if (id == R.id.logOutUser) {
                    showLogoutDialog();
                    return true;
                }
                return false;
            }
        });
        popupMenu.show();
    }

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

    private void logoutUser() {
        // Perform logout action
        DatabaseUtils.getInstance().getFirebaseAuth().signOut();
        startActivity(new Intent(PersonalDetails.this, LoginActivity.class));
        finish(); // Close this activity
    }


    private void findViews(){
        // Initialize TextViews
        menuIcon = findViewById(R.id.menuIcon);
        textAcronyms = findViewById(R.id.textAcronyms);
        fullNameTextView = findViewById(R.id.fullNameTextView);
        dateOfBirthTextView = findViewById(R.id.DOBTextView);
        emailTextView = findViewById(R.id.emailTextView);
    }
}
