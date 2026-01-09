package com.example.skillverse_android;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.example.skillverse_android.utils.FirebaseAuthManager;
import com.google.android.material.appbar.MaterialToolbar;
public class ProfileActivity extends AppCompatActivity {
    private static final String PREF_NAME = "SkillVersePrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_EMAIL = "userEmail";
    private SharedPreferences sharedPreferences;
    private TextView tvUserName, tvUserEmail, tvEnrolledCount, tvCompletedCount, btnLogout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setDecorFitsSystemWindows(false);
        setContentView(R.layout.activity_profile);
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        setupViews();
        loadUserData();
    }
    private void setupViews() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), v.getPaddingBottom());
            return WindowInsetsCompat.CONSUMED;
        });
        tvUserName = findViewById(R.id.tvUserName);
        tvUserEmail = findViewById(R.id.tvUserEmail);
        tvEnrolledCount = findViewById(R.id.tvEnrolledCount);
        tvCompletedCount = findViewById(R.id.tvCompletedCount);
        btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> handleLogout());
        findViewById(R.id.btnEditProfile).setOnClickListener(v -> showEditProfileDialog());
    }

    private void showEditProfileDialog() {
        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_profile, null);
        android.widget.EditText etName = dialogView.findViewById(R.id.etName);
        android.widget.EditText etOldPassword = dialogView.findViewById(R.id.etOldPassword);
        android.widget.EditText etNewPassword = dialogView.findViewById(R.id.etNewPassword);
        android.widget.Button btnSave = dialogView.findViewById(R.id.btnSave);

        etName.setText(tvUserName.getText());

        com.google.android.material.dialog.MaterialAlertDialogBuilder builder =
            new com.google.android.material.dialog.MaterialAlertDialogBuilder(this);
        builder.setView(dialogView);
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        
        // Make background transparent for rounded corners
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        btnSave.setOnClickListener(v -> {
            String newName = etName.getText().toString().trim();
            String oldPass = etOldPassword.getText().toString().trim();
            String newPass = etNewPassword.getText().toString().trim();

            if (newName.isEmpty()) {
                etName.setError("Name cannot be empty");
                return;
            }

            if (!newPass.isEmpty()) {
                if (newPass.length() < 6) {
                    etNewPassword.setError("Password must be at least 6 characters");
                    return;
                }
                if (oldPass.isEmpty()) {
                    etOldPassword.setError("Current password required to change password");
                    return;
                }
                handleUpdateProfile(newName, oldPass, newPass, dialog);
            } else {
                // Only update name
                 handleUpdateNameOnly(newName, dialog);
            }
        });

        dialog.show();
    }

    private void handleUpdateNameOnly(String newName, androidx.appcompat.app.AlertDialog dialog) {
        String userId = FirebaseAuthManager.getCurrentUser().getUid();
        
        // Show loading state if desired, or simpler toast flow
        FirebaseAuthManager.updateDisplayName(newName, new FirebaseAuthManager.AuthCallback() {
            @Override
            public void onSuccess(com.google.firebase.auth.FirebaseUser user) {
                com.example.skillverse_android.utils.FirestoreRepository.updateUserName(userId, newName, new com.example.skillverse_android.utils.FirestoreRepository.DataCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean data) {
                        tvUserName.setText(newName);
                        Toast.makeText(ProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(ProfileActivity.this, "Failed to update Firestore: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onFailure(String error) {
                Toast.makeText(ProfileActivity.this, "Failed to update name: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleUpdateProfile(String newName, String oldPass, String newPass, androidx.appcompat.app.AlertDialog dialog) {
        String userId = FirebaseAuthManager.getCurrentUser().getUid();

        // 1. Re-auth and Update Password
        FirebaseAuthManager.reauthenticateAndUpdatePassword(oldPass, newPass, new FirebaseAuthManager.AuthCallback() {
            @Override
            public void onSuccess(com.google.firebase.auth.FirebaseUser user) {
                // 2. Update Name (Auth + Firestore)
                FirebaseAuthManager.updateDisplayName(newName, new FirebaseAuthManager.AuthCallback() {
                    @Override
                    public void onSuccess(com.google.firebase.auth.FirebaseUser user) {
                        com.example.skillverse_android.utils.FirestoreRepository.updateUserName(userId, newName, new com.example.skillverse_android.utils.FirestoreRepository.DataCallback<Boolean>() {
                            @Override
                            public void onSuccess(Boolean data) {
                                tvUserName.setText(newName);
                                Toast.makeText(ProfileActivity.this, "Profile and password updated successfully", Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                            @Override
                            public void onFailure(String error) {
                                Toast.makeText(ProfileActivity.this, "Password updated, but failed to sync name: " + error, Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }
                        });
                    }
                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(ProfileActivity.this, "Password updated, but failed to update name: " + error, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
            }
            @Override
            public void onFailure(String error) {
                Toast.makeText(ProfileActivity.this, "Failed to update password: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadUserData() {
        String userEmail = FirebaseAuthManager.getCurrentUser().getEmail();
        final String email = (userEmail != null && !userEmail.isEmpty()) ? userEmail : "student@example.com";
        tvUserEmail.setText(email);
        String userId = FirebaseAuthManager.getCurrentUser().getUid();
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                String userName;
                if (documentSnapshot.exists() && documentSnapshot.contains("name")) {
                    userName = documentSnapshot.getString("name");
                } else {
                    userName = FirebaseAuthManager.getCurrentUser().getDisplayName();
                }
                if (userName == null || userName.isEmpty()) {
                    userName = email.split("@")[0];
                    userName = userName.substring(0, 1).toUpperCase() + userName.substring(1);
                }
                tvUserName.setText(userName);
            })
            .addOnFailureListener(e -> {
                String userName = email.split("@")[0];
                userName = userName.substring(0, 1).toUpperCase() + userName.substring(1);
                tvUserName.setText(userName);
            });
        tvEnrolledCount.setText("2");
        tvCompletedCount.setText("0");
    }

    private void handleLogout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        FirebaseAuthManager.logoutUser();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
