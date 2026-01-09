package com.example.skillverse_android;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.skillverse_android.adapters.ModuleListAdapter;
import com.example.skillverse_android.models.Course;
import com.example.skillverse_android.models.Module;
import com.example.skillverse_android.utils.EnrollmentManager;
import com.example.skillverse_android.utils.FirebaseAuthManager;
import com.example.skillverse_android.utils.SwipeGestureHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import java.util.List;
public class CourseDetailActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static final String EXTRA_COURSE_ID = "course_id";
    public static final String EXTRA_COURSE_DOC_ID = "course_doc_id";
    public static final String EXTRA_COURSE_TITLE = "course_title";
    private TextView tvCourseTitle, tvCourseInstructor, tvModuleCount;
    private RecyclerView rvModules;
    private MaterialButton btnEnrollNow;
    private ModuleListAdapter adapter;
    private int courseId;
    private String courseDocId;
    private String courseTitle;
    private Course currentCourse;
    private EnrollmentManager enrollmentManager;
    private boolean isEnrolled = false;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private SwipeGestureHelper swipeHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setDecorFitsSystemWindows(false);
        setContentView(R.layout.activity_course_detail);
        enrollmentManager = EnrollmentManager.getInstance(this);
        setupViews();
        setupNavigationDrawer();
        loadCourseData();
        checkEnrollmentStatus();
        loadModules();
    }
    private void setupViews() {
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
        tvCourseTitle = findViewById(R.id.tvCourseTitle);
        tvCourseInstructor = findViewById(R.id.tvCourseInstructor);
        tvModuleCount = findViewById(R.id.tvModuleCount);
        rvModules = findViewById(R.id.rvModules);
        btnEnrollNow = findViewById(R.id.btnEnrollNow);
        rvModules.setLayoutManager(new LinearLayoutManager(this));
        btnEnrollNow.setOnClickListener(v -> showEnrollmentDialog());
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

    @Override
    public boolean dispatchTouchEvent(android.view.MotionEvent ev) {
        if (swipeHelper != null && swipeHelper.onTouchEvent(ev)) {
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    private void loadCourseData() {
        Intent intent = getIntent();
        courseId = intent.getIntExtra(EXTRA_COURSE_ID, 1);
        courseDocId = intent.getStringExtra(EXTRA_COURSE_DOC_ID);
        courseTitle = intent.getStringExtra(EXTRA_COURSE_TITLE);
        if (courseDocId == null || courseDocId.isEmpty()) {
            courseDocId = String.valueOf(courseId);
        }
        tvCourseTitle.setText(courseTitle != null ? courseTitle : "Course");
        tvCourseInstructor.setText("Loading...");
        com.example.skillverse_android.utils.FirestoreRepository.getCourse(
            courseDocId,
            new com.example.skillverse_android.utils.FirestoreRepository.DataCallback<Course>() {
                @Override
                public void onSuccess(Course course) {
                    currentCourse = course;
                    if (course.getTitle() != null && !course.getTitle().isEmpty()) {
                        tvCourseTitle.setText(course.getTitle());
                    }
                    if (!course.getInstructorIds().isEmpty()) {
                        loadInstructorNames(course.getInstructorIds());
                    } else {
                        tvCourseInstructor.setText("By Instructor");
                    }
                }
                @Override
                public void onFailure(String error) {
                    tvCourseInstructor.setText("By Instructor");
                }
            }
        );
    }
    private void loadInstructorNames(java.util.List<String> instructorIds) {
        com.example.skillverse_android.utils.FirestoreRepository.getInstructorsForCourse(
            instructorIds,
            new com.example.skillverse_android.utils.FirestoreRepository.DataCallback<java.util.List<com.example.skillverse_android.models.Instructor>>() {
                @Override
                public void onSuccess(java.util.List<com.example.skillverse_android.models.Instructor> instructors) {
                    displayInstructors(instructors);
                }
                @Override
                public void onFailure(String error) {
                    tvCourseInstructor.setText("By Instructor");
                }
            }
        );
    }
    private void displayInstructors(java.util.List<com.example.skillverse_android.models.Instructor> instructors) {
        if (instructors.isEmpty()) {
            tvCourseInstructor.setText("By Instructor");
            return;
        }
        StringBuilder instructorNames = new StringBuilder("By ");
        for (int i = 0; i < instructors.size(); i++) {
            instructorNames.append(instructors.get(i).getName());
            if (i < instructors.size() - 1) {
                instructorNames.append(", ");
            }
        }
        tvCourseInstructor.setText(instructorNames.toString());
    }
    private void checkEnrollmentStatus() {
        isEnrolled = enrollmentManager.isEnrolled(courseDocId);
        updateEnrollButton();
        String userId = com.example.skillverse_android.utils.FirebaseAuthManager.getCurrentUser().getUid();
        com.example.skillverse_android.utils.FirestoreRepository.isEnrolled(
            userId,
            courseDocId,
            new com.example.skillverse_android.utils.FirestoreRepository.DataCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean enrolled) {
                    isEnrolled = enrolled;
                    if (enrolled && !enrollmentManager.isEnrolled(courseDocId)) {
                        enrollmentManager.enrollInCourse(courseDocId);
                    } else if (!enrolled && enrollmentManager.isEnrolled(courseDocId)) {
                        enrollmentManager.unenrollFromCourse(courseDocId);
                    }
                    updateEnrollButton();
                }
                @Override
                public void onFailure(String error) {
                }
            }
        );
    }
    private void updateEnrollButton() {
        if (isEnrolled) {
            btnEnrollNow.setVisibility(View.GONE);
        } else {
            btnEnrollNow.setVisibility(View.VISIBLE);
        }
    }
    private void showEnrollmentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_enrollment, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        EditText etEnrollmentKey = dialogView.findViewById(R.id.etEnrollmentKey);
        Button btnSubmitKey = dialogView.findViewById(R.id.btnSubmitKey);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        btnSubmitKey.setOnClickListener(v -> {
            String enteredKey = etEnrollmentKey.getText().toString().trim();
            if (enteredKey.isEmpty()) {
                Toast.makeText(this, "Please enter an enrollment key", Toast.LENGTH_SHORT).show();
                return;
            }
            btnSubmitKey.setEnabled(false);
            btnSubmitKey.setText("Validating...");
            String userId = com.example.skillverse_android.utils.FirebaseAuthManager.getCurrentUser().getUid();
            com.example.skillverse_android.utils.FirestoreRepository.validateAndRedeemEnrollmentKey(
                userId,
                enteredKey,
                new com.example.skillverse_android.utils.FirestoreRepository.DataCallback<String>() {
                    @Override
                    public void onSuccess(String courseId) {
                        enrollmentManager.enrollInCourse(courseId);
                        isEnrolled = true;
                        btnEnrollNow.setVisibility(View.GONE);
                        Toast.makeText(CourseDetailActivity.this,
                            "✅ Successfully enrolled!", Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                        loadModules();
                    }
                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(CourseDetailActivity.this,
                            "❌ " + error, Toast.LENGTH_LONG).show();
                        btnSubmitKey.setEnabled(true);
                        btnSubmitKey.setText("Submit");
                    }
                }
            );
        });
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
    private void loadModules() {
        com.example.skillverse_android.utils.FirestoreRepository.getModulesForCourse(
            courseDocId,
            new com.example.skillverse_android.utils.FirestoreRepository.DataCallback<List<Module>>() {
                @Override
                public void onSuccess(List<Module> firestoreModules) {
                    tvModuleCount.setText(firestoreModules.size() + " Modules");
                    String userId = com.example.skillverse_android.utils.FirebaseAuthManager.getCurrentUser().getUid();
                    loadModuleCompletionStatus(firestoreModules, userId);
                }
                @Override
                public void onFailure(String error) {
                    Toast.makeText(CourseDetailActivity.this,
                        "Failed to load modules: " + error, Toast.LENGTH_SHORT).show();
                    tvModuleCount.setText("0 Modules");
                }
            }
        );
    }
    private void loadModuleCompletionStatus(List<Module> modules, String userId) {
        com.example.skillverse_android.utils.ProgressCacheManager cacheManager =
            com.example.skillverse_android.utils.ProgressCacheManager.getInstance(this);
        for (int i = 0; i < modules.size(); i++) {
            Module module = modules.get(i);
            String modId = module.getDocumentId() != null ? module.getDocumentId() : String.valueOf(module.getId());
            Boolean cachedStatus = cacheManager.getCachedModuleProgress(userId, courseDocId, modId);
            if (cachedStatus != null) {
                module.setCompleted(cachedStatus);
            } else {
                module.setCompleted(false);
            }
            com.example.skillverse_android.utils.FirestoreRepository.getModuleProgress(
                userId,
                courseDocId,
                modId,
                new com.example.skillverse_android.utils.FirestoreRepository.DataCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean completed) {
                        cacheManager.cacheModuleProgress(userId, courseDocId, modId, completed);
                        if (module.isCompleted() != completed) {
                            module.setCompleted(completed);
                            if (adapter != null) {
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                    @Override
                    public void onFailure(String error) {
                    }
                }
            );
        }
        adapter = new ModuleListAdapter(modules, module -> {
            if (isEnrolled) {
                Intent intent = new Intent(CourseDetailActivity.this, ModuleDetailActivity.class);
                intent.putExtra(ModuleDetailActivity.EXTRA_COURSE_ID, courseId);
                intent.putExtra(ModuleDetailActivity.EXTRA_COURSE_DOC_ID, courseDocId);
                intent.putExtra(ModuleDetailActivity.EXTRA_COURSE_TITLE, courseTitle);
                if (module.getDocumentId() != null) {
                    intent.putExtra(ModuleDetailActivity.EXTRA_MODULE_ID, module.getDocumentId());
                } else {
                    intent.putExtra(ModuleDetailActivity.EXTRA_MODULE_ID, module.getId());
                }
                startActivity(intent);
            } else {
                Toast.makeText(this, "🔒 Please enroll to access modules", Toast.LENGTH_SHORT).show();
                showEnrollmentDialog();
            }
        });
        rvModules.setAdapter(adapter);
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadModules();
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
