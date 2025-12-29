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
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
