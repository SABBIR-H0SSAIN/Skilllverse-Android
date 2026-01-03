package com.example.skillverse_android.admin;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.skillverse_android.R;
import com.example.skillverse_android.admin.adapters.AdminModuleAdapter;
import com.example.skillverse_android.admin.dialogs.AddModuleDialog;
import com.example.skillverse_android.admin.dialogs.EditModuleDialog;
import com.example.skillverse_android.admin.repository.AdminFirestoreRepository;
import com.example.skillverse_android.admin.utils.ConfirmationDialog;
import com.example.skillverse_android.models.Module;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
public class ManageModulesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AdminModuleAdapter adapter;
    private List<Module> moduleList = new ArrayList<>();
    private String courseId;
    private String courseTitle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_modules);
        courseId = getIntent().getStringExtra("courseId");
        courseTitle = getIntent().getStringExtra("courseTitle");
        if (courseId == null) {
            Toast.makeText(this, "Error: No course selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        try {
            MaterialToolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setTitle("Modules: " + (courseTitle != null ? courseTitle : "Course"));
                }
            }
            recyclerView = findViewById(R.id.recyclerView);
            FloatingActionButton fab = findViewById(R.id.fab);
            if (recyclerView != null) {
                setupRecyclerView();
            }
            if (fab != null) {
                fab.setOnClickListener(v -> {
                    AddModuleDialog dialog = AddModuleDialog.newInstance(courseId);
                    dialog.setOnModuleAddedListener(() -> {
                    });
                    dialog.show(getSupportFragmentManager(), "add_module");
                });
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private void setupRecyclerView() {
        adapter = new AdminModuleAdapter(moduleList, new AdminModuleAdapter.ModuleActionListener() {
            @Override
            public void onEdit(Module module, int position) {
                EditModuleDialog dialog = EditModuleDialog.newInstance(courseId, module, position);
                dialog.setOnModuleUpdatedListener(() -> {
                });
                dialog.show(getSupportFragmentManager(), "edit_module");
            }
            @Override
            public void onDelete(Module module, int position) {
                ConfirmationDialog.showDeleteConfirmation(ManageModulesActivity.this, "Module", () -> {
                    deleteModule(module, position);
                });
            }
            @Override
            public void onManageResources(Module module, int position) {
                AdminFirestoreRepository.getModulesForCourse(courseId, new AdminFirestoreRepository.DataCallback<List<Module>>() {
                    @Override
                    public void onSuccess(List<Module> modules) {
                         com.google.firebase.firestore.FirebaseFirestore.getInstance()
                            .collection("courses")
                            .document(courseId)
                            .collection("modules")
                            .orderBy("order", com.google.firebase.firestore.Query.Direction.ASCENDING)
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                if (position < queryDocumentSnapshots.size()) {
                                    String moduleId = queryDocumentSnapshots.getDocuments().get(position).getId();
                                    android.content.Intent intent = new android.content.Intent(ManageModulesActivity.this, ManageResourcesActivity.class);
                                    intent.putExtra("courseId", courseId);
                                    intent.putExtra("moduleId", moduleId);
                                    intent.putExtra("moduleTitle", module.getTitle());
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(ManageModulesActivity.this, "Error: Module not found", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(e -> Toast.makeText(ManageModulesActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                    @Override
                    public void onFailure(String error) {
                         Toast.makeText(ManageModulesActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
    private com.google.firebase.firestore.ListenerRegistration moduleListener;
    @Override
    protected void onStart() {
        super.onStart();
        startListening();
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (moduleListener != null) {
            moduleListener.remove();
            moduleListener = null;
        }
    }
    private void startListening() {
        if (moduleListener != null) return;
        moduleListener = AdminFirestoreRepository.listenToModules(courseId, (queryDocumentSnapshots, e) -> {
            if (e != null) {
                Toast.makeText(ManageModulesActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            if (queryDocumentSnapshots != null && adapter != null) {
                moduleList.clear();
                for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Module module = document.toObject(Module.class);
                    module.setDocumentId(document.getId());
                    moduleList.add(module);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }
    private void deleteModule(Module module, int position) {
        if (module.getDocumentId() == null || module.getDocumentId().isEmpty()) {
            Toast.makeText(this, "Error: Module ID is missing", Toast.LENGTH_SHORT).show();
            return;
        }
        AdminFirestoreRepository.deleteModule(courseId, module.getDocumentId(), new AdminFirestoreRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Toast.makeText(ManageModulesActivity.this, "Module deleted successfully", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onFailure(String error) {
                Toast.makeText(ManageModulesActivity.this, "Error deleting module: " + error, Toast.LENGTH_LONG).show();
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
