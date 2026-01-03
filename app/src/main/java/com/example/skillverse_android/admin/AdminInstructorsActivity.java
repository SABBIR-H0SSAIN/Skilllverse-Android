package com.example.skillverse_android.admin;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.skillverse_android.R;
import com.example.skillverse_android.admin.adapters.AdminInstructorAdapter;
import com.example.skillverse_android.admin.repository.AdminFirestoreRepository;
import com.example.skillverse_android.admin.utils.ConfirmationDialog;
import com.example.skillverse_android.models.Instructor;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
public class AdminInstructorsActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AdminInstructorAdapter adapter;
    private List<Instructor> instructorList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_instructors);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Manage Instructors");
            }
        }
        recyclerView = findViewById(R.id.recyclerView);
        FloatingActionButton fab = findViewById(R.id.fab);
        if (recyclerView != null) {
            setupRecyclerView();
        }
        if (fab != null) {
            fab.setOnClickListener(v -> {
                com.example.skillverse_android.admin.dialogs.AddInstructorDialog dialog = com.example.skillverse_android.admin.dialogs.AddInstructorDialog.newInstance();
                dialog.setOnInstructorAddedListener(instructor -> {
                });
                dialog.show(getSupportFragmentManager(), "add_instructor");
            });
        }
    }
    private void setupRecyclerView() {
        adapter = new AdminInstructorAdapter(instructorList, new AdminInstructorAdapter.InstructorActionListener() {
            @Override
            public void onEdit(Instructor instructor) {
                com.example.skillverse_android.admin.dialogs.EditInstructorDialog dialog = com.example.skillverse_android.admin.dialogs.EditInstructorDialog.newInstance(instructor);
                dialog.setOnInstructorUpdatedListener(() -> {
                });
                dialog.show(getSupportFragmentManager(), "edit_instructor");
            }
            @Override
            public void onDelete(Instructor instructor) {
                ConfirmationDialog.showDeleteConfirmation(AdminInstructorsActivity.this, "Instructor", () -> {
                    deleteInstructor(instructor);
                });
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
    private com.google.firebase.firestore.ListenerRegistration instructorListener;
    @Override
    protected void onStart() {
        super.onStart();
        startListening();
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (instructorListener != null) {
            instructorListener.remove();
            instructorListener = null;
        }
    }
    private void startListening() {
        if (instructorListener != null) return;
        instructorListener = AdminFirestoreRepository.listenToInstructors((queryDocumentSnapshots, e) -> {
            if (e != null) {
                Toast.makeText(AdminInstructorsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            if (queryDocumentSnapshots != null && adapter != null) {
                instructorList.clear();
                for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Instructor instructor = document.toObject(Instructor.class);
                    instructor.setId(document.getId());
                    instructorList.add(instructor);
                }
                adapter.notifyDataSetChanged();
            }
        });
    }
    private void deleteInstructor(Instructor instructor) {
        AdminFirestoreRepository.deleteInstructor(instructor.getId(), new AdminFirestoreRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Toast.makeText(AdminInstructorsActivity.this, "Instructor deleted", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onFailure(String error) {
                Toast.makeText(AdminInstructorsActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
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
