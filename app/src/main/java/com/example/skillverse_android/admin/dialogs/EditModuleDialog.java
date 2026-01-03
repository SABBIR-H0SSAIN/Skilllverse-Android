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
import com.example.skillverse_android.models.Module;
public class EditModuleDialog extends DialogFragment {
    private String courseId;
    private Module module;
    private int position;
    private OnModuleUpdatedListener listener;
    private EditText etTitle, etDescription, etDuration, etOrder, etVideoId;
    public interface OnModuleUpdatedListener {
        void onModuleUpdated();
    }
    public static EditModuleDialog newInstance(String courseId, Module module, int position) {
        EditModuleDialog dialog = new EditModuleDialog();
        dialog.courseId = courseId;
        dialog.module = module;
        dialog.position = position;
        return dialog;
    }
    public void setOnModuleUpdatedListener(OnModuleUpdatedListener listener) {
        this.listener = listener;
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit_module, null);
        etTitle = view.findViewById(R.id.etTitle);
        etDescription = view.findViewById(R.id.etDescription);
        etDuration = view.findViewById(R.id.etDuration);
        etOrder = view.findViewById(R.id.etOrder);
        etVideoId = view.findViewById(R.id.etVideoId);
        if (module != null) {
            etTitle.setText(module.getTitle());
            etDescription.setText(module.description());
            etDuration.setText(String.valueOf(module.getDuration()));
            etOrder.setText(String.valueOf(module.getOrder()));
            if (module.getYoutubeVideoId() != null) {
                etVideoId.setText(module.getYoutubeVideoId());
            }
        }
        builder.setView(view)
                .setTitle("Edit Module")
                .setPositiveButton("Save", (dialog, which) -> updateModule())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        return builder.create();
    }
    private void updateModule() {
        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String durationStr = etDuration.getText().toString().trim();
        String orderStr = etOrder.getText().toString().trim();
        String videoId = etVideoId.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a title", Toast.LENGTH_SHORT).show();
            return;
        }
        if (description.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a description", Toast.LENGTH_SHORT).show();
            return;
        }
        int duration = 30;
        int order = 1;
        try {
            if (!durationStr.isEmpty()) duration = Integer.parseInt(durationStr);
            if (!orderStr.isEmpty()) order = Integer.parseInt(orderStr);
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Invalid number format", Toast.LENGTH_SHORT).show();
            return;
        }
        module.setTitle(title);
        module.setDescription(description);
        module.setDuration(duration);
        module.setOrder(order);
        module.setYoutubeVideoId(videoId);
        AdminFirestoreRepository.updateModuleInCourse(courseId, position, module, new AdminFirestoreRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                if (!isAdded() || getContext() == null) return;
                Toast.makeText(getContext(), "Module updated successfully", Toast.LENGTH_SHORT).show();
                if (listener != null) {
                    listener.onModuleUpdated();
                }
                dismiss();
            }
            @Override
            public void onFailure(String error) {
                if (!isAdded() || getContext() == null) return;
                Toast.makeText(getContext(), "Error updating module: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }
}
