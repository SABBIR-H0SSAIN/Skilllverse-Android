package com.example.skillverse_android;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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
import com.example.skillverse_android.adapters.CertificateAdapter;
import com.example.skillverse_android.models.Certificate;
import com.example.skillverse_android.utils.FirebaseAuthManager;
import com.example.skillverse_android.utils.FirestoreRepository;
import com.example.skillverse_android.utils.SwipeGestureHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.navigation.NavigationView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MyCertificatesActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private RecyclerView rvCertificates;
    private LinearLayout llEmptyState;
    private CertificateAdapter adapter;
    private ChipGroup chipGroupFilter;
    private List<Certificate> allCertificates = new ArrayList<>();
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private SwipeGestureHelper swipeHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setDecorFitsSystemWindows(false);
        setContentView(R.layout.activity_my_certificates);
        setupViews();
        setupNavigationDrawer();
        loadCertificates();
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

        rvCertificates = findViewById(R.id.rvCertificates);
        llEmptyState = findViewById(R.id.llEmptyState);
        rvCertificates.setLayoutManager(new LinearLayoutManager(this));

        chipGroupFilter = findViewById(R.id.chipGroupFilter);
        chipGroupFilter.setOnCheckedChangeListener((group, checkedId) -> filterCertificates());
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

    private void loadCertificates() {
        if (FirebaseAuthManager.getCurrentUser() == null) {
            rvCertificates.setVisibility(View.GONE);
            llEmptyState.setVisibility(View.VISIBLE);
            return;
        }

        String userId = FirebaseAuthManager.getCurrentUser().getUid();
        FirestoreRepository.getUserCertificates(userId, new FirestoreRepository.DataCallback<List<Certificate>>() {
            @Override
            public void onSuccess(List<Certificate> certificates) {
                allCertificates.clear();
                allCertificates.addAll(certificates);
                filterCertificates();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(MyCertificatesActivity.this, "Error loading certificates: " + error, Toast.LENGTH_SHORT).show();
                rvCertificates.setVisibility(View.GONE);
                llEmptyState.setVisibility(View.VISIBLE);
            }
        });
    }

    private void filterCertificates() {
        boolean showRevoked = chipGroupFilter.getCheckedChipId() == R.id.chipRevoked;
        List<Certificate> filtered = new ArrayList<>();

        for (Certificate cert : allCertificates) {
            if (cert.isRevoked() == showRevoked) {
                filtered.add(cert);
            }
        }

        if (filtered.isEmpty()) {
            rvCertificates.setVisibility(View.GONE);
            llEmptyState.setVisibility(View.VISIBLE);
        } else {
            rvCertificates.setVisibility(View.VISIBLE);
            llEmptyState.setVisibility(View.GONE);
            adapter = new CertificateAdapter(filtered, certificate -> {
                if (certificate.isRevoked()) {
                    Toast.makeText(this, "Cannot access revoked certificate", Toast.LENGTH_SHORT).show();
                    return;
                }
                exportCertificateToPdf(certificate);
            });
            rvCertificates.setAdapter(adapter);
        }
    }

    private void exportCertificateToPdf(Certificate certificate) {
        PdfDocument document = new PdfDocument();
        int pageWidth = 842;
        int pageHeight = 595;
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        drawCertificate(canvas, certificate, pageWidth, pageHeight);
        document.finishPage(page);
        try {
            String fileName = "Certificate_" + certificate.getCertificateId() + ".pdf";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                values.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                Uri uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
                if (uri != null) {
                    OutputStream out = getContentResolver().openOutputStream(uri);
                    document.writeTo(out);
                    out.close();
                    Toast.makeText(this, "Certificate saved to Downloads!", Toast.LENGTH_LONG).show();
                }
            } else {
                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File file = new File(downloadsDir, fileName);
                FileOutputStream out = new FileOutputStream(file);
                document.writeTo(out);
                out.close();
                Toast.makeText(this, "Certificate saved to: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error saving PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        document.close();
    }

    private void drawCertificate(Canvas canvas, Certificate certificate, int width, int height) {
        int navyBlue = Color.parseColor("#1A365D");
        int gold = Color.parseColor("#C9A227");
        int cream = Color.parseColor("#FFFDF5");
        int gray = Color.parseColor("#666666");

        Paint bgPaint = new Paint();
        bgPaint.setColor(cream);
        canvas.drawRect(0, 0, width, height, bgPaint);

        Paint borderPaint = new Paint();
        borderPaint.setColor(gold);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(12);
        canvas.drawRect(20, 20, width - 20, height - 20, borderPaint);

        Paint innerBorderPaint = new Paint();
        innerBorderPaint.setColor(navyBlue);
        innerBorderPaint.setStyle(Paint.Style.STROKE);
        innerBorderPaint.setStrokeWidth(3);
        canvas.drawRect(40, 40, width - 40, height - 40, innerBorderPaint);

        Paint headerPaint = new Paint();
        headerPaint.setColor(navyBlue);
        headerPaint.setTextAlign(Paint.Align.CENTER);
        headerPaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
        headerPaint.setTextSize(24);
        headerPaint.setLetterSpacing(0.3f);
        canvas.drawText("CERTIFICATE", width / 2f, 100, headerPaint);

        headerPaint.setTextSize(40);
        headerPaint.setLetterSpacing(0.1f);
        canvas.drawText("OF COMPLETION", width / 2f, 150, headerPaint);

        Paint linePaint = new Paint();
        linePaint.setColor(gold);
        linePaint.setStrokeWidth(2);
        canvas.drawLine(width/2f - 100, 175, width/2f + 100, 175, linePaint);

        Paint regularPaint = new Paint();
        regularPaint.setColor(gray);
        regularPaint.setTextAlign(Paint.Align.CENTER);
        regularPaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.NORMAL));
        regularPaint.setTextSize(18);
        canvas.drawText("This is to certify that", width / 2f, 220, regularPaint);

        Paint namePaint = new Paint();
        namePaint.setColor(navyBlue);
        namePaint.setTextAlign(Paint.Align.CENTER);
        namePaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD_ITALIC));
        namePaint.setTextSize(36);
        canvas.drawText(certificate.getStudentName(), width / 2f, 280, namePaint);

        regularPaint.setTextSize(18);
        canvas.drawText("has successfully completed the course", width / 2f, 320, regularPaint);

        Paint coursePaint = new Paint();
        coursePaint.setColor(navyBlue);
        coursePaint.setTextAlign(Paint.Align.CENTER);
        coursePaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
        coursePaint.setTextSize(28);
        canvas.drawText(certificate.getCourseName(), width / 2f, 380, coursePaint);

        canvas.drawLine(width/2f - 80, 410, width/2f + 80, 410, linePaint);

        regularPaint.setTextSize(16);
        regularPaint.setColor(gray);
        canvas.drawText(certificate.getDateCompleted(), width / 2f - 120, 480, regularPaint);

        linePaint.setColor(gray);
        canvas.drawLine(width/2f - 200, 490, width/2f - 40, 490, linePaint);

        regularPaint.setTextSize(12);
        canvas.drawText("Date", width / 2f - 120, 510, regularPaint);

        Paint scorePaint = new Paint();
        scorePaint.setColor(gold);
        scorePaint.setTextAlign(Paint.Align.CENTER);
        scorePaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD));
        scorePaint.setTextSize(20);
        canvas.drawText((int)certificate.getScore() + "%", width / 2f + 120, 480, scorePaint);

        canvas.drawLine(width/2f + 40, 490, width/2f + 200, 490, linePaint);

        regularPaint.setTextSize(12);
        canvas.drawText("Score", width / 2f + 120, 510, regularPaint);

        Paint idPaint = new Paint();
        idPaint.setColor(Color.parseColor("#999999"));
        idPaint.setTextAlign(Paint.Align.CENTER);
        idPaint.setTypeface(Typeface.MONOSPACE);
        idPaint.setTextSize(12);
        canvas.drawText("Certificate ID: " + certificate.getCertificateId(), width / 2f, 550, idPaint);
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
