package com.example.skillverse_android.admin;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.skillverse_android.R;
import com.example.skillverse_android.admin.adapters.AdminKeyAdapter;
import com.example.skillverse_android.admin.dialogs.GenerateKeysDialog;
import com.example.skillverse_android.admin.repository.AdminFirestoreRepository;
import com.example.skillverse_android.admin.utils.ConfirmationDialog;
import com.example.skillverse_android.models.EnrollmentKey;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
public class AdminKeysActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AdminKeyAdapter adapter;
    private List<EnrollmentKey> keyList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_keys);
        try {
            MaterialToolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setTitle("Enrollment Keys");
                }
            }
            recyclerView = findViewById(R.id.recyclerView);
            FloatingActionButton fab = findViewById(R.id.fab);
            if (recyclerView != null) {
                setupRecyclerView();
            }
            if (fab != null) {
                fab.setOnClickListener(v -> {
                    GenerateKeysDialog dialog = new GenerateKeysDialog();
                    dialog.setOnKeysGeneratedListener(() -> {
                    });
                    dialog.show(getSupportFragmentManager(), "generate_keys");
                });
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    private void setupRecyclerView() {
        adapter = new AdminKeyAdapter(keyList, key -> {
            ConfirmationDialog.showDeleteConfirmation(AdminKeysActivity.this, "Key", () -> {
                deleteKey(key);
            });
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
    @Override
    protected void onResume() {
        super.onResume();
    }
    private com.google.firebase.firestore.ListenerRegistration keyListener;
    @Override
    protected void onStart() {
        super.onStart();
        startListening();
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (keyListener != null) {
            keyListener.remove();
            keyListener = null;
        }
    }
    private void startListening() {
        if (keyListener != null) return;
        keyListener = AdminFirestoreRepository.listenToKeys((queryDocumentSnapshots, e) -> {
            if (e != null) {
                Toast.makeText(AdminKeysActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            if (queryDocumentSnapshots != null && adapter != null) {
                keyList.clear();
                for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    EnrollmentKey key = documentToEnrollmentKey(document);
                    if (key != null) {
                        keyList.add(key);
                    }
                }
                java.util.Collections.sort(keyList, (k1, k2) -> {
                    if (k1.isUsed() != k2.isUsed()) {
                        return k1.isUsed() ? 1 : -1;
                    }
                    return Long.compare(k2.getCreatedAt(), k1.getCreatedAt());
                });
                adapter.notifyDataSetChanged();
            }
        });
    }
    private EnrollmentKey documentToEnrollmentKey(com.google.firebase.firestore.DocumentSnapshot doc) {
        EnrollmentKey key = new EnrollmentKey();
        key.setId(doc.getId());
        key.setKey(doc.getString("key"));
        key.setCourseId(doc.getString("courseId"));
        key.setCourseTitle(doc.getString("courseTitle"));
        key.setUsedBy(doc.getString("usedBy"));
        key.setCreatedBy(doc.getString("createdBy"));
        Boolean isUsed = doc.getBoolean("isUsed");
        if (isUsed == null) {
            isUsed = doc.getBoolean("used");
        }
        key.setUsed(isUsed != null ? isUsed : false);
        Long createdAt = doc.getLong("createdAt");
        key.setCreatedAt(createdAt != null ? createdAt : 0);
        Long usedAt = doc.getLong("usedAt");
        key.setUsedAt(usedAt);
        Long expiresAt = doc.getLong("expiresAt");
        key.setExpiresAt(expiresAt);
        return key;
    }
    private void deleteKey(EnrollmentKey key) {
        AdminFirestoreRepository.deleteKey(key.getId(), new AdminFirestoreRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Toast.makeText(AdminKeysActivity.this, "Key deleted", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onFailure(String error) {
                Toast.makeText(AdminKeysActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
