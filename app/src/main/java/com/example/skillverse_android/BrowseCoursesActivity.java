package com.example.skillverse_android;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.skillverse_android.adapters.CourseAdapter;
import com.example.skillverse_android.models.Course;
import com.example.skillverse_android.utils.EnrollmentManager;
import com.example.skillverse_android.utils.FirestoreRepository;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;
public class BrowseCoursesActivity extends AppCompatActivity {
    private RecyclerView rvCourses;
    private CourseAdapter adapter;
    private TextInputEditText etSearch;
    private List<Course> allCourses;
    private EnrollmentManager enrollmentManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setDecorFitsSystemWindows(false);
        setContentView(R.layout.activity_browse_courses);
        enrollmentManager = EnrollmentManager.getInstance(this);
        setupViews();
        setupRecyclerView();
        loadCourses();
        setupSearch();
    }
    private android.widget.AutoCompleteTextView actvCategory, actvTopic;
    private String selectedCategory = "";
    private String selectedTopic = "";
    private com.google.android.material.progressindicator.CircularProgressIndicator progressBar;

    private void setupViews() {
        progressBar = findViewById(R.id.progressBar);
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), v.getPaddingBottom());
            return WindowInsetsCompat.CONSUMED;
        });
        rvCourses = findViewById(R.id.rvCourses);
        etSearch = findViewById(R.id.etSearch);
        actvCategory = findViewById(R.id.actvCategory);
        actvTopic = findViewById(R.id.actvTopic);

        actvCategory.setOnItemClickListener((parent, view, position, id) -> {
            selectedCategory = parent.getItemAtPosition(position).toString();
            if (selectedCategory.equals("All Categories")) selectedCategory = "";
            filterCourses(etSearch.getText().toString());
        });

        actvTopic.setOnItemClickListener((parent, view, position, id) -> {
            selectedTopic = parent.getItemAtPosition(position).toString();
            if (selectedTopic.equals("All Topics")) selectedTopic = "";
            filterCourses(etSearch.getText().toString());
        });
    }

    private void setupRecyclerView() {
        adapter = new CourseAdapter(course -> {
            Intent intent = new Intent(BrowseCoursesActivity.this, CourseDetailActivity.class);
            int courseIdInt = 1;
            try {
                courseIdInt = Integer.parseInt(course.getId());
            } catch (NumberFormatException e) {
                courseIdInt = Math.abs(course.getId().hashCode());
            }
            intent.putExtra(CourseDetailActivity.EXTRA_COURSE_ID, courseIdInt);
            intent.putExtra(CourseDetailActivity.EXTRA_COURSE_DOC_ID, course.getId());
            intent.putExtra(CourseDetailActivity.EXTRA_COURSE_TITLE, course.getTitle());
            startActivity(intent);
        }, true);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 1);
        rvCourses.setLayoutManager(layoutManager);
        rvCourses.setAdapter(adapter);
    }

    private void loadCourses() {
        if (progressBar != null) progressBar.setVisibility(android.view.View.VISIBLE);
        FirestoreRepository.getCourses(new FirestoreRepository.DataCallback<List<Course>>() {
            @Override
            public void onSuccess(List<Course> courses) {
                if (progressBar != null) progressBar.setVisibility(android.view.View.GONE);
                android.util.Log.d("BrowseCourses", "Received " + courses.size() + " courses from Firestore");
                allCourses = new ArrayList<>();
                for (Course course : courses) {
                    boolean enrolled = enrollmentManager.isEnrolled(course.getId());
                    if (!enrolled && course.isPublished()) {
                        allCourses.add(course);
                    }
                }
                populateFilters(allCourses);
                adapter.setCourses(allCourses);
            }
            @Override
            public void onFailure(String error) {
                if (progressBar != null) progressBar.setVisibility(android.view.View.GONE);
                android.util.Log.e("BrowseCourses", "Failed to load courses: " + error);
                Toast.makeText(BrowseCoursesActivity.this,
                    "Failed to load courses: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void populateFilters(List<Course> courses) {
        java.util.Set<String> categories = new java.util.HashSet<>();
        java.util.Set<String> topics = new java.util.HashSet<>(); // Using Difficulty as Topic

        categories.add("All Categories");
        topics.add("All Topics");

        for (Course course : courses) {
            if (course.getCategory() != null && !course.getCategory().isEmpty()) {
                categories.add(course.getCategory());
            }
            if (course.getDifficulty() != null && !course.getDifficulty().isEmpty()) {
                topics.add(course.getDifficulty());
            }
        }

        android.widget.ArrayAdapter<String> categoryAdapter = new android.widget.ArrayAdapter<>(
            this, R.layout.item_dropdown, new ArrayList<>(categories));
        actvCategory.setAdapter(categoryAdapter);
        actvCategory.setText("All Categories", false);

        android.widget.ArrayAdapter<String> topicAdapter = new android.widget.ArrayAdapter<>(
            this, R.layout.item_dropdown, new ArrayList<>(topics));
        actvTopic.setAdapter(topicAdapter);
        actvTopic.setText("All Topics", false);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterCourses(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterCourses(String query) {
        String lowerQuery = query.toLowerCase();
        List<Course> filtered = new ArrayList<>();
        
        if (allCourses == null) return;

        for (Course course : allCourses) {
            boolean matchesSearch = query.isEmpty() ||
                course.getTitle().toLowerCase().contains(lowerQuery) ||
                course.getCategory().toLowerCase().contains(lowerQuery) ||
                course.getDifficulty().toLowerCase().contains(lowerQuery);
            
            boolean matchesCategory = selectedCategory.isEmpty() || 
                (course.getCategory() != null && course.getCategory().equals(selectedCategory));
                
            boolean matchesTopic = selectedTopic.isEmpty() || 
                (course.getDifficulty() != null && course.getDifficulty().equals(selectedTopic));

            if (matchesSearch && matchesCategory && matchesTopic) {
                filtered.add(course);
            }
        }
        adapter.setCourses(filtered);
    }
}
