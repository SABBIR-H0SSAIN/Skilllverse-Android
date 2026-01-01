package com.example.skillverse_android.utils;
import com.example.skillverse_android.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
public class AdminAuthManager {
    private static final String COLLECTION_USERS = "users";
    private static final String ROLE_ADMIN = "admin";
    private static final String ROLE_STUDENT = "student";
    public static void checkAdminAccess(OnAdminCheckListener listener) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            listener.onAdminCheckComplete(false, null);
            return;
        }
        FirebaseFirestore.getInstance()
                .collection(COLLECTION_USERS)
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        if (user != null) {
                            boolean isAdmin = ROLE_ADMIN.equals(user.getRole());
                            listener.onAdminCheckComplete(isAdmin, user);
                        } else {
                            listener.onAdminCheckComplete(false, null);
                        }
                    } else {
                        listener.onAdminCheckComplete(false, null);
                    }
                })
                .addOnFailureListener(e -> {
                    listener.onAdminCheckComplete(false, null);
                });
    }
    public static void getUserRole(OnRoleCheckListener listener) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            listener.onRoleCheckComplete(null);
            return;
        }
        FirebaseFirestore.getInstance()
                .collection(COLLECTION_USERS)
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String role = documentSnapshot.getString("role");
                        listener.onRoleCheckComplete(role != null ? role : ROLE_STUDENT);
                    } else {
                        listener.onRoleCheckComplete(ROLE_STUDENT);
                    }
                })
                .addOnFailureListener(e -> {
                    listener.onRoleCheckComplete(ROLE_STUDENT);
                });
    }
    public static void getCurrentUser(OnUserFetchListener listener) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            listener.onUserFetchComplete(null);
            return;
        }
        FirebaseFirestore.getInstance()
                .collection(COLLECTION_USERS)
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        listener.onUserFetchComplete(user);
                    } else {
                        listener.onUserFetchComplete(null);
                    }
                })
                .addOnFailureListener(e -> {
                    listener.onUserFetchComplete(null);
                });
    }
    public interface OnAdminCheckListener {
        void onAdminCheckComplete(boolean isAdmin, User user);
    }
    public interface OnRoleCheckListener {
        void onRoleCheckComplete(String role);
    }
    public interface OnUserFetchListener {
        void onUserFetchComplete(User user);
    }
}
