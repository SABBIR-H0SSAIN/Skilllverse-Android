package com.example.skillverse_android.admin.adapters;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.skillverse_android.R;
import com.example.skillverse_android.models.Module;
import java.util.List;
public class AdminModuleAdapter extends RecyclerView.Adapter<AdminModuleAdapter.ViewHolder> {
    private List<Module> modules;
    private ModuleActionListener listener;
    public interface ModuleActionListener {
        void onEdit(Module module, int position);
        void onDelete(Module module, int position);
        void onManageResources(Module module, int position);
    }
    public AdminModuleAdapter(List<Module> modules, ModuleActionListener listener) {
        this.modules = modules;
        this.listener = listener;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_module, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Module module = modules.get(position);
        holder.tvTitle.setText(module.getTitle() != null ? module.getTitle() : "Untitled Module");
        holder.tvDescription.setText(module.description() != null ? module.description() : "No description");
        holder.tvDuration.setText(module.getDuration() + " minutes");
        holder.tvOrder.setText("Order: " + module.getOrder());
        String videoId = module.getYoutubeVideoId();
        if (videoId != null && !videoId.isEmpty()) {
            holder.tvVideo.setText("Video: " + videoId);
            holder.tvVideo.setVisibility(View.VISIBLE);
        } else {
            holder.tvVideo.setVisibility(View.GONE);
        }
        holder.btnResources.setOnClickListener(v -> listener.onManageResources(module, position));
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(module, position));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(module, position));
    }
    @Override
    public int getItemCount() {
        return modules.size();
    }
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvDuration, tvOrder, tvVideo;
        android.widget.Button btnResources;
        ImageButton btnEdit, btnDelete;
        ViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvOrder = itemView.findViewById(R.id.tvOrder);
            tvVideo = itemView.findViewById(R.id.tvVideo);
            btnResources = itemView.findViewById(R.id.btnResources);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
