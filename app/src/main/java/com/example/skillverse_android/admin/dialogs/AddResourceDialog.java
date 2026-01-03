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
public class AddResourceDialog extends DialogFragment {
    private String courseId;
    private String moduleId;
    private OnResourceAddedListener listener;
    private EditText etTitle, etUrl, etSize;
    private Spinner spinnerType;
    public interface OnResourceAddedListener {
        void onResourceAdded();
    }
    public static AddResourceDialog newInstance(String courseId, String moduleId) {
        AddResourceDialog dialog = new AddResourceDialog();
        dialog.courseId = courseId;
        dialog.moduleId = moduleId;
        return dialog;
    }
    public void setOnResourceAddedListener(OnResourceAddedListener listener) {
        this.listener = listener;
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_add_resource, null);
        etTitle = view.findViewById(R.id.etTitle);
        etUrl = view.findViewById(R.id.etUrl);
        etSize = view.findViewById(R.id.etSize);
        spinnerType = view.findViewById(R.id.spinnerType);
        String[] types = {"PDF", "Video", "Notes", "Quiz"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);
        builder.setView(view)
                .setTitle("Add Resource")
                .setPositiveButton("Create", (dialog, which) -> createResource())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        return builder.create();
    }
    private void createResource() {
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
        ModuleResource resource = new ModuleResource();
        resource.setTitle(title);
        resource.setType(type);
        resource.setUrl(url);
        resource.setSize(size.isEmpty() ? "N/A" : size);
        resource.setId(0);
        AdminFirestoreRepository.addResourceToModule(courseId, moduleId, resource, new AdminFirestoreRepository.DataCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "Resource added successfully", Toast.LENGTH_SHORT).show();
                    if (listener != null) {
                        listener.onResourceAdded();
                    }
                    dismiss();
                }
            }
            @Override
            public void onFailure(String error) {
                if (isAdded() && getContext() != null) {
                    Toast.makeText(getContext(), "Error adding resource: " + error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
