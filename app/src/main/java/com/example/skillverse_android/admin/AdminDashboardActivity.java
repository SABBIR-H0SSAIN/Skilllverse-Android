package com.example.skillverse_android.admin;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.example.skillverse_android.LoginActivity;
import com.example.skillverse_android.R;
import com.example.skillverse_android.utils.FirebaseAuthManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
public class AdminDashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar toolbar;
    private TextView tvTotalCourses;
    private TextView tvTotalStudents;
    private TextView tvTotalEnrollments;
    private TextView tvActiveKeys;
    private MaterialCardView cardManageCourses;
    private MaterialCardView cardManageInstructors;
    private MaterialCardView cardGenerateKeys;
    private MaterialCardView cardManageUsers;
    private FirebaseFirestore db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);
        db = FirebaseFirestore.getInstance();
        initializeViews();
        setupToolbar();
        setupNavigationDrawer();
        setupQuickActions();
        loadDashboardStats();
    }
    private void initializeViews() {
        drawerLayout = findViewById(R.id.admin_drawer_layout);
        navigationView = findViewById(R.id.admin_nav_view);
        toolbar = findViewById(R.id.admin_toolbar);
        tvTotalCourses = findViewById(R.id.tvTotalCourses);
        tvTotalStudents = findViewById(R.id.tvTotalStudents);
        tvTotalEnrollments = findViewById(R.id.tvTotalEnrollments);
        tvActiveKeys = findViewById(R.id.tvActiveKeys);
        cardManageCourses = findViewById(R.id.cardManageCourses);
        cardManageInstructors = findViewById(R.id.cardManageInstructors);
        cardGenerateKeys = findViewById(R.id.cardGenerateKeys);
        cardManageUsers = findViewById(R.id.cardManageUsers);
        if (drawerLayout == null || navigationView == null || toolbar == null) {
            Toast.makeText(this, "Error loading admin dashboard", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Admin Dashboard");
        }
    }
    private void setupNavigationDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.app_name, R.string.app_name);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        updateAdminInfo();
    }

    private void updateAdminInfo() {
        View headerView = navigationView.getHeaderView(0);
        TextView navHeaderName = headerView.findViewById(R.id.nav_header_admin_name);
        TextView navHeaderEmail = headerView.findViewById(R.id.nav_header_admin_email);

        com.google.firebase.auth.FirebaseUser currentUser = FirebaseAuthManager.getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            navHeaderEmail.setText(email);

            // Try to set initial name from Auth
            String displayName = currentUser.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                navHeaderName.setText(displayName);
            } else if (email != null) {
                String nameFromEmail = email.split("@")[0];
                navHeaderName.setText(nameFromEmail.substring(0, 1).toUpperCase() + nameFromEmail.substring(1));
            } else {
                navHeaderName.setText("Admin");
            }

            // Fetch latest from Firestore
            db.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        if (name != null && !name.isEmpty()) {
                            navHeaderName.setText(name);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    // Keep existing/auth name on failure
                });
        }
    }
    private void setupQuickActions() {
        if (cardManageCourses != null) {
            cardManageCourses.setOnClickListener(v -> {
                Intent intent = new Intent(this, AdminCoursesActivity.class);
                startActivity(intent);
            });
        }
        if (cardManageInstructors != null) {
            cardManageInstructors.setOnClickListener(v -> {
                Intent intent = new Intent(this, AdminInstructorsActivity.class);
                startActivity(intent);
            });
        }
        if (cardGenerateKeys != null) {
            cardGenerateKeys.setOnClickListener(v -> {
                Intent intent = new Intent(this, AdminKeysActivity.class);
                startActivity(intent);
            });
        }
        if (cardManageUsers != null) {
            cardManageUsers.setOnClickListener(v -> {
                Intent intent = new Intent(this, AdminUsersActivity.class);
                startActivity(intent);
            });
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardStats();
    }
    private void loadDashboardStats() {
        db.collection("courses")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (tvTotalCourses != null) {
                        tvTotalCourses.setText(String.valueOf(querySnapshot.size()));
                    }
                })
                .addOnFailureListener(e -> {
                    if (tvTotalCourses != null) {
                        tvTotalCourses.setText("0");
                    }
                });
        db.collection("users")
                .whereEqualTo("role", "student")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (tvTotalStudents != null) {
                        tvTotalStudents.setText(String.valueOf(querySnapshot.size()));
                    }
                })
                .addOnFailureListener(e -> {
                    if (tvTotalStudents != null) {
                        tvTotalStudents.setText("0");
                    }
                });
        db.collection("enrollments")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (tvTotalEnrollments != null) {
                        tvTotalEnrollments.setText(String.valueOf(querySnapshot.size()));
                    }
                })
                .addOnFailureListener(e -> {
                    if (tvTotalEnrollments != null) {
                        tvTotalEnrollments.setText("0");
                    }
                });
        db.collection("enrollmentKeys")
                .whereEqualTo("isUsed", false)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (tvActiveKeys != null) {
                        tvActiveKeys.setText(String.valueOf(querySnapshot.size()));
                    }
                })
                .addOnFailureListener(e -> {
                    if (tvActiveKeys != null) {
                        tvActiveKeys.setText("0");
                    }
                });
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_admin_dashboard) {
        } else if (id == R.id.nav_admin_courses) {
            Intent intent = new Intent(this, AdminCoursesActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_admin_instructors) {
            Intent intent = new Intent(this, AdminInstructorsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_admin_keys) {
            Intent intent = new Intent(this, AdminKeysActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_admin_users) {
            Intent intent = new Intent(this, AdminUsersActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_admin_logout) {
            handleLogout();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
    private void handleLogout() {
        FirebaseAuthManager.logoutUser();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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
