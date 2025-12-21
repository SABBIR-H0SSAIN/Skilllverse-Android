package com.example.skillverse_android.utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;
public class FirebaseAuthManager {
    private static final FirebaseAuth auth = FirebaseAuth.getInstance();
    private static final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onFailure(String error);
    }
    public static void registerUser(String email, String password, String name, AuthCallback callback) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    FirebaseUser user = task.getResult().getUser();
                    if (user != null) {
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build();
                        user.updateProfile(profileUpdates)
                            .addOnCompleteListener(updateTask -> {
                                createUserProfile(user.getUid(), name, email,
                                    () -> callback.onSuccess(user),
                                    error -> callback.onFailure("Registration successful but profile creation failed")
                                );
                            });
                    }
                } else {
                    String error = task.getException() != null ?
                        task.getException().getMessage() : "Registration failed";
                    callback.onFailure(error);
                }
            });
    }
    public static void loginUser(String email, String password, AuthCallback callback) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    FirebaseUser user = task.getResult().getUser();
                    if (user != null) {
                        callback.onSuccess(user);
                    } else {
                        callback.onFailure("Login failed");
                    }
                } else {
                    String error = task.getException() != null ?
                        task.getException().getMessage() : "Login failed";
                    callback.onFailure(error);
                }
            });
    }
    public static void logoutUser() {
        auth.signOut();
    }
    public static FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }
    public static boolean isUserLoggedIn() {
        return auth.getCurrentUser() != null;
    }
    private static void createUserProfile(String userId, String name, String email,
                                         Runnable onSuccess, java.util.function.Consumer<String> onFailure) {
        Map<String, Object> userProfile = new HashMap<>();
        userProfile.put("name", name);
        userProfile.put("email", email);
        userProfile.put("createdAt", System.currentTimeMillis());
        firestore.collection("users").document(userId)
            .set(userProfile)
            .addOnSuccessListener(aVoid -> onSuccess.run())
            .addOnFailureListener(e -> onFailure.accept(e.getMessage()));
    }
}
