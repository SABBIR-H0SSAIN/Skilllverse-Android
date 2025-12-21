package com.example.skillverse_android.utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
public class FirebaseManager {
    private static FirebaseManager instance;
    private final FirebaseAuth auth;
    private final FirebaseFirestore firestore;
    private FirebaseManager() {
        this.auth = FirebaseAuth.getInstance();
        this.firestore = FirebaseFirestore.getInstance();
        firestore.enableNetwork();
    }
    public static synchronized FirebaseManager getInstance() {
        if (instance == null) {
            instance = new FirebaseManager();
        }
        return instance;
    }
    public FirebaseAuth getAuth() {
        return auth;
    }
    public FirebaseFirestore getFirestore() {
        return firestore;
    }
}
