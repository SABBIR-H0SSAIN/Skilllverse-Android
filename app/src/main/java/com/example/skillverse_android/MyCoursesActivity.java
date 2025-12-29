package com.example.skillverse_android;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.skillverse_android.adapters.CourseAdapter;
import com.example.skillverse_android.models.Course;
import com.example.skillverse_android.utils.EnrollmentManager;
import com.example.skillverse_android.utils.FirebaseAuthManager;
import com.example.skillverse_android.utils.FirestoreRepository;
import com.example.skillverse_android.utils.FirestoreRepository;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.ArrayList;
import java.util.List;
public class MyCoursesActivity extends AppCompatActivity {
    private RecyclerView rvMyCourses;
    private LinearLayout llEmptyState;
    private CourseAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setDecorFitsSystemWindows(false);
        setContentView(R.layout.activity_my_courses);
        setupViews();
        loadCourses();
    }
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
        rvMyCourses = findViewById(R.id.rvMyCourses);
        llEmptyState = findViewById(R.id.tvEmptyState);
        adapter = new CourseAdapter(course -> {});
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        rvMyCourses.setLayoutManager(layoutManager);
        rvMyCourses.setAdapter(adapter);
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
}
