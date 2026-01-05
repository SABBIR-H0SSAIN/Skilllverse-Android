package com.example.skillverse_android.admin.dialogs;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.example.skillverse_android.R;
import com.example.skillverse_android.admin.repository.AdminFirestoreRepository;
import com.example.skillverse_android.models.Course;
import com.example.skillverse_android.models.EnrollmentKey;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;
public class GenerateKeysDialog extends DialogFragment {
    private Spinner spinnerCourse;
    private EditText etCount;
    private List<Course> courseList = new ArrayList<>();
    private OnKeysGeneratedListener listener;
    public interface OnKeysGeneratedListener {
        void onKeysGenerated();
    }
    public void setOnKeysGeneratedListener(OnKeysGeneratedListener listener) {
        this.listener = listener;
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_generate_keys, null);
        spinnerCourse = view.findViewById(R.id.spinnerCourse);
        etCount = view.findViewById(R.id.etCount);
        loadCourses();
        return new AlertDialog.Builder(getContext())
                .setTitle("Generate Enrollment Keys")
                .setView(view)
                .setPositiveButton("Generate", (dialog, which) -> generateKeys())
                .setNegativeButton("Cancel", null)
                .create();
    }
    private void loadCourses() {
        AdminFirestoreRepository.getAllCourses(new AdminFirestoreRepository.DataCallback<List<Course>>() {
            @Override
            public void onSuccess(List<Course> data) {
                if (getContext() != null) {
                    courseList = data;
                    List<String> courseTitles = new ArrayList<>();
                    for (Course course : data) {
                        courseTitles.add(course.getTitle());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                        android.R.layout.simple_spinner_item, courseTitles);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerCourse.setAdapter(adapter);
                }
            }
            @Override
            public void onFailure(String error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error loading courses", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private void generateKeys() {
        if (courseList.isEmpty()) {
            Toast.makeText(getContext(), "No courses available", Toast.LENGTH_SHORT).show();
            return;
        }
        String countStr = etCount.getText().toString();
        if (countStr.isEmpty()) {
            Toast.makeText(getContext(), "Enter number of keys", Toast.LENGTH_SHORT).show();
            return;
        }
        int count = Integer.parseInt(countStr);
        if (count < 1 || count > 100) {
            Toast.makeText(getContext(), "Count must be between 1 and 100", Toast.LENGTH_SHORT).show();
            return;
        }
        Course selectedCourse = courseList.get(spinnerCourse.getSelectedItemPosition());
        String createdBy = FirebaseAuth.getInstance().getCurrentUser().getUid();
        AdminFirestoreRepository.generateKeys(selectedCourse.getId(), selectedCourse.getTitle(), count, null, createdBy,
            new AdminFirestoreRepository.DataCallback<List<EnrollmentKey>>() {
                @Override
                public void onSuccess(List<EnrollmentKey> data) {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), count + " keys generated", Toast.LENGTH_SHORT).show();
                    }
                    if (listener != null) {
                        listener.onKeysGenerated();
                    }
                }
                @Override
                public void onFailure(String error) {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }
}
