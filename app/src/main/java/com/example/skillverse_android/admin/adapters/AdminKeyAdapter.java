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
public class AdminKeyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_HEADER = 1;

    private List<Object> items;
    private OnDeleteListener deleteListener;
    private Context context;

    public interface OnDeleteListener {
        void onDelete(EnrollmentKey key);
    }

    public AdminKeyAdapter(List<Object> items, OnDeleteListener deleteListener) {
        this.items = items;
        this.deleteListener = deleteListener;
    }

    @Override
    public int getItemViewType(int position) {
        if (items.get(position) instanceof String) {
            return TYPE_HEADER;
        }
        return TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        if (viewType == TYPE_HEADER) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_admin_key, parent, false);
            return new KeyViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_HEADER) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            headerHolder.tvTitle.setText((String) items.get(position));
        } else {
            KeyViewHolder keyHolder = (KeyViewHolder) holder;
            EnrollmentKey key = (EnrollmentKey) items.get(position);
            
            keyHolder.tvKeyCode.setText(key.getKey() != null ? key.getKey() : "N/A");
            
            String title = key.getCourseTitle();
            keyHolder.tvCourseTitle.setText("Course: " + (title != null && !title.isEmpty() ? title : "Unknown"));
            
            if (key.isUsed()) {
                keyHolder.tvStatus.setText("Used");
            } else {
                keyHolder.tvStatus.setText("Unused");
            }
            
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            String createdDate = key.getCreatedAt() > 0 ? sdf.format(new Date(key.getCreatedAt())) : "Unknown";
            keyHolder.tvCreated.setText("Created: " + createdDate);
            
            keyHolder.btnCopy.setOnClickListener(v -> copyToClipboard(key.getKey()));
            
            if (key.isUsed()) {
                keyHolder.btnDelete.setVisibility(View.GONE);
            } else {
                keyHolder.btnDelete.setVisibility(View.VISIBLE);
                keyHolder.btnDelete.setOnClickListener(v -> deleteListener.onDelete(key));
            }
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
        return items.size();
    }

    static class KeyViewHolder extends RecyclerView.ViewHolder {
        TextView tvKeyCode, tvCourseTitle, tvStatus, tvCreated;
        ImageButton btnCopy, btnDelete;

        KeyViewHolder(View itemView) {
            super(itemView);
            tvKeyCode = itemView.findViewById(R.id.tvKeyCode);
            tvCourseTitle = itemView.findViewById(R.id.tvCourseTitle);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvCreated = itemView.findViewById(R.id.tvCreated);
            btnCopy = itemView.findViewById(R.id.btnCopy);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;

        HeaderViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvHeaderTitle);
        }
    }
}
