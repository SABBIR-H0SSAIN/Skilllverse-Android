package com.example.skillverse_android.utils;
import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Set;
public class EnrollmentManager {
    private static final String PREF_NAME = "enrollments";
    private static final String KEY_ENROLLED_COURSES = "enrolled_courses";
    private static EnrollmentManager instance;
    private SharedPreferences preferences;
    private EnrollmentManager(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    public static synchronized EnrollmentManager getInstance(Context context) {
        if (instance == null) {
            instance = new EnrollmentManager(context);
        }
        return instance;
    }
    public boolean enrollInCourse(String courseId) {
        Set<String> enrolledCourses = getEnrolledCourses();
        enrolledCourses.add(courseId);
        return preferences.edit()
                .putStringSet(KEY_ENROLLED_COURSES, enrolledCourses)
                .commit();
    }
    public boolean isEnrolled(String courseId) {
        return getEnrolledCourses().contains(courseId);
    }
    public Set<String> getEnrolledCourses() {
        return new HashSet<>(preferences.getStringSet(KEY_ENROLLED_COURSES, new HashSet<>()));
    }
    public boolean unenrollFromCourse(String courseId) {
        Set<String> enrolledCourses = getEnrolledCourses();
        enrolledCourses.remove(courseId);
        return preferences.edit()
                .putStringSet(KEY_ENROLLED_COURSES, enrolledCourses)
                .commit();
    }
}
