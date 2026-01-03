package com.example.skillverse_android.admin.adapters;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.skillverse_android.R;
import com.example.skillverse_android.models.ModuleResource;
import java.util.List;
public class AdminResourceAdapter extends RecyclerView.Adapter<AdminResourceAdapter.ViewHolder> {
    private List<ModuleResource> resources;
    private ResourceActionListener listener;
    public interface ResourceActionListener {
        void onEdit(ModuleResource resource, int position);
        void onDelete(ModuleResource resource, int position);
    }
    public AdminResourceAdapter(List<ModuleResource> resources, ResourceActionListener listener) {
        this.resources = resources;
        this.listener = listener;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_resource, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ModuleResource resource = resources.get(position);
        holder.tvTitle.setText(resource.getTitle() != null ? resource.getTitle() : "Untitled Resource");
        holder.tvType.setText(resource.getType() != null ? resource.getType() : "Unknown");
        holder.tvSize.setText(resource.getSize() != null ? resource.getSize() : "N/A");
        holder.tvIcon.setText(resource.getIcon());
        String url = resource.getUrl();
        if (url != null && !url.isEmpty()) {
            holder.tvUrl.setText(url);
            holder.tvUrl.setVisibility(View.VISIBLE);
        } else {
            holder.tvUrl.setVisibility(View.GONE);
        }
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(resource, position));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(resource, position));
    }
    @Override
    public int getItemCount() {
        return resources.size();
    }
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvType, tvSize, tvUrl, tvIcon;
        ImageButton btnEdit, btnDelete;
        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvType = itemView.findViewById(R.id.tvType);
            tvSize = itemView.findViewById(R.id.tvSize);
            tvUrl = itemView.findViewById(R.id.tvUrl);
            tvIcon = itemView.findViewById(R.id.tvIcon);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
