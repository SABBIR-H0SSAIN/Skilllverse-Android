package com.example.skillverse_android.admin.dialogs;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.example.skillverse_android.R;
import com.example.skillverse_android.admin.repository.AdminFirestoreRepository;
import com.example.skillverse_android.models.Course;
import com.example.skillverse_android.models.Instructor;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;
public class EditCourseDialog extends DialogFragment {
    private Course course;
    private OnCourseUpdatedListener listener;
    private List<Instructor> instructorList = new ArrayList<>();
    private List<String> selectedInstructorIds = new ArrayList<>();
    private boolean[] selectedInstructors;
    private EditText etTitle, etDescription, etDuration, etLessons, etPrice, etImageUrl;
    private Spinner spinnerCategory, spinnerDifficulty;
    private MaterialButton btnSelectInstructors;
    private CheckBox cbPublished;
    public interface OnCourseUpdatedListener {
        void onCourseUpdated();
    }
    public static EditCourseDialog newInstance(Course course) {
        EditCourseDialog dialog = new EditCourseDialog();
        dialog.course = course;
        return dialog;
    }
    public void setOnCourseUpdatedListener(OnCourseUpdatedListener listener) {
        this.listener = listener;
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit_course, null);
        etTitle = view.findViewById(R.id.etTitle);
        etDescription = view.findViewById(R.id.etDescription);
        etDuration = view.findViewById(R.id.etDuration);
        etLessons = view.findViewById(R.id.etLessons);
        etPrice = view.findViewById(R.id.etPrice);
        etImageUrl = view.findViewById(R.id.etImageUrl);
        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        spinnerDifficulty = view.findViewById(R.id.spinnerDifficulty);
        btnSelectInstructors = view.findViewById(R.id.btnSelectInstructors);
        cbPublished = view.findViewById(R.id.cbPublished);
        setupCategorySpinner();
        setupDifficultySpinner();
        loadInstructors();
        if (course != null) {
            etTitle.setText(course.getTitle());
            etDescription.setText(course.getDescription());
            etDuration.setText(String.valueOf(course.getDuration()));
            etLessons.setText(String.valueOf(course.getLessonsCount()));
            etPrice.setText(String.valueOf(course.getPrice()));
            etImageUrl.setText(course.getImageUrl());
            cbPublished.setChecked(course.isPublished());
            if (course.getInstructorIds() != null) {
                selectedInstructorIds = new ArrayList<>(course.getInstructorIds());
            }
            if (course.getCategory() != null) {
                int categoryPos = ((ArrayAdapter<String>) spinnerCategory.getAdapter()).getPosition(course.getCategory());
                if (categoryPos >= 0) spinnerCategory.setSelection(categoryPos);
            }
            if (course.getDifficulty() != null) {
                int difficultyPos = ((ArrayAdapter<String>) spinnerDifficulty.getAdapter()).getPosition(course.getDifficulty());
                if (difficultyPos >= 0) spinnerDifficulty.setSelection(difficultyPos);
            }
        }
        btnSelectInstructors.setOnClickListener(v -> showInstructorSelectionDialog());
        builder.setView(view)
                .setTitle("Edit Course")
                .setPositiveButton("Save", (dialog, which) -> updateCourse())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        return builder.create();
    }
    private void setupCategorySpinner() {
        String[] categories = {"Programming", "Design", "Business", "Marketing", "Photography", "Music", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }
    private void setupDifficultySpinner() {
        String[] difficulties = {"Beginner", "Intermediate", "Advanced"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, difficulties);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDifficulty.setAdapter(adapter);
    }
    private void loadInstructors() {
        AdminFirestoreRepository.getAllInstructors(new AdminFirestoreRepository.DataCallback<List<Instructor>>() {
            @Override
            public void onSuccess(List<Instructor> data) {
                instructorList = data;
                selectedInstructors = new boolean[instructorList.size()];
                for (int i = 0; i < instructorList.size(); i++) {
                    if (selectedInstructorIds.contains(instructorList.get(i).getId())) {
                        selectedInstructors[i] = true;
                    }
                }
                updateInstructorButtonText();
            }
            @Override
            public void onFailure(String error) {
                Toast.makeText(requireContext(), "Error loading instructors: " + error, Toast.LENGTH_SHORT).show();
                selectedInstructors = new boolean[0];
            }
        });
    }
    private void showInstructorSelectionDialog() {
        if (instructorList.isEmpty()) {
            Toast.makeText(requireContext(), "No instructors available", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] instructorNames = new String[instructorList.size()];
        for (int i = 0; i < instructorList.size(); i++) {
            instructorNames[i] = instructorList.get(i).getName();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select Instructors");
        builder.setMultiChoiceItems(instructorNames, selectedInstructors, (dialog, which, isChecked) -> {
            selectedInstructors[which] = isChecked;
        });
        builder.setPositiveButton("OK", (dialog, which) -> {
            selectedInstructorIds.clear();
            for (int i = 0; i < selectedInstructors.length; i++) {
                if (selectedInstructors[i]) {
                    selectedInstructorIds.add(instructorList.get(i).getId());
                }
            }
            updateInstructorButtonText();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    private void updateInstructorButtonText() {
        int count = 0;
        if (selectedInstructors != null) {
            for (boolean selected : selectedInstructors) {
                if (selected) count++;
            }
        }
        if (count == 0) {
            btnSelectInstructors.setText("Select Instructors (none)");
        } else {
            btnSelectInstructors.setText("Select Instructors (" + count + " selected)");
        }
    }
    private void updateCourse() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String durationStr = etDuration.getText().toString().trim();
        String lessonsStr = etLessons.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String imageUrl = etImageUrl.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a title", Toast.LENGTH_SHORT).show();
            return;
        }
        if (description.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a description", Toast.LENGTH_SHORT).show();
            return;
        }
        int duration = 0;
        int lessons = 0;
        double price = 0.0;
        try {
            if (!durationStr.isEmpty()) duration = Integer.parseInt(durationStr);
            if (!lessonsStr.isEmpty()) lessons = Integer.parseInt(lessonsStr);
            if (!priceStr.isEmpty()) price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Invalid number format", Toast.LENGTH_SHORT).show();
            return;
        }
        course.setTitle(title);
        course.setDescription(description);
        course.setCategory(spinnerCategory.getSelectedItem().toString());
        course.setDifficulty(spinnerDifficulty.getSelectedItem().toString());
        course.setDuration(duration);
        course.setLessonsCount(lessons);
        course.setPrice(price);
        course.setImageUrl(imageUrl);
        course.setInstructorIds(new ArrayList<>(selectedInstructorIds));
        course.setPublished(cbPublished.isChecked());
        AdminFirestoreRepository.updateCourse(course, new AdminFirestoreRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                if (!isAdded() || getContext() == null) return;
                Toast.makeText(getContext(), "Course updated successfully", Toast.LENGTH_SHORT).show();
                if (listener != null) {
                    listener.onCourseUpdated();
                }
                dismiss();
            }
            @Override
            public void onFailure(String error) {
                if (!isAdded() || getContext() == null) return;
                Toast.makeText(getContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
