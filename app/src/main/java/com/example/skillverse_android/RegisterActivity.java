package com.example.skillverse_android;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.skillverse_android.databinding.ActivityRegisterBinding;
import com.example.skillverse_android.utils.FirebaseAuthManager;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseUser;
public class RegisterActivity extends AppCompatActivity {
    private ActivityRegisterBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setDecorFitsSystemWindows(false);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setupClickListeners();
    }
    private void setupClickListeners() {
        binding.btnRegister.setOnClickListener(v -> handleRegistration());
        binding.tvSignIn.setOnClickListener(v -> {
            finish();
        });
    }
    private void handleRegistration() {
        String fullName = binding.etFullName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();
        if (!validateName(fullName) || !validateEmail(email) ||
            !validatePassword(password) || !validateConfirmPassword(password, confirmPassword)) {
            return;
        }
        binding.btnRegister.setEnabled(false);
        binding.btnRegister.setText("Creating account...");
        FirebaseAuthManager.registerUser(email, password, fullName, new FirebaseAuthManager.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                runOnUiThread(() -> {
                    binding.btnRegister.setEnabled(true);
                    binding.btnRegister.setText(R.string.register_button);
                    Toast.makeText(RegisterActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    binding.btnRegister.setEnabled(true);
                    binding.btnRegister.setText(R.string.register_button);
                    Toast.makeText(RegisterActivity.this, "Registration failed: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }
    private boolean validateName(String name) {
        TextInputLayout nameLayout = binding.nameInputLayout;
        if (TextUtils.isEmpty(name)) {
            nameLayout.setError(getString(R.string.error_empty_name));
            return false;
        }
        if (name.length() < 2) {
            nameLayout.setError("Name must be at least 2 characters");
            return false;
        }
        nameLayout.setError(null);
        return true;
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
        if (!password.matches(".*[a-zA-Z].*") || !password.matches(".*\\d.*")) {
            passwordLayout.setError(getString(R.string.error_weak_password));
            return false;
        }
        passwordLayout.setError(null);
        return true;
    }
    private boolean validateConfirmPassword(String password, String confirmPassword) {
        TextInputLayout confirmPasswordLayout = binding.confirmPasswordInputLayout;
        if (TextUtils.isEmpty(confirmPassword)) {
            confirmPasswordLayout.setError(getString(R.string.error_empty_password));
            return false;
        }
        if (!password.equals(confirmPassword)) {
            confirmPasswordLayout.setError(getString(R.string.error_password_mismatch));
            return false;
        }
        confirmPasswordLayout.setError(null);
        return true;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
