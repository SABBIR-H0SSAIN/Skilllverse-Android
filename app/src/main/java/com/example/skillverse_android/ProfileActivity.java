package com.example.skillverse_android;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.skillverse_android.utils.FirebaseAuthManager;
import com.example.skillverse_android.utils.SwipeGestureHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
public class ProfileActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String PREF_NAME = "SkillVersePrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_EMAIL = "userEmail";
    private SharedPreferences sharedPreferences;
    private TextView tvUserName, tvUserEmail, tvEnrolledCount, tvCompletedCount, btnLogout;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private SwipeGestureHelper swipeHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setDecorFitsSystemWindows(false);
        setContentView(R.layout.activity_profile);
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        setupViews();
        setupNavigationDrawer();
        loadUserData();
    }
    private void setupViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
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

    private void setupNavigationDrawer() {
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(this);
        ViewCompat.setOnApplyWindowInsetsListener(navigationView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            View headerView = navigationView.getHeaderView(0);
            headerView.setPadding(
                headerView.getPaddingLeft(),
                systemBars.top + 24,
                headerView.getPaddingRight(),
                headerView.getPaddingBottom()
            );
            return insets;
        });

        updateNavHeader();

        swipeHelper = new SwipeGestureHelper(this, () -> {
            if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    private void updateNavHeader() {
        if (FirebaseAuthManager.getCurrentUser() == null) return;
        String userEmail = FirebaseAuthManager.getCurrentUser().getEmail();
        String userId = FirebaseAuthManager.getCurrentUser().getUid();
        View headerView = navigationView.getHeaderView(0);
        TextView navHeaderName = headerView.findViewById(R.id.nav_header_name);
        TextView navHeaderEmail = headerView.findViewById(R.id.nav_header_email);
        navHeaderEmail.setText(userEmail != null ? userEmail : "");
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
                    userName = userEmail != null ? userEmail.split("@")[0] : "User";
                }
                navHeaderName.setText(userName);
            });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_dashboard) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else if (id == R.id.nav_browse_courses) {
            startActivity(new Intent(this, BrowseCoursesActivity.class));
        } else if (id == R.id.nav_my_courses) {
            startActivity(new Intent(this, MyCoursesActivity.class));
        } else if (id == R.id.nav_certificates) {
            startActivity(new Intent(this, MyCertificatesActivity.class));
        } else if (id == R.id.nav_profile) {
             
        } else if (id == R.id.nav_logout) {
            handleLogout();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
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
                 handleUpdateNameOnly(newName, dialog);
            }
        });

        dialog.show();
    }

    private void handleUpdateNameOnly(String newName, androidx.appcompat.app.AlertDialog dialog) {
        String userId = FirebaseAuthManager.getCurrentUser().getUid();
        
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

        FirebaseAuthManager.reauthenticateAndUpdatePassword(oldPass, newPass, new FirebaseAuthManager.AuthCallback() {
            @Override
            public void onSuccess(com.google.firebase.auth.FirebaseUser user) {
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

    @Override
    public boolean dispatchTouchEvent(android.view.MotionEvent ev) {
        if (swipeHelper != null && swipeHelper.onTouchEvent(ev)) {
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
