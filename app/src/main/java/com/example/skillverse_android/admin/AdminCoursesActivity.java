package com.example.skillverse_android.admin;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.skillverse_android.R;
import com.example.skillverse_android.admin.adapters.AdminCourseAdapter;
import com.example.skillverse_android.admin.dialogs.AddCourseDialog;
import com.example.skillverse_android.admin.dialogs.EditCourseDialog;
import com.example.skillverse_android.admin.repository.AdminFirestoreRepository;
import com.example.skillverse_android.admin.utils.ConfirmationDialog;
import com.example.skillverse_android.models.Course;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
public class AdminCoursesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AdminCourseAdapter adapter;
    private List<Course> courseList = new ArrayList<>();
    private List<Course> filteredList = new ArrayList<>();
    private com.google.firebase.firestore.ListenerRegistration courseListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_courses);
        try {
            MaterialToolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    getSupportActionBar().setTitle("Manage Courses");
                }
            }
            recyclerView = findViewById(R.id.recyclerView);
            FloatingActionButton fab = findViewById(R.id.fab);
            if (recyclerView != null) {
                setupRecyclerView();
            }
            if (fab != null) {
                fab.setOnClickListener(v -> {
                    AddCourseDialog dialog = new AddCourseDialog();
                    dialog.setOnCourseAddedListener(() -> {});
                    dialog.show(getSupportFragmentManager(), "add_course");
                });
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private void setupRecyclerView() {
        adapter = new AdminCourseAdapter(filteredList, new AdminCourseAdapter.CourseActionListener() {
            @Override
            public void onEdit(Course course) {
                EditCourseDialog dialog = EditCourseDialog.newInstance(course);
                dialog.setOnCourseUpdatedListener(() -> {});
                dialog.show(getSupportFragmentManager(), "edit_course");
            }
            @Override
            public void onDelete(Course course) {
                ConfirmationDialog.showDeleteConfirmation(AdminCoursesActivity.this, "Course", () -> {
                    deleteCourse(course);
                });
            }
            @Override
            public void onManageModules(Course course) {
                android.content.Intent intent = new android.content.Intent(AdminCoursesActivity.this, ManageModulesActivity.class);
                intent.putExtra("courseId", course.getId());
                intent.putExtra("courseTitle", course.getTitle());
                startActivity(intent);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
    @Override
    protected void onStart() {
        super.onStart();
        startListening();
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (courseListener != null) {
            courseListener.remove();
            courseListener = null;
        }
    }
    private void startListening() {
        if (courseListener != null) return;
        courseListener = AdminFirestoreRepository.listenToCourses((queryDocumentSnapshots, e) -> {
            if (e != null) {
                android.util.Log.e("AdminCourses", "Listener error: " + e.getMessage(), e);
                Toast.makeText(AdminCoursesActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return;
            }
            if (queryDocumentSnapshots != null && adapter != null) {
                android.util.Log.d("AdminCourses", "Received " + queryDocumentSnapshots.size() + " documents");
                courseList.clear();
                for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    try {
                        Course course = documentToCourse(document);
                        if (course != null) {
                            courseList.add(course);
                            android.util.Log.d("AdminCourses", "Successfully parsed: " + course.getTitle());
                        }
                    } catch (Exception ex) {
                        android.util.Log.e("AdminCourses", "Error parsing course " + document.getId() + ": " + ex.getMessage(), ex);
                    }
                }
                android.util.Log.d("AdminCourses", "Total courses loaded: " + courseList.size());
                filteredList.clear();
                filteredList.addAll(courseList);
                adapter.notifyDataSetChanged();
            }
        });
    }
    private Course documentToCourse(com.google.firebase.firestore.DocumentSnapshot doc) {
        Course course = new Course();
        course.setId(doc.getId());
        course.setTitle(doc.getString("title"));
        course.setDescription(doc.getString("description"));
        course.setCategory(doc.getString("category"));
        course.setDifficulty(doc.getString("difficulty"));
        course.setImageUrl(doc.getString("imageUrl"));
        course.setEnrollmentKey(doc.getString("enrollmentKey"));
        course.setDuration(parseIntField(doc.get("duration")));
        course.setLessonsCount(parseIntField(doc.get("lessonsCount")));
        course.setEnrollmentCount(parseIntField(doc.get("enrollmentCount")));
        Double rating = doc.getDouble("rating");
        course.setRating(rating != null ? rating.floatValue() : 0.0f);
        Double price = doc.getDouble("price");
        course.setPrice(price != null ? price : 0.0);
        Boolean published = doc.getBoolean("published");
        course.setPublished(published != null ? published : false);
        Long createdAt = doc.getLong("createdAt");
        course.setCreatedAt(createdAt);
        Long updatedAt = doc.getLong("updatedAt");
        course.setUpdatedAt(updatedAt);
        java.util.List<String> instructorIds = (java.util.List<String>) doc.get("instructorIds");
        course.setInstructorIds(instructorIds != null ? instructorIds : new java.util.ArrayList<>());
        return course;
    }
    private int parseIntField(Object value) {
        if (value == null) return 0;
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
    private void deleteCourse(Course course) {
        AdminFirestoreRepository.deleteCourse(course.getId(), new AdminFirestoreRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Toast.makeText(AdminCoursesActivity.this, "Course deleted", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onFailure(String error) {
                Toast.makeText(AdminCoursesActivity.this, "Error deleting course: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_admin_search, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                filterCourses(newText);
                return true;
            }
        });
        return true;
    }
    private void filterCourses(String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(courseList);
        } else {
            for (Course course : courseList) {
                if (course.getTitle().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(course);
                }
            }
        }
        adapter.notifyDataSetChanged();
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
