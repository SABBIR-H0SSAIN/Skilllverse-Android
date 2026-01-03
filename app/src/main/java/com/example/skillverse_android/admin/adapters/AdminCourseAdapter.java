package com.example.skillverse_android.admin.adapters;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.skillverse_android.R;
import com.example.skillverse_android.models.Course;
import java.util.List;
public class AdminCourseAdapter extends RecyclerView.Adapter<AdminCourseAdapter.ViewHolder> {
    private List<Course> courses;
    private CourseActionListener listener;
    public interface CourseActionListener {
        void onEdit(Course course);
        void onDelete(Course course);
        void onManageModules(Course course);
    }
    public AdminCourseAdapter(List<Course> courses, CourseActionListener listener) {
        this.courses = courses;
        this.listener = listener;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_course, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Course course = courses.get(position);
        holder.tvTitle.setText(course.getTitle() != null ? course.getTitle() : "Untitled");
        String category = course.getCategory() != null ? course.getCategory() : "Unknown";
        String difficulty = course.getDifficulty() != null ? course.getDifficulty() : "Unknown";
        holder.tvCategory.setText(category + " • " + difficulty);
        holder.tvStatus.setText(course.isPublished() ? "Published" : "Draft");
        if (course.getImageUrl() != null && !course.getImageUrl().isEmpty()) {
            com.bumptech.glide.Glide.with(holder.itemView.getContext())
                    .load(course.getImageUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(holder.ivThumbnail);
        } else {
            holder.ivThumbnail.setImageResource(android.R.drawable.ic_menu_gallery);
        }
        holder.btnManageModules.setOnClickListener(v -> listener.onManageModules(course));
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(course));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(course));
    }
    @Override
    public int getItemCount() {
        return courses.size();
    }
    static class ViewHolder extends RecyclerView.ViewHolder {
        com.google.android.material.imageview.ShapeableImageView ivThumbnail;
        TextView tvTitle, tvCategory, tvStatus;
        android.widget.Button btnManageModules;
        ImageButton btnEdit, btnDelete;
        ViewHolder(View itemView) {
            super(itemView);
            ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnManageModules = itemView.findViewById(R.id.btnManageModules);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
