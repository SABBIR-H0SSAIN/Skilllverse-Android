package com.example.skillverse_android.adapters;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.skillverse_android.R;
import com.example.skillverse_android.models.Module;
import java.util.List;
public class ModuleListAdapter extends RecyclerView.Adapter<ModuleListAdapter.ModuleViewHolder> {
    private List<Module> modules;
    private OnModuleClickListener listener;
    public interface OnModuleClickListener {
        void onModuleClick(Module module);
    }
    public ModuleListAdapter(List<Module> modules, OnModuleClickListener listener) {
        this.modules = modules;
        this.listener = listener;
    }
    @NonNull
    @Override
    public ModuleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_module, parent, false);
        return new ModuleViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ModuleViewHolder holder, int position) {
        Module module = modules.get(position);
        holder.bind(module, position);
    }
    @Override
    public int getItemCount() {
        return modules.size();
    }
    class ModuleViewHolder extends RecyclerView.ViewHolder {
        TextView tvModuleIcon, tvModuleTitle, tvModuleDuration, tvCompletionStatus;
        ModuleViewHolder(View itemView) {
            super(itemView);
            tvModuleIcon = itemView.findViewById(R.id.tvModuleIcon);
            tvModuleTitle = itemView.findViewById(R.id.tvModuleTitle);
            tvModuleDuration = itemView.findViewById(R.id.tvModuleDuration);
            tvCompletionStatus = itemView.findViewById(R.id.tvCompletionStatus);
        }
        void bind(Module module, int position) {
            tvModuleTitle.setText((position + 1) + ". " + module.getTitle());
            tvModuleDuration.setText("⏱️ " + module.getDuration() + " min");
            if (module.isCompleted()) {
                tvCompletionStatus.setVisibility(View.VISIBLE);
                tvModuleIcon.setText("✅");
            } else {
                tvCompletionStatus.setVisibility(View.GONE);
                tvModuleIcon.setText("📖");
            }
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onModuleClick(module);
                }
            });
        }
    }
}
