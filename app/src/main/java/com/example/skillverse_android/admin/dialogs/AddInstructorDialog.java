package com.example.skillverse_android.admin.dialogs;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.example.skillverse_android.R;
import com.example.skillverse_android.admin.repository.AdminFirestoreRepository;
import com.example.skillverse_android.models.Instructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
public class AddInstructorDialog extends DialogFragment {
    private OnInstructorAddedListener listener;
    private EditText etName, etTitle, etBio, etEmail, etExpertise, etImageUrl;
    public interface OnInstructorAddedListener {
        void onInstructorAdded(Instructor instructor);
    }
    public static AddInstructorDialog newInstance() {
        return new AddInstructorDialog();
    }
    public void setOnInstructorAddedListener(OnInstructorAddedListener listener) {
        this.listener = listener;
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_instructor, null);
        etName = view.findViewById(R.id.etName);
        etTitle = view.findViewById(R.id.etTitle);
        etBio = view.findViewById(R.id.etBio);
        etEmail = view.findViewById(R.id.etEmail);
        etExpertise = view.findViewById(R.id.etExpertise);
        etImageUrl = view.findViewById(R.id.etImageUrl);
        builder.setView(view)
                .setTitle("Add New Instructor")
                .setPositiveButton("Create", (dialog, which) -> createInstructor())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        return builder.create();
    }
    private void createInstructor() {
        String name = etName.getText().toString().trim();
        String title = etTitle.getText().toString().trim();
        String bio = etBio.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String expertiseStr = etExpertise.getText().toString().trim();
        String imageUrl = etImageUrl.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a name", Toast.LENGTH_SHORT).show();
            return;
        }
        if (title.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a title", Toast.LENGTH_SHORT).show();
            return;
        }
        if (email.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter an email", Toast.LENGTH_SHORT).show();
            return;
        }
        List<String> expertise = new ArrayList<>();
        if (!expertiseStr.isEmpty()) {
            String[] parts = expertiseStr.split(",");
            for (String part : parts) {
                expertise.add(part.trim());
            }
        }
        Instructor instructor = new Instructor(
            "",
            name,
            title,
            bio,
            imageUrl,
            email,
            expertise,
            0.0f,
            0,
            System.currentTimeMillis()
        );
        AdminFirestoreRepository.createInstructor(instructor, new AdminFirestoreRepository.DataCallback<String>() {
            @Override
            public void onSuccess(String instructorId) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Instructor added successfully", Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onInstructorAdded(instructor);
                    }
                    dismiss();
                }
            }
            @Override
            public void onFailure(String error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error adding instructor: " + error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
