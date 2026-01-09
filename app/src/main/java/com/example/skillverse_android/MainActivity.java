package com.example.skillverse_android;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
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
import com.example.skillverse_android.utils.EnrollmentManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;
import com.example.skillverse_android.utils.SwipeGestureHelper;
import com.google.android.material.navigation.NavigationView;
import java.util.ArrayList;
import java.util.List;
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "SkillVersePrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_NAME = "userName";
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private MaterialToolbar toolbar;
    private TextView tvWelcome;
    private CardView cardMyCoursesAction;
    private CardView cardBrowseAction, cardCertificatesAction, cardProfileAction;
    private SwipeGestureHelper swipeHelper;




    private MaterialCardView cardContinueLearning;
    private TextView tvContinueCourseTitle;
    private TextView tvContinueModuleTitle;
    private android.widget.LinearLayout llContinueLearningSection;


    private void setupQuickActions() {
        cardMyCoursesAction.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MyCoursesActivity.class);
            startActivity(intent);
        });
        cardBrowseAction.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, BrowseCoursesActivity.class);
            startActivity(intent);
        });
        cardCertificatesAction.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MyCertificatesActivity.class);
            startActivity(intent);
        });
        cardProfileAction.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });
    }
    private RecyclerView rvRecentCourses;
    private MaterialCardView cardEmptyState;
    private CourseAdapter recentCoursesAdapter;
    private EnrollmentManager enrollmentManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setDecorFitsSystemWindows(false);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        if (!FirebaseAuthManager.isUserLoggedIn()) {
            navigateToLogin();
            return;
        }
        enrollmentManager = EnrollmentManager.getInstance(this);
        setupViews();
        setupNavigationDrawer();
        setupQuickActions();
        updateUserInfo();
        loadRecentCourses();
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadLastAccessed();
        loadLastAccessed();
        loadRecentCourses();
        verifyUserAccess();
    }
    
    private void verifyUserAccess() {
        String userId = FirebaseAuthManager.getCurrentUser().getUid();
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (!documentSnapshot.exists()) {
                      
                     handleLogout();
                } else {
                      
                     String role = documentSnapshot.getString("role");
                     if (role != null) {
                         String currentCached = sharedPreferences.getString("user_role", null);
                         if (!role.equals(currentCached)) {
                             sharedPreferences.edit().putString("user_role", role).apply();
                              
                             if ("admin".equals(role)) {
                                 Intent intent = new Intent(MainActivity.this, com.example.skillverse_android.admin.AdminDashboardActivity.class);
                                 startActivity(intent);
                                 finish();
                             }
                         }
                     }
                }
            })
            .addOnFailureListener(e -> {
                 
            });
    }
    private com.google.android.material.progressindicator.CircularProgressIndicator progressBar;

    private void setupViews() {
        progressBar = findViewById(R.id.progressBar);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        tvWelcome = findViewById(R.id.tvWelcome);
        cardMyCoursesAction = findViewById(R.id.cardMyCoursesAction);
        cardBrowseAction = findViewById(R.id.cardBrowseAction);
        cardCertificatesAction = findViewById(R.id.cardCertificatesAction);
        cardProfileAction = findViewById(R.id.cardProfileAction);
        
        llContinueLearningSection = findViewById(R.id.llContinueLearningSection);
        cardContinueLearning = findViewById(R.id.cardContinueLearning);
        tvContinueCourseTitle = findViewById(R.id.tvContinueCourseTitle);
        tvContinueModuleTitle = findViewById(R.id.tvContinueModuleTitle);
        
        rvRecentCourses = findViewById(R.id.rvRecentCourses);
        cardEmptyState = findViewById(R.id.cardEmptyState);
        setSupportActionBar(toolbar);
        rvRecentCourses.setLayoutManager(new LinearLayoutManager(this));
        recentCoursesAdapter = new CourseAdapter(course -> {
            Intent intent = new Intent(MainActivity.this, CourseDetailActivity.class);
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
        }, false);
        recentCoursesAdapter.setShowProgress(true);
        rvRecentCourses.setAdapter(recentCoursesAdapter);
        ViewCompat.setOnApplyWindowInsetsListener(toolbar, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), v.getPaddingBottom());
            return WindowInsetsCompat.CONSUMED;
        });
    }
    private void setupNavigationDrawer() {
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.app_name, R.string.app_name);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

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

        swipeHelper = new SwipeGestureHelper(this, () -> {
            if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    private void updateUserInfo() {
        String userEmail = sharedPreferences.getString(KEY_USER_EMAIL, "student@example.com");
        String userId = FirebaseAuthManager.getCurrentUser().getUid();
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
                    userName = userEmail.split("@")[0];
                    userName = userName.substring(0, 1).toUpperCase() + userName.substring(1);
                }
                tvWelcome.setText("Welcome, " + userName + "!");
                View headerView = navigationView.getHeaderView(0);
                TextView navHeaderName = headerView.findViewById(R.id.nav_header_name);
                TextView navHeaderEmail = headerView.findViewById(R.id.nav_header_email);
                navHeaderName.setText(userName);
                navHeaderEmail.setText(userEmail);
            })
            .addOnFailureListener(e -> {
                String userName = userEmail.split("@")[0];
                userName = userName.substring(0, 1).toUpperCase() + userName.substring(1);
                tvWelcome.setText("Welcome, " + userName + "!");
                View headerView = navigationView.getHeaderView(0);
                TextView navHeaderName = headerView.findViewById(R.id.nav_header_name);
                TextView navHeaderEmail = headerView.findViewById(R.id.nav_header_email);
                navHeaderName.setText(userName);
                navHeaderEmail.setText(userEmail);
            });
    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_browse_courses) {
            Intent intent = new Intent(MainActivity.this, BrowseCoursesActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_my_courses) {
            Intent intent = new Intent(MainActivity.this, MyCoursesActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_certificates) {
            Intent intent = new Intent(MainActivity.this, MyCertificatesActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_profile) {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_logout) {
            handleLogout();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
    private void handleLogout() {
        FirebaseAuthManager.logoutUser();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        navigateToLogin();
    }
    private boolean isUserLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    private void loadRecentCourses() {
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        rvRecentCourses.setVisibility(View.GONE);
        List<Course> allCourses = new ArrayList<>();
        String userId = FirebaseAuthManager.getCurrentUser().getUid();
        com.example.skillverse_android.utils.FirestoreRepository.getUserEnrollments(
            userId,
            new com.example.skillverse_android.utils.FirestoreRepository.DataCallback<List<Course>>() {
                @Override
                public void onSuccess(List<Course> enrolledCourses) {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    rvRecentCourses.setVisibility(View.VISIBLE);
                    List<Course> recentCourses = enrolledCourses.size() > 3
                        ? enrolledCourses.subList(0, 3)
                        : enrolledCourses;
                    for (Course course : recentCourses) {
                        int courseId = 1;
                        try {
                            courseId = Integer.parseInt(course.getId());
                        } catch (NumberFormatException e) {
                            courseId = Math.abs(course.getId().hashCode());
                        }
                        com.example.skillverse_android.utils.FirestoreRepository.getCourseProgress(
                            userId,
                            course.getId() != null ? course.getId() : String.valueOf(courseId),
                            course.getLessonsCount(),
                            new com.example.skillverse_android.utils.FirestoreRepository.DataCallback<Integer>() {
                                @Override
                                public void onSuccess(Integer progress) {
                                    course.setProgress(progress);
                                    recentCoursesAdapter.notifyDataSetChanged();
                                }
                                @Override
                                public void onFailure(String error) {
                                    course.setProgress(0);
                                }
                            }
                        );
                    }
                    recentCoursesAdapter.setCourses(recentCourses);
                }
                @Override
                public void onFailure(String error) {
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    rvRecentCourses.setVisibility(View.VISIBLE);
                    recentCoursesAdapter.setCourses(new ArrayList<>());
                }
            }
        );
    }
    private int calculateCourseProgress(String courseId) {
        return 0;
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

    private void loadLastAccessed() {
        String userId = FirebaseAuthManager.getCurrentUser().getUid();
        com.example.skillverse_android.utils.FirestoreRepository.getLastAccessed(userId,
            new com.example.skillverse_android.utils.FirestoreRepository.DataCallback<java.util.Map<String, Object>>() {
                @Override
                public void onSuccess(java.util.Map<String, Object> data) {
                    if (data != null && data.containsKey("courseId") && data.containsKey("moduleId")) {
                        String courseId = (String) data.get("courseId");
                        String courseTitle = (String) data.get("courseTitle");
                        String moduleId = (String) data.get("moduleId");
                        String moduleTitle = (String) data.get("moduleTitle");

                        try {
                            if (tvContinueCourseTitle != null && courseTitle != null) {
                                tvContinueCourseTitle.setText(courseTitle);
                            }
                            
                            if (tvContinueModuleTitle != null && moduleTitle != null) {
                                tvContinueModuleTitle.setText(moduleTitle);
                            }
                            
                            if (llContinueLearningSection != null) {
                                llContinueLearningSection.setVisibility(View.VISIBLE);
                            }

                            if (cardContinueLearning != null) {
                                cardContinueLearning.setOnClickListener(v -> {
                                    Intent intent = new Intent(MainActivity.this, ModuleDetailActivity.class);
                                    int courseIdInt = 1;
                                    try {
                                        courseIdInt = Integer.parseInt(courseId);
                                    } catch (NumberFormatException e) {
                                        courseIdInt = Math.abs(courseId.hashCode());
                                    }
                                    intent.putExtra(ModuleDetailActivity.EXTRA_COURSE_ID, courseIdInt);
                                    intent.putExtra(ModuleDetailActivity.EXTRA_COURSE_DOC_ID, courseId);
                                    intent.putExtra(ModuleDetailActivity.EXTRA_COURSE_TITLE, courseTitle);
                                    intent.putExtra(ModuleDetailActivity.EXTRA_MODULE_ID, moduleId);
                                    startActivity(intent);
                                });
                            }
                        } catch (Exception e) {
                             
                        }
                    } else {
                        if (llContinueLearningSection != null) llContinueLearningSection.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onFailure(String error) {
                    if (llContinueLearningSection != null) llContinueLearningSection.setVisibility(View.GONE);
                }
            });
    }
}