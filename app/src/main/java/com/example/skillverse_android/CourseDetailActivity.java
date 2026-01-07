package com.example.skillverse_android;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.skillverse_android.adapters.ModuleListAdapter;
import com.example.skillverse_android.models.Course;
import com.example.skillverse_android.models.Module;
import com.example.skillverse_android.utils.EnrollmentManager;
import com.example.skillverse_android.utils.EnrollmentManager;
import com.example.skillverse_android.utils.EnrollmentManager;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import java.util.List;
public class CourseDetailActivity extends AppCompatActivity {
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setDecorFitsSystemWindows(false);
        setContentView(R.layout.activity_course_detail);
        enrollmentManager = EnrollmentManager.getInstance(this);
        setupViews();
        loadCourseData();
        checkEnrollmentStatus();
        loadModules();
    }
    private void setupViews() {
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
        super.onBackPressed();
    }
}
