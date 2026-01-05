package com.example.skillverse_android.admin;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.skillverse_android.R;
import com.example.skillverse_android.admin.adapters.AdminUserAdapter;
import com.example.skillverse_android.admin.repository.AdminFirestoreRepository;
import com.example.skillverse_android.admin.utils.ConfirmationDialog;
import com.example.skillverse_android.models.User;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.ArrayList;
import java.util.List;
public class AdminUsersActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AdminUserAdapter adapter;
    private List<User> userList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_users);
        try {
            MaterialToolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setTitle("Manage Users");
                }
            }
            recyclerView = findViewById(R.id.recyclerView);
            if (recyclerView != null) {
                setupRecyclerView();
                loadUsers();
            } else {
                Toast.makeText(this, "Error loading layout", Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }
    private void setupRecyclerView() {
        try {
            adapter = new AdminUserAdapter(userList, new AdminUserAdapter.UserActionListener() {
                @Override
                public void onChangeRole(User user) {
                    String newRole = user.isAdmin() ? "student" : "admin";
                    ConfirmationDialog.show(AdminUsersActivity.this,
                        "Change Role",
                        "Change " + user.getName() + "'s role to " + newRole + "?",
                        () -> changeUserRole(user, newRole));
                }
                @Override
                public void onDelete(User user) {
                    ConfirmationDialog.showDeleteConfirmation(AdminUsersActivity.this, "User", () -> {
                        deleteUser(user);
                    });
                }
            });
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(adapter);
        } catch (Exception e) {
            Toast.makeText(this, "Error setting up list: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadUsers();
    }
    private void loadUsers() {
        AdminFirestoreRepository.getAllUsers(new AdminFirestoreRepository.DataCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> data) {
                userList.clear();
                userList.addAll(data);
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onFailure(String error) {
                Toast.makeText(AdminUsersActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void changeUserRole(User user, String newRole) {
        AdminFirestoreRepository.updateUserRole(user.getUserId(), newRole, new AdminFirestoreRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Toast.makeText(AdminUsersActivity.this, "Role updated", Toast.LENGTH_SHORT).show();
                loadUsers();
            }
            @Override
            public void onFailure(String error) {
                Toast.makeText(AdminUsersActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void deleteUser(User user) {
        AdminFirestoreRepository.deleteUser(user.getUserId(), new AdminFirestoreRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Toast.makeText(AdminUsersActivity.this, "User deleted", Toast.LENGTH_SHORT).show();
                loadUsers();
            }
            @Override
            public void onFailure(String error) {
                Toast.makeText(AdminUsersActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
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
