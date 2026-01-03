package com.example.skillverse_android.admin.adapters;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.skillverse_android.R;
import com.example.skillverse_android.models.Instructor;
import java.util.List;
public class AdminInstructorAdapter extends RecyclerView.Adapter<AdminInstructorAdapter.ViewHolder> {
    private List<Instructor> instructors;
    private InstructorActionListener listener;
    public interface InstructorActionListener {
        void onEdit(Instructor instructor);
        void onDelete(Instructor instructor);
    }
    public AdminInstructorAdapter(List<Instructor> instructors, InstructorActionListener listener) {
        this.instructors = instructors;
        this.listener = listener;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_instructor, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Instructor instructor = instructors.get(position);
        holder.tvName.setText(instructor.getName() != null ? instructor.getName() : "Unknown");
        holder.tvTitle.setText(instructor.getTitle() != null ? instructor.getTitle() : "No title");
        holder.tvEmail.setText(instructor.getEmail() != null ? instructor.getEmail() : "No email");
        holder.tvCourses.setText(instructor.getCoursesCount() + " courses");
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(instructor));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(instructor));
    }
    @Override
    public int getItemCount() {
        return instructors.size();
    }
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvTitle, tvEmail, tvCourses;
        ImageButton btnEdit, btnDelete;
        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvEmail = itemView.findViewById(R.id.tvEmail);
            tvCourses = itemView.findViewById(R.id.tvCourses);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
