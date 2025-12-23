package com.example.skillverse_android.adapters;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.skillverse_android.CourseDetailActivity;
import com.example.skillverse_android.R;
import com.example.skillverse_android.models.Course;
import com.bumptech.glide.Glide;
import android.widget.ImageView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
public class CourseAdapter extends RecyclerView.Adapter<CourseAdapter.CourseViewHolder> {
    private List<Course> courses;
    private OnCourseClickListener listener;
    private boolean showPrice = false;
    private boolean showProgress = false;
    public interface OnCourseClickListener {
        void onCourseClick(Course course);
    }
    public CourseAdapter(OnCourseClickListener listener) {
        this.courses = new ArrayList<>();
        this.listener = listener;
    }
    public CourseAdapter(OnCourseClickListener listener, boolean showPrice) {
        this.courses = new ArrayList<>();
        this.listener = listener;
        this.showPrice = showPrice;
    }
    public void setCourses(List<Course> courses) {
        this.courses = courses;
        notifyDataSetChanged();
    }
    public void setShowPrice(boolean showPrice) {
        this.showPrice = showPrice;
        notifyDataSetChanged();
    }
    public void setShowProgress(boolean showProgress) {
        this.showProgress = showProgress;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public CourseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_course_card, parent, false);
        return new CourseViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull CourseViewHolder holder, int position) {
        Course course = courses.get(position);
        holder.bind(course, showPrice, showProgress);
    }
    @Override
    public int getItemCount() {
        return courses != null ? courses.size() : 0;
    }
    class CourseViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvInstructor, tvDuration, tvLessons, tvDifficulty, tvPrice;
        ImageView ivCourseImage;
        TextView tvProgressPercent;
        ProgressBar progressBar;
        LinearLayout llProgress;
        CourseViewHolder(@NonNull View itemView) {
            super(itemView);
            ivCourseImage = itemView.findViewById(R.id.ivCourseImage);
            tvTitle = itemView.findViewById(R.id.tvCourseTitle);
            tvInstructor = itemView.findViewById(R.id.tvInstructor);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvLessons = itemView.findViewById(R.id.tvLessons);
            tvDifficulty = itemView.findViewById(R.id.tvDifficulty);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvProgressPercent = itemView.findViewById(R.id.tvProgressPercent);
            progressBar = itemView.findViewById(R.id.progressBar);
            llProgress = itemView.findViewById(R.id.llProgress);
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Course course = courses.get(position);
                    if (course.getId() == null) {
                        return;
                    }
                    if (listener != null) {
                        listener.onCourseClick(course);
                    }
                }
            });
        }
        void bind(Course course, boolean showPrice, boolean showProgress) {
            tvTitle.setText(course.getTitle());
            tvDuration.setText(course.getDuration() + " hours");
            tvLessons.setText(course.getLessonsCount() + " lessons");
            int instructorCount = course.getInstructorIds().size();
            if (instructorCount > 0) {
                tvInstructor.setText(instructorCount == 1 ? "1 Instructor" : instructorCount + " Instructors");
            } else {
                tvInstructor.setText("Instructor");
            }
            tvDifficulty.setText(course.getDifficulty());
            if (showPrice) {
                tvPrice.setText(String.format(Locale.US, "৳%d", (int) course.getPrice()));
                tvPrice.setVisibility(View.VISIBLE);
            } else {
                tvPrice.setVisibility(View.GONE);
            }
            if (showProgress) {
                int progress = course.getProgress();
                tvProgressPercent.setText(progress + "%");
                progressBar.setProgress(progress);
                llProgress.setVisibility(View.VISIBLE);
            } else {
                llProgress.setVisibility(View.GONE);
            }
            
            if (course.getImageUrl() != null && !course.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                    .load(course.getImageUrl())
                    .centerCrop()
                    .placeholder(R.drawable.gradient_blue)
                    .into(ivCourseImage);
            } else {
                ivCourseImage.setImageResource(R.drawable.gradient_blue);
            }
        }
    }
}
