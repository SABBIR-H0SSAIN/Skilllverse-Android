package com.example.skillverse_android.admin.adapters;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.skillverse_android.R;
import com.example.skillverse_android.models.User;
import java.util.List;
public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.ViewHolder> {
    private List<User> users;
    private UserActionListener listener;
    public interface UserActionListener {
        void onChangeRole(User user);
        void onDelete(User user);
    }
    public AdminUserAdapter(List<User> users, UserActionListener listener) {
        this.users = users;
        this.listener = listener;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_user, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User user = users.get(position);
        holder.tvName.setText(user.getName() != null ? user.getName() : "Unknown");
        holder.tvEmail.setText(user.getEmail() != null ? user.getEmail() : "No email");
        String role = user.getRole();
        holder.tvRole.setText(role != null ? role.toUpperCase() : "STUDENT");
        holder.btnChangeRole.setOnClickListener(v -> listener.onChangeRole(user));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(user));
    }
    @Override
    public int getItemCount() {
        return users.size();
    }
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvRole;
        ImageButton btnChangeRole, btnDelete;
        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvRole = itemView.findViewById(R.id.tvRole);
            btnChangeRole = itemView.findViewById(R.id.btnChangeRole);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
