package com.example.skillverse_android.admin.adapters;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.skillverse_android.R;
import com.example.skillverse_android.models.EnrollmentKey;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
public class AdminKeyAdapter extends RecyclerView.Adapter<AdminKeyAdapter.ViewHolder> {
    private List<EnrollmentKey> keys;
    private OnDeleteListener deleteListener;
    private Context context;
    public interface OnDeleteListener {
        void onDelete(EnrollmentKey key);
    }
    public AdminKeyAdapter(List<EnrollmentKey> keys, OnDeleteListener deleteListener) {
        this.keys = keys;
        this.deleteListener = deleteListener;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_admin_key, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EnrollmentKey key = keys.get(position);
        holder.tvKeyCode.setText(key.getKey() != null ? key.getKey() : "N/A");
        String title = key.getCourseTitle();
        holder.tvCourseTitle.setText("Course: " + (title != null && !title.isEmpty() ? title : "Unknown"));
        if (key.isUsed()) {
            holder.tvStatus.setText("Used");
        } else {
            holder.tvStatus.setText("Unused");
        }
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        String createdDate = key.getCreatedAt() > 0 ? sdf.format(new Date(key.getCreatedAt())) : "Unknown";
        holder.tvCreated.setText("Created: " + createdDate);
        holder.btnCopy.setOnClickListener(v -> copyToClipboard(key.getKey()));
        if (key.isUsed()) {
            holder.btnDelete.setVisibility(View.GONE);
        } else {
            holder.btnDelete.setVisibility(View.VISIBLE);
            holder.btnDelete.setOnClickListener(v -> deleteListener.onDelete(key));
        }
    }
    private void copyToClipboard(String key) {
        if (context != null && key != null) {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Enrollment Key", key);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, "Key copied to clipboard", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public int getItemCount() {
        return keys.size();
    }
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvKeyCode, tvCourseTitle, tvStatus, tvCreated;
        ImageButton btnCopy, btnDelete;
        ViewHolder(View itemView) {
            super(itemView);
            tvKeyCode = itemView.findViewById(R.id.tvKeyCode);
            tvCourseTitle = itemView.findViewById(R.id.tvCourseTitle);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvCreated = itemView.findViewById(R.id.tvCreated);
            btnCopy = itemView.findViewById(R.id.btnCopy);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
