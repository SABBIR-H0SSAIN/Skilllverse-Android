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
public class EditInstructorDialog extends DialogFragment {
    private Instructor instructor;
    private OnInstructorUpdatedListener listener;
    private EditText etName, etTitle, etBio, etEmail, etExpertise, etImageUrl;
    public interface OnInstructorUpdatedListener {
        void onInstructorUpdated();
    }
    public static EditInstructorDialog newInstance(Instructor instructor) {
        EditInstructorDialog dialog = new EditInstructorDialog();
        dialog.instructor = instructor;
        return dialog;
    }
    public void setOnInstructorUpdatedListener(OnInstructorUpdatedListener listener) {
        this.listener = listener;
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit_instructor, null);
        etName = view.findViewById(R.id.etName);
        etTitle = view.findViewById(R.id.etTitle);
        etBio = view.findViewById(R.id.etBio);
        etEmail = view.findViewById(R.id.etEmail);
        etExpertise = view.findViewById(R.id.etExpertise);
        etImageUrl = view.findViewById(R.id.etImageUrl);
        if (instructor != null) {
            etName.setText(instructor.getName());
            etTitle.setText(instructor.getTitle());
            etBio.setText(instructor.getBio());
            etEmail.setText(instructor.getEmail());
            etImageUrl.setText(instructor.getProfileImageUrl());
            if (instructor.getExpertise() != null && !instructor.getExpertise().isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < instructor.getExpertise().size(); i++) {
                    sb.append(instructor.getExpertise().get(i));
                    if (i < instructor.getExpertise().size() - 1) {
                        sb.append(", ");
                    }
                }
                etExpertise.setText(sb.toString());
            }
        }
        builder.setView(view)
                .setTitle("Edit Instructor")
                .setPositiveButton("Save", (dialog, which) -> updateInstructor())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        return builder.create();
    }
    private void updateInstructor() {
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
        instructor.setName(name);
        instructor.setTitle(title);
        instructor.setBio(bio);
        instructor.setEmail(email);
        instructor.setExpertise(expertise);
        instructor.setProfileImageUrl(imageUrl);
        AdminFirestoreRepository.updateInstructor(instructor, new AdminFirestoreRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Instructor updated successfully", Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onInstructorUpdated();
                    }
                    dismiss();
                }
            }
            @Override
            public void onFailure(String error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error updating instructor: " + error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
