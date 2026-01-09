package com.example.skillverse_android;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.skillverse_android.adapters.CourseAdapter;
import com.example.skillverse_android.models.Course;
import com.example.skillverse_android.utils.EnrollmentManager;
import com.example.skillverse_android.utils.FirebaseAuthManager;
import com.example.skillverse_android.utils.FirestoreRepository;
import com.example.skillverse_android.utils.SwipeGestureHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import java.util.ArrayList;
import java.util.List;
public class BrowseCoursesActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private RecyclerView rvCourses;
    private CourseAdapter adapter;
    private TextInputEditText etSearch;
    private List<Course> allCourses;
    private EnrollmentManager enrollmentManager;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private SwipeGestureHelper swipeHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setDecorFitsSystemWindows(false);
        setContentView(R.layout.activity_browse_courses);
        enrollmentManager = EnrollmentManager.getInstance(this);
        setupViews();
        setupNavigationDrawer();
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
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
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

    private void setupNavigationDrawer() {
        navigationView.setItemIconTintList(null);
        navigationView.setNavigationItemSelectedListener(this);
        ViewCompat.setOnApplyWindowInsetsListener(navigationView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            View headerView = navigationView.getHeaderView(0);
            headerView.setPadding(
                headerView.getPaddingLeft(),
                systemBars.top + 24,
                headerView.getPaddingRight(),
                headerView.getPaddingBottom()
            );
            return insets;
        });

        updateNavHeader();

        swipeHelper = new SwipeGestureHelper(this, () -> {
            if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    private void updateNavHeader() {
        if (FirebaseAuthManager.getCurrentUser() == null) return;
        String userEmail = FirebaseAuthManager.getCurrentUser().getEmail();
        String userId = FirebaseAuthManager.getCurrentUser().getUid();
        View headerView = navigationView.getHeaderView(0);
        TextView navHeaderName = headerView.findViewById(R.id.nav_header_name);
        TextView navHeaderEmail = headerView.findViewById(R.id.nav_header_email);
        navHeaderEmail.setText(userEmail != null ? userEmail : "");
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                String userName;
                if (documentSnapshot.exists() && documentSnapshot.contains("name")) {
                    userName = documentSnapshot.getString("name");
                } else {
                    userName = FirebaseAuthManager.getCurrentUser().getDisplayName();
                }
                if (userName == null || userName.isEmpty()) {
                    userName = userEmail != null ? userEmail.split("@")[0] : "User";
                }
                navHeaderName.setText(userName);
            });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_dashboard) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else if (id == R.id.nav_browse_courses) {
             
        } else if (id == R.id.nav_my_courses) {
            startActivity(new Intent(this, MyCoursesActivity.class));
        } else if (id == R.id.nav_certificates) {
            startActivity(new Intent(this, MyCertificatesActivity.class));
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
        } else if (id == R.id.nav_logout) {
            handleLogout();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void handleLogout() {
        FirebaseAuthManager.logoutUser();
        SharedPreferences prefs = getSharedPreferences("SkillVersePrefs", MODE_PRIVATE);
        prefs.edit().clear().apply();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
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
        java.util.Set<String> topics = new java.util.HashSet<>();

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

    @Override
    public boolean dispatchTouchEvent(android.view.MotionEvent ev) {
        if (swipeHelper != null && swipeHelper.onTouchEvent(ev)) {
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
