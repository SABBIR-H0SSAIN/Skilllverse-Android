package com.example.skillverse_android.utils;
import android.content.Context;
import android.content.SharedPreferences;
public class ProgressCacheManager {
    private static final String PREF_NAME = "progress_cache";
    private static ProgressCacheManager instance;
    private SharedPreferences preferences;
    private ProgressCacheManager(Context context) {
        preferences = context.getApplicationContext()
            .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    public static synchronized ProgressCacheManager getInstance(Context context) {
        if (instance == null) {
            instance = new ProgressCacheManager(context);
        }
        return instance;
    }
    public void cacheModuleProgress(String userId, String courseId, String moduleId, boolean completed) {
        String key = getModuleKey(userId, courseId, moduleId);
        preferences.edit().putBoolean(key, completed).apply();
    }
    public void cacheModuleProgress(String userId, int courseId, int moduleId, boolean completed) {
        cacheModuleProgress(userId, String.valueOf(courseId), String.valueOf(moduleId), completed);
    }
    public Boolean getCachedModuleProgress(String userId, String courseId, String moduleId) {
        String key = getModuleKey(userId, courseId, moduleId);
        if (preferences.contains(key)) {
            return preferences.getBoolean(key, false);
        }
        return null;
    }
    public Boolean getCachedModuleProgress(String userId, int courseId, int moduleId) {
        return getCachedModuleProgress(userId, String.valueOf(courseId), String.valueOf(moduleId));
    }
    public void cacheCourseProgress(String userId, String courseId, int progress) {
        String key = getCourseKey(userId, courseId);
        preferences.edit().putInt(key, progress).apply();
    }
    public void cacheCourseProgress(String userId, int courseId, int progress) {
        cacheCourseProgress(userId, String.valueOf(courseId), progress);
    }
    public Integer getCachedCourseProgress(String userId, String courseId) {
        String key = getCourseKey(userId, courseId);
        if (preferences.contains(key)) {
            return preferences.getInt(key, 0);
        }
        return null;
    }
    public Integer getCachedCourseProgress(String userId, int courseId) {
        return getCachedCourseProgress(userId, String.valueOf(courseId));
    }
    public void clearUserCache(String userId) {
        SharedPreferences.Editor editor = preferences.edit();
        for (String key : preferences.getAll().keySet()) {
            if (key.startsWith(userId + "_")) {
                editor.remove(key);
            }
        }
        editor.apply();
    }
    private String getModuleKey(String userId, String courseId, String moduleId) {
        return userId + "_course_" + courseId + "_module_" + moduleId;
    }
    private String getModuleKey(String userId, int courseId, int moduleId) {
        return getModuleKey(userId, String.valueOf(courseId), String.valueOf(moduleId));
    }
    private String getCourseKey(String userId, String courseId) {
        return userId + "_course_" + courseId + "_progress";
    }
    private String getCourseKey(String userId, int courseId) {
        return getCourseKey(userId, String.valueOf(courseId));
    }
}
