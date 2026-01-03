package com.example.skillverse_android.admin;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.skillverse_android.R;
import com.example.skillverse_android.admin.adapters.AdminResourceAdapter;
import com.example.skillverse_android.admin.dialogs.AddResourceDialog;
import com.example.skillverse_android.admin.dialogs.EditResourceDialog;
import com.example.skillverse_android.admin.repository.AdminFirestoreRepository;
import com.example.skillverse_android.admin.utils.ConfirmationDialog;
import com.example.skillverse_android.models.ModuleResource;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
public class ManageResourcesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AdminResourceAdapter adapter;
    private List<ModuleResource> resourceList = new ArrayList<>();
    private String courseId;
    private String moduleId;
    private String moduleTitle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_resources);
        courseId = getIntent().getStringExtra("courseId");
        moduleId = getIntent().getStringExtra("moduleId");
        moduleTitle = getIntent().getStringExtra("moduleTitle");
        if (courseId == null || moduleId == null) {
            Toast.makeText(this, "Error: Missing course or module ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Resources: " + (moduleTitle != null ? moduleTitle : "Module"));
        }
        recyclerView = findViewById(R.id.recyclerView);
        FloatingActionButton fab = findViewById(R.id.fab);
        setupRecyclerView();
        fab.setOnClickListener(v -> {
            AddResourceDialog dialog = AddResourceDialog.newInstance(courseId, moduleId);
            dialog.setOnResourceAddedListener(() -> {
            });
            dialog.show(getSupportFragmentManager(), "add_resource");
        });
    }
    private void setupRecyclerView() {
        adapter = new AdminResourceAdapter(resourceList, new AdminResourceAdapter.ResourceActionListener() {
            @Override
            public void onEdit(ModuleResource resource, int position) {
                EditResourceDialog dialog = EditResourceDialog.newInstance(courseId, moduleId, resource, position);
                dialog.setOnResourceUpdatedListener(() -> {
                });
                dialog.show(getSupportFragmentManager(), "edit_resource");
            }
            @Override
            public void onDelete(ModuleResource resource, int position) {
                ConfirmationDialog.showDeleteConfirmation(ManageResourcesActivity.this, "Resource", () -> {
                    deleteResource(resource);
                });
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
    private com.google.firebase.firestore.ListenerRegistration resourceListener;
    @Override
    protected void onStart() {
        super.onStart();
        startListening();
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (resourceListener != null) {
            resourceListener.remove();
            resourceListener = null;
        }
    }
    private void startListening() {
        if (resourceListener != null) return;
        resourceListener = AdminFirestoreRepository.listenToResourcesForModule(courseId, moduleId, (queryDocumentSnapshots, e) -> {
            if (e != null) {
                Toast.makeText(ManageResourcesActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            if (queryDocumentSnapshots != null && adapter != null) {
                resourceList.clear();
                for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    ModuleResource resource = document.toObject(ModuleResource.class);
                    resource.setDocumentId(document.getId());
                    resourceList.add(resource);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }
    private void deleteResource(ModuleResource resource) {
        AdminFirestoreRepository.deleteResourceFromModule(courseId, moduleId, resource.getDocumentId(), new AdminFirestoreRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Toast.makeText(ManageResourcesActivity.this, "Resource deleted", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onFailure(String error) {
                Toast.makeText(ManageResourcesActivity.this, "Error deleting resource: " + error, Toast.LENGTH_LONG).show();
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
