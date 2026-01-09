package com.example.skillverse_android;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
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
import com.example.skillverse_android.models.Module;
import com.example.skillverse_android.models.ModuleResource;
import com.example.skillverse_android.utils.FirebaseAuthManager;
import com.example.skillverse_android.utils.FirestoreRepository;
import com.example.skillverse_android.utils.SwipeGestureHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import java.util.List;
public class ModuleDetailActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    public static final String EXTRA_COURSE_ID = "course_id";
    public static final String EXTRA_COURSE_DOC_ID = "course_doc_id";
    public static final String EXTRA_COURSE_TITLE = "course_title";
    public static final String EXTRA_MODULE_ID = "module_id";
    private TextView tvModuleCounter, tvModuleTitle, tvModuleDescription, tvModuleDuration;
    private TextView tvVideoTitle;
    private WebView webViewVideo;
    private android.widget.ImageView ivVideoThumbnail;
    private View llVideoPlaceholder;
    private MaterialButton btnPreviousModule, btnNextModule, btnMarkCompleted;
    private LinearLayout llResources;
    private TextView tvResourceCount;
    private List<Module> modules;
    private Module currentModule;
    private int currentModuleIndex = 0;
    private int courseId;
    private String courseDocId;
    private String courseTitle;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private SwipeGestureHelper swipeHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setDecorFitsSystemWindows(false);
        setContentView(R.layout.activity_module_detail);
        setupViews();
        setupNavigationDrawer();
        loadIntentData();
    }
    private void loadIntentData() {
        Intent intent = getIntent();
        courseId = intent.getIntExtra(EXTRA_COURSE_ID, 1);
        courseDocId = intent.getStringExtra(EXTRA_COURSE_DOC_ID);
        courseTitle = intent.getStringExtra(EXTRA_COURSE_TITLE);
        if (courseDocId == null || courseDocId.isEmpty()) {
            courseDocId = String.valueOf(courseId);
        }
        courseTitle = intent.getStringExtra(EXTRA_COURSE_TITLE);
        courseTitle = intent.getStringExtra(EXTRA_COURSE_TITLE);
        String moduleIdStr = intent.getStringExtra(EXTRA_MODULE_ID);
        int moduleIdInt = intent.getIntExtra(EXTRA_MODULE_ID, -1);
        String moduleId = null;
        if (moduleIdStr != null && !moduleIdStr.isEmpty()) {
            moduleId = moduleIdStr;
        } else if (moduleIdInt != -1) {
            moduleId = String.valueOf(moduleIdInt);
        } else {
            moduleId = String.valueOf(moduleIdInt);
        }
        final String finalModuleId = moduleId;
        com.example.skillverse_android.utils.FirestoreRepository.getModulesForCourse(
            courseDocId,
            new com.example.skillverse_android.utils.FirestoreRepository.DataCallback<List<Module>>() {
                @Override
                public void onSuccess(List<Module> firestoreModules) {
                    modules = firestoreModules;
                    for (int i = 0; i < modules.size(); i++) {
                        String modId = modules.get(i).getDocumentId() != null
                            ? modules.get(i).getDocumentId()
                            : String.valueOf(modules.get(i).getId());
                        if (modId.equals(finalModuleId)) {
                            currentModuleIndex = i;
                            break;
                        }
                    }
                    if (modules != null && !modules.isEmpty()) {
                        displayCurrentModule();
                    }
                }
                @Override
                public void onFailure(String error) {
                    Toast.makeText(ModuleDetailActivity.this,
                        "Failed to load modules: " + error, Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        );
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
        tvModuleCounter = findViewById(R.id.tvModuleCounter);
        tvModuleTitle = findViewById(R.id.tvModuleTitle);
        tvModuleDescription = findViewById(R.id.tvModuleDescription);
        tvModuleDuration = findViewById(R.id.tvModuleDuration);
        tvVideoTitle = findViewById(R.id.tvVideoTitle);
        webViewVideo = findViewById(R.id.webViewVideo);
        ivVideoThumbnail = findViewById(R.id.ivVideoThumbnail);
        llVideoPlaceholder = findViewById(R.id.llVideoPlaceholder);
        btnPreviousModule = findViewById(R.id.btnPreviousModule);
        btnNextModule = findViewById(R.id.btnNextModule);
        btnMarkCompleted = findViewById(R.id.btnMarkCompleted);
        llResources = findViewById(R.id.llResources);
        tvResourceCount = findViewById(R.id.tvResourceCount);
        WebSettings webSettings = webViewVideo.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);
        webViewVideo.setWebChromeClient(new android.webkit.WebChromeClient());
        webViewVideo.setVisibility(View.GONE);
        llVideoPlaceholder.setOnClickListener(v -> loadYouTubeVideo());
        btnPreviousModule.setOnClickListener(v -> navigateToPreviousModule());
        btnNextModule.setOnClickListener(v -> navigateToNextModule());
        btnMarkCompleted.setOnClickListener(v -> toggleModuleCompletion());
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

    private void loadModules() {
    }
    private void displayCurrentModule() {
        if (modules == null || modules.isEmpty()) return;
        currentModule = modules.get(currentModuleIndex);
        tvModuleCounter.setText("Module " + (currentModuleIndex + 1) + " of " + modules.size());
        tvModuleTitle.setText(currentModule.getTitle());
        String description = currentModule.getDescription();
        if (description == null || description.isEmpty()) {
            description = currentModule.description();
        }
        if (description == null || description.isEmpty()) {
            description = "No description available";
        }
        tvModuleDescription.setText(description);
        tvModuleDuration.setText("⏱️ " + currentModule.getDuration() + " minutes");
        tvVideoTitle.setText(currentModule.getTitle());
        llVideoPlaceholder.setVisibility(View.VISIBLE);
        webViewVideo.setVisibility(View.GONE);
        
        String videoId = currentModule.getYoutubeVideoId();
        if (videoId != null && !videoId.isEmpty()) {
            String thumbnailUrl = "https://img.youtube.com/vi/" + videoId + "/maxresdefault.jpg";
            new Thread(() -> {
                try {
                    java.io.InputStream input = new java.net.URL(thumbnailUrl).openStream();
                    android.graphics.Bitmap bitmap = android.graphics.BitmapFactory.decodeStream(input);
                    runOnUiThread(() -> {
                        if (ivVideoThumbnail != null) {
                            ivVideoThumbnail.setImageBitmap(bitmap);
                        }
                    });
                } catch (Exception e) {
                }
            }).start();
        }
        
        btnPreviousModule.setEnabled(currentModuleIndex > 0);
        btnNextModule.setEnabled(currentModuleIndex < modules.size() - 1);
        updateMarkCompletedButton(currentModule);
        String userId = com.example.skillverse_android.utils.FirebaseAuthManager.getCurrentUser().getUid();
        com.example.skillverse_android.utils.ProgressCacheManager cacheManager =
            com.example.skillverse_android.utils.ProgressCacheManager.getInstance(this);
        Boolean cachedStatus = cacheManager.getCachedModuleProgress(userId, courseId, currentModule.getId());
        if (cachedStatus != null) {
            currentModule.setCompleted(cachedStatus);
            updateMarkCompletedButton(currentModule);
        }
        com.example.skillverse_android.utils.FirestoreRepository.getModuleProgress(
            userId,
            courseDocId,
            currentModule.getDocumentId() != null ? currentModule.getDocumentId() : String.valueOf(currentModule.getId()),
            new com.example.skillverse_android.utils.FirestoreRepository.DataCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean completed) {
                    cacheManager.cacheModuleProgress(userId, courseDocId,
                        currentModule.getDocumentId() != null ? currentModule.getDocumentId() : String.valueOf(currentModule.getId()),
                        completed);
                    if (currentModule.isCompleted() != completed) {
                        currentModule.setCompleted(completed);
                        updateMarkCompletedButton(currentModule);
                    }
                }
                @Override
                public void onFailure(String error) {
                    if (cachedStatus == null) {
                        currentModule.setCompleted(false);
                        updateMarkCompletedButton(currentModule);
                    }
                }
            }
        );
        loadResourcesForCurrentModule(currentModule);
        
        String userIdLastAccessed = com.example.skillverse_android.utils.FirebaseAuthManager.getCurrentUser().getUid();
        String modId = currentModule.getDocumentId() != null ? currentModule.getDocumentId() : String.valueOf(currentModule.getId());
        
        com.example.skillverse_android.utils.FirestoreRepository.updateLastAccessed(
            userIdLastAccessed,
            courseDocId,
            courseTitle,
            modId,
            currentModule.getTitle(),
            new com.example.skillverse_android.utils.FirestoreRepository.DataCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean data) {
                }

                @Override
                public void onFailure(String error) {
                }
            }
        );
    }
    private void loadYouTubeVideo() {
        if (currentModule == null) return;
        webViewVideo.setVisibility(View.GONE);
        llVideoPlaceholder.setVisibility(View.VISIBLE);

        String videoId = currentModule.getYoutubeVideoId();
        try {
            Intent appIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse("vnd.youtube:" + videoId));
            startActivity(appIntent);
        } catch (Exception e) {
            try {
                Intent webIntent = new Intent(Intent.ACTION_VIEW,
                    android.net.Uri.parse("https://www.youtube.com/watch?v=" + videoId));
                startActivity(webIntent);
            } catch (Exception ex) {
                displayCurrentModule();

            }
        }
    }
    private void updateMarkCompletedButton(Module module) {
        if (module.isCompleted()) {
            btnMarkCompleted.setText("✓ Completed");
            btnMarkCompleted.setBackgroundTintList(getColorStateList(android.R.color.darker_gray));
            btnMarkCompleted.setEnabled(true);
        } else {
            btnMarkCompleted.setText("✓ Mark as Completed");
            btnMarkCompleted.setBackgroundTintList(getColorStateList(R.color.success));
            btnMarkCompleted.setEnabled(true);
        }
    }
    private void toggleModuleCompletion() {
        if (currentModule == null) return;
        boolean newCompletedState = !currentModule.isCompleted();
        String userId = com.example.skillverse_android.utils.FirebaseAuthManager.getCurrentUser().getUid();
        com.example.skillverse_android.utils.ProgressCacheManager cacheManager =
            com.example.skillverse_android.utils.ProgressCacheManager.getInstance(this);
        cacheManager.cacheModuleProgress(userId, courseDocId, currentModule.getDocumentId(), newCompletedState);
        currentModule.setCompleted(newCompletedState);
        btnMarkCompleted.setEnabled(false);
        btnMarkCompleted.setText(newCompletedState ? "Saving..." : "Unmarking...");
        com.example.skillverse_android.utils.FirestoreRepository.updateModuleProgress(
            userId,
            courseDocId,
            currentModule.getDocumentId(),
            newCompletedState,
            new com.example.skillverse_android.utils.FirestoreRepository.DataCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean success) {
                    updateMarkCompletedButton(currentModule);
                    Toast.makeText(ModuleDetailActivity.this,
                        newCompletedState ? "✓ Module completed!" : "Module unmarked",
                        Toast.LENGTH_SHORT).show();
                    if (newCompletedState) {
                        checkCourseCompletionAndAwardCertificate(userId);
                    }
                }
                @Override
                public void onFailure(String error) {
                    currentModule.setCompleted(!newCompletedState);
                    cacheManager.cacheModuleProgress(userId, courseDocId, currentModule.getDocumentId(), !newCompletedState);
                    updateMarkCompletedButton(currentModule);
                    Toast.makeText(ModuleDetailActivity.this,
                        "Failed to save: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        );
    }
    private void loadResourcesForCurrentModule(Module module) {
        if (module.getDocumentId() == null || module.getDocumentId().isEmpty()) {
            Toast.makeText(this, "Error: Module ID is missing", Toast.LENGTH_SHORT).show();
            return;
        }
        com.example.skillverse_android.utils.FirestoreRepository.getResourcesForModule(
            courseDocId,
            module.getDocumentId(),
            new com.example.skillverse_android.utils.FirestoreRepository.DataCallback<List<ModuleResource>>() {
                @Override
                public void onSuccess(List<ModuleResource> resources) {
                    tvResourceCount.setText(String.valueOf(resources.size()));
                    llResources.removeAllViews();
                    for (ModuleResource resource : resources) {
                        View resourceView = LayoutInflater.from(ModuleDetailActivity.this)
                            .inflate(R.layout.item_resource, llResources, false);
                        TextView tvResourceIcon = resourceView.findViewById(R.id.tvResourceIcon);
                        TextView tvResourceTitle = resourceView.findViewById(R.id.tvResourceTitle);
                        TextView tvResourceType = resourceView.findViewById(R.id.tvResourceType);
                        TextView tvResourceSize = resourceView.findViewById(R.id.tvResourceSize);
                        tvResourceIcon.setText(resource.getIcon());
                        tvResourceTitle.setText(resource.getTitle());
                        tvResourceType.setText(resource.getType());
                        tvResourceSize.setText(resource.getSize());
                        resourceView.setOnClickListener(v -> {
                            String url = resource.getUrl();
                            if (url != null && !url.isEmpty()) {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url));
                                try {
                                    startActivity(browserIntent);
                                } catch (Exception e) {
                                }
                            } else {
                            }
                        });
                        llResources.addView(resourceView);
                    }
                }
                @Override
                public void onFailure(String error) {
                    tvResourceCount.setText("0");
                    llResources.removeAllViews();
                    Toast.makeText(ModuleDetailActivity.this,
                        "Failed to load resources: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        );
    }
    private void navigateToPreviousModule() {
        if (currentModuleIndex > 0) {
            currentModuleIndex--;
            displayCurrentModule();
        }
    }
    private void navigateToNextModule() {
        if (currentModuleIndex < modules.size() - 1) {
            currentModuleIndex++;
            displayCurrentModule();
        }
    }
    private void checkCourseCompletionAndAwardCertificate(String userId) {
        com.example.skillverse_android.utils.FirestoreRepository.checkCourseCompletion(
            userId, courseDocId,
            new com.example.skillverse_android.utils.FirestoreRepository.DataCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean isComplete) {
                    if (isComplete) {
                        com.example.skillverse_android.utils.FirestoreRepository.hasCertificate(
                            userId, courseDocId,
                            new com.example.skillverse_android.utils.FirestoreRepository.DataCallback<Boolean>() {
                                @Override
                                public void onSuccess(Boolean hasCert) {
                                    if (!hasCert) {
                                        awardCertificate(userId);
                                    }
                                }
                                @Override
                                public void onFailure(String error) {
                                }
                            }
                        );
                    }
                }
                @Override
                public void onFailure(String error) {
                }
            }
        );
    }
    private void awardCertificate(String userId) {
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                String studentName = "Student";
                if (documentSnapshot.exists() && documentSnapshot.getString("name") != null) {
                    studentName = documentSnapshot.getString("name");
                } else {
                    if (com.example.skillverse_android.utils.FirebaseAuthManager.getCurrentUser() != null) {
                        String displayName = com.example.skillverse_android.utils.FirebaseAuthManager.getCurrentUser().getDisplayName();
                        if (displayName != null && !displayName.isEmpty()) {
                            studentName = displayName;
                        } else {
                            String email = com.example.skillverse_android.utils.FirebaseAuthManager.getCurrentUser().getEmail();
                            if (email != null) {
                                studentName = email.split("@")[0];
                            }
                        }
                    }
                }
                final String finalStudentName = studentName;
                com.example.skillverse_android.utils.FirestoreRepository.createCertificate(
                    userId, courseDocId, courseTitle, finalStudentName,
                    new com.example.skillverse_android.utils.FirestoreRepository.DataCallback<String>() {
                        @Override
                        public void onSuccess(String certificateId) {
                            showCongratulationsDialog(certificateId);
                        }
                        @Override
                        public void onFailure(String error) {
                            Toast.makeText(ModuleDetailActivity.this,
                                "Could not create certificate: " + error, Toast.LENGTH_SHORT).show();
                        }
                    }
                );
            })
            .addOnFailureListener(e -> {
                createCertificateWithFallbackName(userId);
            });
    }
    private void createCertificateWithFallbackName(String userId) {
        String studentName = "Student";
        if (com.example.skillverse_android.utils.FirebaseAuthManager.getCurrentUser() != null) {
            String displayName = com.example.skillverse_android.utils.FirebaseAuthManager.getCurrentUser().getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                studentName = displayName;
            }
        }
        com.example.skillverse_android.utils.FirestoreRepository.createCertificate(
            userId, courseDocId, courseTitle, studentName,
            new com.example.skillverse_android.utils.FirestoreRepository.DataCallback<String>() {
                @Override
                public void onSuccess(String certificateId) {
                    showCongratulationsDialog(certificateId);
                }
                @Override
                public void onFailure(String error) {
                    Toast.makeText(ModuleDetailActivity.this,
                        "Could not create certificate: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        );
    }
    private void showCongratulationsDialog(String certificateId) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("🎉 Congratulations!")
            .setMessage("You have successfully completed all modules in \"" + courseTitle + "\"!\n\n" +
                        "Your certificate has been issued.\n\n" +
                        "Certificate ID: " + certificateId + "\n\n" +
                        "View your certificates in 'My Certificates' section.")
            .setPositiveButton("View Certificates", (dialog, which) -> {
                Intent intent = new Intent(ModuleDetailActivity.this, MyCertificatesActivity.class);
                startActivity(intent);
            })
            .setNegativeButton("Close", null)
            .setCancelable(false)
            .show();
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
