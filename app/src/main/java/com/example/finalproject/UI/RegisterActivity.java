package com.example.finalproject.UI;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finalproject.R;
import com.example.finalproject.Utils.DatabaseUtils;
import com.example.finalproject.Utils.MyUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseUser;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private EditText fullNameEditText;
    private EditText registerEmailEditText;
    private EditText registerPasswordEditText;
    private EditText validatePasswordEditText;
    private Button registerButton;
    private Button selectDateOfBirthButton;
    private TextView selectedDateTextView;
    private MyUtils myUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initUtils();
        findAllViews();
        setListeners();
    }

    private void initUtils() {
        myUtils = MyUtils.getInstance(this);
    }

    private void findAllViews() {
        fullNameEditText = findViewById(R.id.fullNameEditText);
        registerEmailEditText = findViewById(R.id.registerEmailEditText);
        registerPasswordEditText = findViewById(R.id.registerPasswordEditText);
        validatePasswordEditText = findViewById(R.id.validatePasswordEditText);
        registerButton = findViewById(R.id.registerButton);
        selectDateOfBirthButton = findViewById(R.id.selectDateOfBirthButton);
        selectedDateTextView = findViewById(R.id.selectedDateTextView);
    }

    private void setListeners() {
        selectDateOfBirthButton.setOnClickListener(view -> showDatePickerDialog());
        registerButton.setOnClickListener(view -> verifyInputsAndRegister());
    }

    private void verifyInputsAndRegister() {
        if (validateInputs()) {
            registerUser();
        } else {
            showInputRestrictionsDialog();
        }
    }

    private boolean validateInputs() {
        Boolean returnValue = true;
        String fullName = fullNameEditText.getText().toString().trim();
        String dateOfBirth = selectedDateTextView.getText().toString().trim();
        String email = registerEmailEditText.getText().toString().trim();
        String password = registerPasswordEditText.getText().toString().trim();
        String confirmPassword = validatePasswordEditText.getText().toString().trim();

        // Validate full name
        if (TextUtils.isEmpty(fullName) || fullName.length() < 6 || fullName.length() > 20) {
            fullNameEditText.setError(getString(R.string.restriction_full_name));
            returnValue = false;
        }

        // Validate date of birth
        if (TextUtils.isEmpty(dateOfBirth)) {
            selectDateOfBirthButton.setError(getString(R.string.restriction_date_of_birth));
            returnValue = false;
        } else {
            // Check if date of birth is in the past
            Calendar today = Calendar.getInstance();
            Calendar dob = Calendar.getInstance();
            String[] dobParts = dateOfBirth.split("/");
            dob.set(Calendar.YEAR, Integer.parseInt(dobParts[2]));
            dob.set(Calendar.MONTH, Integer.parseInt(dobParts[1]) - 1); // Calendar months are 0-indexed
            dob.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dobParts[0]));

            if (dob.after(today)) {
                selectedDateTextView.setError(getString(R.string.restriction_date_of_birth));
                returnValue = false;
            }
        }

        // Validate username
        if (TextUtils.isEmpty(email) || email.indexOf('@') == -1) {
            registerEmailEditText.setError(getString(R.string.restriction_email));
            returnValue = false;
        }

        // Validate password
        if (TextUtils.isEmpty(password) || password.length() < 6 || password.length() > 10) {
            registerPasswordEditText.setError(getString(R.string.restriction_password));
            returnValue = false;
        } else {
            // Check if password contains uppercase, lowercase, and at least one number
            if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$")) {
                registerPasswordEditText.setError(getString(R.string.restriction_password));
                returnValue = false;
            }
        }

        // Validate confirmed password
        if (!confirmPassword.equals(password) || confirmPassword.equals("")) {
            validatePasswordEditText.setError(getString(R.string.restriction_confirm_password));
            returnValue = false;
        }
        return returnValue;
    }

    private void registerUser() {
        String email = registerEmailEditText.getText().toString().trim();
        String password = registerPasswordEditText.getText().toString().trim();
        String fullName = fullNameEditText.getText().toString().trim();
        String dateOfBirth = selectedDateTextView.getText().toString().trim();

        DatabaseUtils.getInstance().getFirebaseAuth().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = DatabaseUtils.getInstance().getCurrentUser();
                        if (user != null) {
                            saveUserToDatabase(email, fullName, dateOfBirth);
                            navigateToUserProfile(email);
                        } else {
                            myUtils.showToast("User is null!");
                        }
                    } else {
                        myUtils.showToast("Registration failed: " + task.getException().getMessage());
                    }
                });
    }

    private void saveUserToDatabase(String email, String fullName, String dateOfBirth) {
        Map<String, Boolean> likedRecipes = new HashMap<>();
        likedRecipes.put("a", true);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("fullName", fullName);
        hashMap.put("email", email);
        hashMap.put("dateOfBirth", dateOfBirth);
        hashMap.put("liked_recipes", likedRecipes);
        // Get a reference to the "users" node in the database
        DatabaseUtils.getInstance().getDatabaseReference().child("Users")
                .child(email.replace('.','!'))
                .setValue(hashMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        myUtils.showToast("Data added successfully!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        myUtils.showToast("Failed to add new user!");
                    }
                });
    }


    private void navigateToUserProfile(String email) {
        Intent intent = new Intent(RegisterActivity.this, MainFeed.class);
        intent.putExtra("email", email);
        startActivity(intent);
        finish(); // Finish RegisterActivity to prevent user from going back
    }

    private void showInputRestrictionsDialog() {
        // Construct the dialog message using string resources
        StringBuilder message = new StringBuilder();
        if (TextUtils.isEmpty(fullNameEditText.getText())) {
            message.append(getString(R.string.restriction_full_name)).append("\n");
            fullNameEditText.setError(getString(R.string.restriction_full_name));
        }
        if (TextUtils.isEmpty(selectedDateTextView.getText())) {
            message.append(getString(R.string.restriction_date_of_birth)).append("\n");
            selectDateOfBirthButton.setError(getString(R.string.restriction_date_of_birth));
        }
        if (TextUtils.isEmpty(registerEmailEditText.getText())) {
            message.append(getString(R.string.restriction_email)).append("\n");
            registerEmailEditText.setError(getString(R.string.restriction_email));
        }
        if (TextUtils.isEmpty(registerPasswordEditText.getText())) {
            message.append(getString(R.string.restriction_password)).append("\n");
            registerPasswordEditText.setError(getString(R.string.restriction_password));
        } else {
            String password = registerPasswordEditText.getText().toString().trim();
            // Check if password contains uppercase, lowercase, and at least one number
            if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$")) {
                message.append(getString(R.string.restriction_password)).append("\n");
                registerPasswordEditText.setError(getString(R.string.restriction_password));
            }
        }
        if (TextUtils.isEmpty(validatePasswordEditText.getText())) {
            message.append(getString(R.string.restriction_confirm_password)).append("\n");
            validatePasswordEditText.setError(getString(R.string.restriction_confirm_password));
        }

        // Show the dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Input Restrictions");
        builder.setMessage(message.toString());
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showDatePickerDialog() {
        // Get current date
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        // Create date picker dialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        // Update the dateOfBirthEditText with the selected date
                        String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                        selectedDateTextView.setText(selectedDate);
                    }
                },
                year, month, dayOfMonth);

        // Show the date picker dialog
        datePickerDialog.show();
    }
}
