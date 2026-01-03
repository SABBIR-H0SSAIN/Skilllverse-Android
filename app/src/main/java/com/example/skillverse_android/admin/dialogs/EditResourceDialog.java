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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.example.skillverse_android.R;
import com.example.skillverse_android.admin.repository.AdminFirestoreRepository;
import com.example.skillverse_android.models.ModuleResource;
public class EditResourceDialog extends DialogFragment {
    private String courseId;
    private String moduleId;
    private ModuleResource resource;
    private int position;
    private OnResourceUpdatedListener listener;
    private EditText etTitle, etUrl, etSize;
    private Spinner spinnerType;
    public interface OnResourceUpdatedListener {
        void onResourceUpdated();
    }
    public static EditResourceDialog newInstance(String courseId, String moduleId, ModuleResource resource, int position) {
        EditResourceDialog dialog = new EditResourceDialog();
        dialog.courseId = courseId;
        dialog.moduleId = moduleId;
        dialog.resource = resource;
        dialog.position = position;
        return dialog;
    }
    public void setOnResourceUpdatedListener(OnResourceUpdatedListener listener) {
        this.listener = listener;
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit_resource, null);
        etTitle = view.findViewById(R.id.etTitle);
        etUrl = view.findViewById(R.id.etUrl);
        etSize = view.findViewById(R.id.etSize);
        spinnerType = view.findViewById(R.id.spinnerType);
        String[] types = {"PDF", "Video", "Notes", "Quiz"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);
        if (resource != null) {
            etTitle.setText(resource.getTitle());
            etUrl.setText(resource.getUrl());
            etSize.setText(resource.getSize());
            String type = resource.getType();
            for (int i = 0; i < types.length; i++) {
                if (types[i].equals(type)) {
                    spinnerType.setSelection(i);
                    break;
                }
            }
        }
        builder.setView(view)
                .setTitle("Edit Resource")
                .setPositiveButton("Save", (dialog, which) -> updateResource())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        return builder.create();
    }
    private void updateResource() {
        String title = etTitle.getText().toString().trim();
        String url = etUrl.getText().toString().trim();
        String size = etSize.getText().toString().trim();
        String type = spinnerType.getSelectedItem().toString();
        if (title.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a title", Toast.LENGTH_SHORT).show();
            return;
        }
        if (url.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a URL", Toast.LENGTH_SHORT).show();
            return;
        }
        resource.setTitle(title);
        resource.setType(type);
        resource.setUrl(url);
        resource.setSize(size.isEmpty() ? "N/A" : size);
        AdminFirestoreRepository.updateResourceInModule(courseId, moduleId, resource, new AdminFirestoreRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Resource updated successfully", Toast.LENGTH_SHORT).show();
                }
                if (listener != null) {
                    listener.onResourceUpdated();
                }
                dismiss();
            }
            @Override
            public void onFailure(String error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Error updating resource: " + error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
