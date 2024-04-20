package com.example.finalproject.Utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

public class DatabaseUtils {
    private static DatabaseUtils instance;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;

    private FirebaseStorage firebaseStorage;

    private DatabaseUtils() {
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        firebaseStorage = FirebaseStorage.getInstance();
    }

    public static synchronized DatabaseUtils getInstance() {
        if (instance == null) {
            instance = new DatabaseUtils();
        }
        return instance;
    }

    public FirebaseStorage getFirebaseStorage(){return firebaseStorage;}

    public void fetchDatabase(){databaseReference = FirebaseDatabase.getInstance().getReference();}

    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public void signOutUser() {firebaseAuth.signOut();}

    public FirebaseAuth getFirebaseAuth(){
        return firebaseAuth;
    }

    public DatabaseReference getDatabaseReference() {
        return databaseReference;
    }

}
