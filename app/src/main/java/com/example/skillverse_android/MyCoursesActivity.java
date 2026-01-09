package com.example.skillverse_android;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.skillverse_android.adapters.CourseAdapter;
import com.example.skillverse_android.models.Course;
import com.example.skillverse_android.utils.EnrollmentManager;
import com.example.skillverse_android.utils.FirebaseAuthManager;
import com.example.skillverse_android.utils.FirestoreRepository;
import com.example.skillverse_android.utils.SwipeGestureHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import java.util.ArrayList;
import java.util.List;
public class MyCoursesActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private RecyclerView rvMyCourses;
    private LinearLayout llEmptyState;
    private CourseAdapter adapter;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private SwipeGestureHelper swipeHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setDecorFitsSystemWindows(false);
        setContentView(R.layout.activity_my_courses);
        setupViews();
        setupNavigationDrawer();
        loadCourses();
    }
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
        rvMyCourses = findViewById(R.id.rvMyCourses);
        llEmptyState = findViewById(R.id.tvEmptyState);
        adapter = new CourseAdapter(course -> {});
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvMyCourses.setLayoutManager(layoutManager);
        rvMyCourses.setAdapter(adapter);
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
            startActivity(new Intent(this, BrowseCoursesActivity.class));
        } else if (id == R.id.nav_my_courses) {
             
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

    private void loadCourses() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        rvMyCourses.setVisibility(View.GONE);
        llEmptyState.setVisibility(View.GONE);
        EnrollmentManager enrollmentManager = EnrollmentManager.getInstance(this);
        String userId = FirebaseAuthManager.getCurrentUser().getUid();
        FirestoreRepository.getUserEnrollments(userId, new FirestoreRepository.DataCallback<List<Course>>() {
            @Override
            public void onSuccess(List<Course> enrolledCourses) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                if (enrolledCourses.isEmpty()) {
                    rvMyCourses.setVisibility(View.GONE);
                    llEmptyState.setVisibility(View.VISIBLE);
                } else {
                    rvMyCourses.setVisibility(View.VISIBLE);
                    calculateProgressForCourses(enrolledCourses);
                }
            }
            @Override
            public void onFailure(String error) {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                Toast.makeText(MyCoursesActivity.this,
                    "Failed to load courses: " + error, Toast.LENGTH_LONG).show();
                rvMyCourses.setVisibility(View.GONE);
                llEmptyState.setVisibility(View.VISIBLE);
            }
        });
    }
    private void calculateProgressForCourses(List<Course> courses) {
        String userId = FirebaseAuthManager.getCurrentUser().getUid();
        for (int i = 0; i < courses.size(); i++) {
            Course course = courses.get(i);
            int courseIdInt = 1;
            try {
                courseIdInt = Integer.parseInt(course.getId());
            } catch (NumberFormatException e) {
                if (course.getId() != null) {
                    courseIdInt = Math.abs(course.getId().hashCode());
                }
            }
            String courseIdStr = course.getId() != null ? course.getId() : String.valueOf(courseIdInt);
            FirestoreRepository.getCourseProgress(userId, courseIdStr, new FirestoreRepository.DataCallback<Integer>() {
                @Override
                public void onSuccess(Integer progress) {
                    course.setProgress(progress);
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                }
                @Override
                public void onFailure(String error) {
                    course.setProgress(0);
                }
            });
        }
        rvMyCourses.setVisibility(View.VISIBLE);
        llEmptyState.setVisibility(View.GONE);
        adapter = new CourseAdapter(course -> {
            Intent intent = new Intent(MyCoursesActivity.this, CourseDetailActivity.class);
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
        });
        adapter.setShowProgress(true);
        adapter.setCourses(courses);
        rvMyCourses.setAdapter(adapter);
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadCourses();
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
