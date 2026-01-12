package com.example.skillverse_android;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.skillverse_android.admin.AdminDashboardActivity;
import com.example.skillverse_android.databinding.ActivityLoginBinding;
import com.example.skillverse_android.utils.AdminAuthManager;
import com.example.skillverse_android.utils.FirebaseAuthManager;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;
import android.content.SharedPreferences;

public class LoginActivity extends AppCompatActivity {
    private static final String PREF_NAME = "SkillVersePrefs";
    private static final String KEY_USER_ROLE = "user_role";
    private ActivityLoginBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setDecorFitsSystemWindows(false);

         
        if (FirebaseAuthManager.isUserLoggedIn()) {
            SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            String cachedRole = prefs.getString(KEY_USER_ROLE, null);
            
            if (cachedRole != null) {
                 
                if ("admin".equals(cachedRole)) {
                    navigateToAdminDashboard();
                } else {
                    navigateToMainActivity();
                }
                return;
            }
            
             
            binding = ActivityLoginBinding.inflate(getLayoutInflater());
            setContentView(binding.getRoot());
            showLoadingState();
            checkUserRoleAndNavigate(true);
            return;
        }

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupClickListeners();
    }
    private void setupClickListeners() {
        binding.btnLogin.setOnClickListener(v -> handleLogin());
        binding.tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

    }
    private void handleLogin() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        if (!validateEmail(email) || !validatePassword(password)) {
            return;
        }
        binding.btnLogin.setEnabled(false);
        binding.btnLogin.setText("Logging in...");
        FirebaseAuthManager.loginUser(email, password, new FirebaseAuthManager.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                runOnUiThread(() -> {
                    checkUserRoleAndNavigate(false);
                });
            }
            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    binding.btnLogin.setEnabled(true);
                    binding.btnLogin.setText(R.string.login_button);
                    Toast.makeText(LoginActivity.this, "Login failed: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    private boolean validateEmail(String email) {
        TextInputLayout emailLayout = binding.emailInputLayout;
        if (TextUtils.isEmpty(email)) {
            emailLayout.setError(getString(R.string.error_empty_email));
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError(getString(R.string.error_invalid_email));
            return false;
        }
        emailLayout.setError(null);
        return true;
    }
    private boolean validatePassword(String password) {
        TextInputLayout passwordLayout = binding.passwordInputLayout;
        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError(getString(R.string.error_empty_password));
            return false;
        }
        if (password.length() < 6) {
            passwordLayout.setError(getString(R.string.error_short_password));
            return false;
        }
        passwordLayout.setError(null);
        return true;
    }
    private void showLoadingState() {
        for (int i = 0; i < binding.getRoot().getChildCount(); i++) {
            binding.getRoot().getChildAt(i).setVisibility(View.GONE);
        }
        android.widget.ProgressBar progressBar = new android.widget.ProgressBar(this);
        binding.getRoot().addView(progressBar);
    }

    private void checkUserRoleAndNavigate(boolean isAutoLogin) {
        AdminAuthManager.getUserRole(new AdminAuthManager.OnRoleCheckListener() {
            @Override
            public void onRoleCheckComplete(String role) {
                boolean isAdminSelected = binding.rbAdmin.isChecked();

                getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                    .edit()
                    .putString(KEY_USER_ROLE, role)
                    .apply();

                if (!isAutoLogin) {
                    binding.btnLogin.setEnabled(true);
                    binding.btnLogin.setText(R.string.login_button);
                }

                if (isAdminSelected) {
                    if ("admin".equals(role)) {
                        navigateToAdminDashboard();
                    } else {
                        navigateToMainActivity();
                    }
                } else {
                    navigateToMainActivity();
                }
            }
        });
    }
    private void navigateToAdminDashboard() {
        Intent intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
