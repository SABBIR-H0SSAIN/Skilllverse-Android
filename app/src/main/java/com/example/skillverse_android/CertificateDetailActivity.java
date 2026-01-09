package com.example.skillverse_android;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.DialogInterface;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.skillverse_android.models.Certificate;
import com.example.skillverse_android.utils.FirestoreRepository;
import com.example.skillverse_android.utils.FirebaseAuthManager;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class CertificateDetailActivity extends AppCompatActivity {
    
    public static final String EXTRA_CERTIFICATE_ID = "extra_certificate_id";

    private TextView tvCertificateTitle, tvCertificateId, tvCourseName, tvStudentName, tvCompletionDate;
    private TextView tvRevokeStatus, tvRevokeReason;
    private MaterialButton btnRevoke, btnUnrevoke;
    
    private String certificateDocId;
    private Certificate certificate;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_certificate_detail);

        db = FirebaseFirestore.getInstance();

        if (getIntent().hasExtra(EXTRA_CERTIFICATE_ID)) {
            certificateDocId = getIntent().getStringExtra(EXTRA_CERTIFICATE_ID);
        } else {
            Toast.makeText(this, "Error: No Certificate ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        loadCertificateDetails();
        setupListeners();
    }

    private void initializeViews() {
        tvCertificateTitle = findViewById(R.id.tvCertificateTitle);
        tvCertificateId = findViewById(R.id.tvCertificateId);
        tvCourseName = findViewById(R.id.tvCourseName);
        tvStudentName = findViewById(R.id.tvStudentName);
        tvCompletionDate = findViewById(R.id.tvCompletionDate);
        tvRevokeStatus = findViewById(R.id.tvRevokeStatus);
        tvRevokeReason = findViewById(R.id.tvRevokeReason);
        btnRevoke = findViewById(R.id.btnRevoke);
        btnUnrevoke = findViewById(R.id.btnUnrevoke);
    }

    private void setupListeners() {
        btnRevoke.setOnClickListener(v -> showRevokeDialog());
        btnUnrevoke.setOnClickListener(v -> showUnrevokeConfirmation());
    }

    private void loadCertificateDetails() {
        db.collection("certificates").document(certificateDocId).get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    certificate = documentSnapshot.toObject(Certificate.class);
                    if (certificate != null) {
                        certificate.setDocumentId(documentSnapshot.getId());
                        updateUI(certificate);
                    }
                } else {
                    Toast.makeText(this, "Certificate not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            })
            .addOnFailureListener(e -> Toast.makeText(this, "Error loading certificate: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateUI(Certificate cert) {
        tvCertificateId.setText("ID: " + cert.getCertificateId());
        tvCourseName.setText("Course: " + cert.getCourseName());
        tvStudentName.setText("Student: " + cert.getStudentName());
        tvCompletionDate.setText("Completed: " + cert.getDateCompleted());

        if (cert.isRevoked()) {
            tvRevokeStatus.setText("Status: REVOKED");
            tvRevokeStatus.setTextColor(Color.RED);
            tvRevokeReason.setVisibility(View.VISIBLE);
            tvRevokeReason.setText("Reason: " + cert.getRevokeReason());
            btnRevoke.setVisibility(View.GONE);
            btnUnrevoke.setVisibility(View.VISIBLE);
        } else {
            tvRevokeStatus.setText("Status: Active");
            tvRevokeStatus.setTextColor(Color.parseColor("#4CAF50"));  
            tvRevokeReason.setVisibility(View.GONE);
            btnRevoke.setVisibility(View.VISIBLE);
            btnUnrevoke.setVisibility(View.GONE);
        }
    }

    private void showRevokeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Revoke Certificate");

        final EditText input = new EditText(this);
        input.setHint("Enter reason for revocation...");
        builder.setView(input);

        builder.setPositiveButton("Confirm", (dialog, which) -> {
            String reason = input.getText().toString().trim();
            if (TextUtils.isEmpty(reason)) {
                Toast.makeText(this, "Revocation reason is required", Toast.LENGTH_SHORT).show();
                return;
            }
            performRevocation(reason);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showUnrevokeConfirmation() {
        new AlertDialog.Builder(this)
            .setTitle("Un-revoke Certificate")
            .setMessage("Are you sure you want to restore this certificate? This will clear the revocation history.")
            .setPositiveButton("Yes", (dialog, which) -> performUnrevocation())
            .setNegativeButton("No", null)
            .show();
    }

    private void performRevocation(String reason) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("isRevoked", true);
        updates.put("revokeReason", reason);
        updates.put("revokedAt", System.currentTimeMillis());
        updates.put("revokedBy", FirebaseAuthManager.getCurrentUser().getUid());

        updateFirestore(updates, "Certificate revoked successfully");
    }

    private void performUnrevocation() {
        Map<String, Object> updates = new HashMap<>();
        updates.put("isRevoked", false);
        updates.put("revokeReason", null);
        updates.put("revokedAt", null);
        updates.put("revokedBy", null);

        updateFirestore(updates, "Certificate restored successfully");
    }

    private void updateFirestore(Map<String, Object> updates, String successMessage) {
        db.collection("certificates").document(certificateDocId).update(updates)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, successMessage, Toast.LENGTH_SHORT).show();
                loadCertificateDetails();  
            })
            .addOnFailureListener(e -> Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
