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
    private android.widget.AutoCompleteTextView actvCourse;
    private List<String> courseTitles = new ArrayList<>();
    private String currentFilterCourseTitle = "All Courses";
    private List<EnrollmentKey> allKeys = new ArrayList<>();  
    private RecyclerView recyclerView;
    private AdminKeyAdapter adapter;
    private List<Object> itemList = new ArrayList<>();

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
            actvCourse = findViewById(R.id.actvCourse);
            FloatingActionButton fab = findViewById(R.id.fab);

            if (recyclerView != null) {
                setupRecyclerView();
            }

            if (actvCourse != null) {
                setupCourseFilter();
            }

            if (fab != null) {
                fab.setOnClickListener(v -> {
                    GenerateKeysDialog dialog = new GenerateKeysDialog();
                    dialog.setOnKeysGeneratedListener((keys) -> {
                        showGeneratedKeysDialog(keys);
                    });
                    dialog.show(getSupportFragmentManager(), "generate_keys");
                });
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void showGeneratedKeysDialog(List<EnrollmentKey> keys) {
        if (keys == null || keys.isEmpty()) return;

        android.app.Dialog dialog = new android.app.Dialog(this);
        dialog.setContentView(R.layout.dialog_generated_keys);
        dialog.getWindow().setLayout(
            android.view.ViewGroup.LayoutParams.MATCH_PARENT,
            android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        );

        android.widget.TextView tvGeneratedKeys = dialog.findViewById(R.id.tvGeneratedKeys);
        com.google.android.material.button.MaterialButton btnCopyAll = dialog.findViewById(R.id.btnCopyAll);

        StringBuilder sb = new StringBuilder();
        for (EnrollmentKey key : keys) {
            sb.append(key.getKey()).append("\n");
        }
        tvGeneratedKeys.setText(sb.toString().trim());

        btnCopyAll.setOnClickListener(v -> {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(android.content.Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Generated Keys", sb.toString());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "All keys copied to clipboard", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void setupCourseFilter() {
        courseTitles.add("All Courses");
        android.widget.ArrayAdapter<String> adapter = new android.widget.ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, courseTitles);
        actvCourse.setAdapter(adapter);
        
         
        AdminFirestoreRepository.getAllCourses(new AdminFirestoreRepository.DataCallback<List<com.example.skillverse_android.models.Course>>() {
            @Override
            public void onSuccess(List<com.example.skillverse_android.models.Course> courses) {
                courseTitles.clear();
                courseTitles.add("All Courses");
                for (com.example.skillverse_android.models.Course course : courses) {
                    if (course.getTitle() != null) {
                        courseTitles.add(course.getTitle());
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(AdminKeysActivity.this, "Failed to load courses for filter", Toast.LENGTH_SHORT).show();
            }
        });

        actvCourse.setOnItemClickListener((parent, view, position, id) -> {
            currentFilterCourseTitle = parent.getItemAtPosition(position).toString();
            filterKeys();
        });
    }

    private void setupRecyclerView() {
        adapter = new AdminKeyAdapter(itemList, key -> {
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
                allKeys.clear();
                for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    EnrollmentKey key = documentToEnrollmentKey(document);
                    if (key != null) {
                        allKeys.add(key);
                    }
                }
                filterKeys();
            }
        });
    }

    private void filterKeys() {
        List<EnrollmentKey> filteredKeys = new ArrayList<>();
        if ("All Courses".equals(currentFilterCourseTitle)) {
            filteredKeys.addAll(allKeys);
        } else {
            for (EnrollmentKey key : allKeys) {
                if (currentFilterCourseTitle.equals(key.getCourseTitle())) {
                    filteredKeys.add(key);
                }
            }
        }

        List<EnrollmentKey> unusedKeys = new ArrayList<>();
        List<EnrollmentKey> usedKeys = new ArrayList<>();

        for (EnrollmentKey key : filteredKeys) {
            if (key.isUsed()) {
                usedKeys.add(key);
            } else {
                unusedKeys.add(key);
            }
        }

         
        java.util.Collections.sort(unusedKeys, (k1, k2) -> Long.compare(k2.getCreatedAt(), k1.getCreatedAt()));
         
        java.util.Collections.sort(usedKeys, (k1, k2) -> Long.compare(k2.getCreatedAt(), k1.getCreatedAt()));

        itemList.clear();
        itemList.addAll(unusedKeys);
        
        if (!usedKeys.isEmpty()) {
            itemList.add("Used Keys");
            itemList.addAll(usedKeys);
        }

        adapter.notifyDataSetChanged();
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
