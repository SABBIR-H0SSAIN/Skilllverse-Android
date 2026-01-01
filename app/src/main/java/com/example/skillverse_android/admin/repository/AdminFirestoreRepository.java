package com.example.skillverse_android.admin.repository;
import com.example.skillverse_android.models.Course;
import com.example.skillverse_android.models.Instructor;
import com.example.skillverse_android.models.Module;
import com.example.skillverse_android.models.EnrollmentKey;
import com.example.skillverse_android.models.User;
import com.example.skillverse_android.models.ModuleResource;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class AdminFirestoreRepository {
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String COLLECTION_COURSES = "courses";
    private static final String COLLECTION_INSTRUCTORS = "instructors";
    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_ENROLLMENT_KEYS = "enrollmentKeys";
    private static final String COLLECTION_MODULES = "modules";
    private static final String COLLECTION_ENROLLMENTS = "enrollments";
    public static com.google.firebase.firestore.ListenerRegistration listenToCourses(
            com.google.firebase.firestore.EventListener<com.google.firebase.firestore.QuerySnapshot> listener) {
        return db.collection(COLLECTION_COURSES)
                .addSnapshotListener(listener);
    }
    public static void getAllCourses(DataCallback<List<Course>> callback) {
        android.util.Log.d("AdminFirestore", "getAllCourses: Starting query...");
        db.collection(COLLECTION_COURSES)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    android.util.Log.d("AdminFirestore", "getAllCourses: Query successful, documents: " + queryDocumentSnapshots.size());
                    List<Course> courses = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            android.util.Log.d("AdminFirestore", "Processing document: " + document.getId());
                            Course course = document.toObject(Course.class);
                            course.setId(document.getId());
                            courses.add(course);
                            android.util.Log.d("AdminFirestore", "Successfully added course: " + course.getTitle());
                        } catch (Exception e) {
                            android.util.Log.e("AdminFirestore", "Error parsing course " + document.getId() + ": " + e.getMessage(), e);
                        }
                    }
                    android.util.Log.d("AdminFirestore", "getAllCourses: Returning " + courses.size() + " courses");
                    callback.onSuccess(courses);
                })
                .addOnFailureListener(e -> {
                    android.util.Log.e("AdminFirestore", "getAllCourses: Query failed: " + e.getMessage(), e);
                    callback.onFailure(e.getMessage());
                });
    }
    public static void searchCourses(String query, DataCallback<List<Course>> callback) {
        db.collection(COLLECTION_COURSES)
                .orderBy("title")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Course> courses = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Course course = document.toObject(Course.class);
                        course.setId(document.getId());
                        courses.add(course);
                    }
                    callback.onSuccess(courses);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    public static void getCoursesByCategory(String category, DataCallback<List<Course>> callback) {
        db.collection(COLLECTION_COURSES)
                .whereEqualTo("category", category)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Course> courses = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Course course = document.toObject(Course.class);
                        course.setId(document.getId());
                        courses.add(course);
                    }
                    callback.onSuccess(courses);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    public static void createCourse(Course course, DataCallback<String> callback) {
        course.setCreatedAt(System.currentTimeMillis());
        course.setUpdatedAt(System.currentTimeMillis());
        course.setEnrollmentCount(0);
        course.setRating(0.0f);
        db.collection(COLLECTION_COURSES)
                .add(course)
                .addOnSuccessListener(documentReference -> {
                    callback.onSuccess(documentReference.getId());
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    public static void updateCourse(Course course, DataCallback<Void> callback) {
        course.setUpdatedAt(System.currentTimeMillis());
        db.collection(COLLECTION_COURSES)
                .document(course.getId())
                .set(course)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    public static void deleteCourse(String courseId, DataCallback<Void> callback) {
        db.collection(COLLECTION_COURSES)
                .document(courseId)
                .collection(COLLECTION_MODULES)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        document.getReference().delete();
                    }
                    db.collection(COLLECTION_COURSES)
                            .document(courseId)
                            .delete()
                            .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    public static com.google.firebase.firestore.ListenerRegistration listenToModules(
            String courseId,
            com.google.firebase.firestore.EventListener<com.google.firebase.firestore.QuerySnapshot> listener) {
        return db.collection(COLLECTION_COURSES)
                .document(courseId)
                .collection(COLLECTION_MODULES)
                .orderBy("order", Query.Direction.ASCENDING)
                .addSnapshotListener(listener);
    }
    public static void getModulesForCourse(String courseId, DataCallback<List<Module>> callback) {
        db.collection(COLLECTION_COURSES)
                .document(courseId)
                .collection(COLLECTION_MODULES)
                .orderBy("order", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Module> modules = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Module module = document.toObject(Module.class);
                        module.setDocumentId(document.getId());
                        modules.add(module);
                    }
                    callback.onSuccess(modules);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    public static void createModule(String courseId, Module module, DataCallback<String> callback) {
        Map<String, Object> moduleData = new HashMap<>();
        moduleData.put("title", module.getTitle());
        moduleData.put("description", module.description());
        moduleData.put("duration", module.getDuration());
        moduleData.put("order", module.getOrder());
        moduleData.put("youtubeVideoId", module.getYoutubeVideoId());
        moduleData.put("completed", false);
        db.collection(COLLECTION_COURSES)
                .document(courseId)
                .collection(COLLECTION_MODULES)
                .add(moduleData)
                .addOnSuccessListener(documentReference -> {
                    callback.onSuccess(documentReference.getId());
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    public static void updateModule(String courseId, String moduleId, Module module, DataCallback<Void> callback) {
        Map<String, Object> moduleData = new HashMap<>();
        moduleData.put("title", module.getTitle());
        moduleData.put("description", module.description());
        moduleData.put("duration", module.getDuration());
        moduleData.put("order", module.getOrder());
        moduleData.put("youtubeVideoId", module.getYoutubeVideoId());
        db.collection(COLLECTION_COURSES)
                .document(courseId)
                .collection(COLLECTION_MODULES)
                .document(moduleId)
                .update(moduleData)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    public static void deleteModule(String courseId, String moduleId, DataCallback<Void> callback) {
        db.collection(COLLECTION_COURSES)
                .document(courseId)
                .collection(COLLECTION_MODULES)
                .document(moduleId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    public static void addModuleToCourse(String courseId, Module module, DataCallback<Void> callback) {
        createModule(courseId, module, new DataCallback<String>() {
            @Override
            public void onSuccess(String moduleId) {
                callback.onSuccess(null);
            }
            @Override
            public void onFailure(String error) {
                callback.onFailure(error);
            }
        });
    }
    public static void updateModuleInCourse(String courseId, int position, Module module, DataCallback<Void> callback) {
        getModulesForCourse(courseId, new DataCallback<List<Module>>() {
            @Override
            public void onSuccess(List<Module> modules) {
                if (position < 0 || position >= modules.size()) {
                    callback.onFailure("Invalid module position");
                    return;
                }
                db.collection(COLLECTION_COURSES)
                        .document(courseId)
                        .collection(COLLECTION_MODULES)
                        .orderBy("order", Query.Direction.ASCENDING)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (position < queryDocumentSnapshots.size()) {
                                String moduleId = queryDocumentSnapshots.getDocuments().get(position).getId();
                                updateModule(courseId, moduleId, module, callback);
                            } else {
                                callback.onFailure("Module not found at position");
                            }
                        })
                        .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
            }
            @Override
            public void onFailure(String error) {
                callback.onFailure(error);
            }
        });
    }
    public static com.google.firebase.firestore.ListenerRegistration listenToResourcesForModule(
            String courseId, String moduleId,
            com.google.firebase.firestore.EventListener<com.google.firebase.firestore.QuerySnapshot> listener) {
        return db.collection(COLLECTION_COURSES)
                .document(courseId)
                .collection(COLLECTION_MODULES)
                .document(moduleId)
                .collection("resources")
                .addSnapshotListener(listener);
    }
    public static void getResourcesForModule(String courseId, String moduleId, DataCallback<List<ModuleResource>> callback) {
        db.collection(COLLECTION_COURSES)
                .document(courseId)
                .collection(COLLECTION_MODULES)
                .document(moduleId)
                .collection("resources")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ModuleResource> resources = new ArrayList<>();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        ModuleResource resource = document.toObject(ModuleResource.class);
                        resource.setDocumentId(document.getId());
                        resources.add(resource);
                    }
                    callback.onSuccess(resources);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    public static void addResourceToModule(String courseId, String moduleId, ModuleResource resource, DataCallback<Void> callback) {
        db.collection(COLLECTION_COURSES)
                .document(courseId)
                .collection(COLLECTION_MODULES)
                .document(moduleId)
                .collection("resources")
                .add(resource)
                .addOnSuccessListener(documentReference -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    public static void updateResourceInModule(String courseId, String moduleId, ModuleResource resource, DataCallback<Void> callback) {
        if (resource.getDocumentId() == null || resource.getDocumentId().isEmpty()) {
            callback.onFailure("Resource document ID is missing");
            return;
        }
        db.collection(COLLECTION_COURSES)
                .document(courseId)
                .collection(COLLECTION_MODULES)
                .document(moduleId)
                .collection("resources")
                .document(resource.getDocumentId())
                .set(resource)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    public static void deleteResourceFromModule(String courseId, String moduleId, String documentId, DataCallback<Void> callback) {
        if (documentId == null || documentId.isEmpty()) {
            callback.onFailure("Resource document ID is missing");
            return;
        }
        db.collection(COLLECTION_COURSES)
                .document(courseId)
                .collection(COLLECTION_MODULES)
                .document(moduleId)
                .collection("resources")
                .document(documentId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    public static void createInstructor(Instructor instructor, DataCallback<String> callback) {
        String instructorId = db.collection(COLLECTION_INSTRUCTORS).document().getId();
        instructor.setId(instructorId);
        Map<String, Object> instructorData = new HashMap<>();
        instructorData.put("id", instructorId);
        instructorData.put("name", instructor.getName());
        instructorData.put("title", instructor.getTitle());
        instructorData.put("bio", instructor.getBio());
        instructorData.put("profileImageUrl", instructor.getProfileImageUrl());
        instructorData.put("email", instructor.getEmail());
        instructorData.put("expertise", instructor.getExpertise());
        instructorData.put("rating", instructor.getRating());
        instructorData.put("coursesCount", instructor.getCoursesCount());
        instructorData.put("createdAt", instructor.getCreatedAt());
        db.collection(COLLECTION_INSTRUCTORS)
                .document(instructorId)
                .set(instructorData)
                .addOnSuccessListener(aVoid -> callback.onSuccess(instructorId))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    public static com.google.firebase.firestore.ListenerRegistration listenToInstructors(
            com.google.firebase.firestore.EventListener<com.google.firebase.firestore.QuerySnapshot> listener) {
        return db.collection(COLLECTION_INSTRUCTORS)
                .orderBy("name", Query.Direction.ASCENDING)
                .addSnapshotListener(listener);
    }
    public static void getAllInstructors(DataCallback<List<Instructor>> callback) {
        db.collection(COLLECTION_INSTRUCTORS)
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Instructor> instructors = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Instructor instructor = document.toObject(Instructor.class);
                        instructor.setId(document.getId());
                        instructors.add(instructor);
                    }
                    callback.onSuccess(instructors);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    public static void updateInstructor(Instructor instructor, DataCallback<Void> callback) {
        db.collection(COLLECTION_INSTRUCTORS)
                .document(instructor.getId())
                .set(instructor)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    public static void deleteInstructor(String instructorId, DataCallback<Void> callback) {
        db.collection(COLLECTION_INSTRUCTORS)
                .document(instructorId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    public static com.google.firebase.firestore.ListenerRegistration listenToKeys(
            com.google.firebase.firestore.EventListener<com.google.firebase.firestore.QuerySnapshot> listener) {
        return db.collection(COLLECTION_ENROLLMENT_KEYS)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener(listener);
    }
    public static void getAllKeys(DataCallback<List<EnrollmentKey>> callback) {
        db.collection(COLLECTION_ENROLLMENT_KEYS)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<EnrollmentKey> keys = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        EnrollmentKey key = document.toObject(EnrollmentKey.class);
                        key.setId(document.getId());
                        keys.add(key);
                    }
                    callback.onSuccess(keys);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    public static void generateKeys(String courseId, String courseTitle, int count, Long expiresAt, String createdBy, DataCallback<List<EnrollmentKey>> callback) {
        List<EnrollmentKey> generatedKeys = new ArrayList<>();
        long createdAt = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            String keyCode = generateKeyCode();
            Map<String, Object> keyData = new HashMap<>();
            keyData.put("key", keyCode);
            keyData.put("courseId", courseId);
            keyData.put("courseTitle", courseTitle);
            keyData.put("isUsed", false);
            keyData.put("usedBy", null);
            keyData.put("usedAt", null);
            keyData.put("createdAt", createdAt);
            keyData.put("createdBy", createdBy);
            if (expiresAt != null) {
                keyData.put("expiresAt", expiresAt);
            }
            EnrollmentKey key = new EnrollmentKey();
            key.setKey(keyCode);
            key.setCourseId(courseId);
            key.setCourseTitle(courseTitle);
            key.setUsed(false);
            key.setCreatedAt(createdAt);
            key.setCreatedBy(createdBy);
            if (expiresAt != null) {
                key.setExpiresAt(expiresAt);
            }
            db.collection(COLLECTION_ENROLLMENT_KEYS)
                    .add(keyData)
                    .addOnSuccessListener(documentReference -> {
                        key.setId(documentReference.getId());
                        generatedKeys.add(key);
                        if (generatedKeys.size() == count) {
                            callback.onSuccess(generatedKeys);
                        }
                    })
                    .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
        }
    }
    public static void deleteKey(String keyId, DataCallback<Void> callback) {
        db.collection(COLLECTION_ENROLLMENT_KEYS)
                .document(keyId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    public static void getAllUsers(DataCallback<List<User>> callback) {
        db.collection(COLLECTION_USERS)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> users = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        User user = document.toObject(User.class);
                        user.setUserId(document.getId());
                        users.add(user);
                    }
                    callback.onSuccess(users);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    public static void updateUserRole(String userId, String newRole, DataCallback<Void> callback) {
        db.collection(COLLECTION_USERS)
                .document(userId)
                .update("role", newRole)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    public static void deleteUser(String userId, DataCallback<Void> callback) {
        db.collection(COLLECTION_USERS)
                .document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    private static String generateKeyCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder key = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            key.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        key.append("-");
        for (int i = 0; i < 4; i++) {
            key.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        key.append("-");
        for (int i = 0; i < 4; i++) {
            key.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return key.toString();
    }
    public interface DataCallback<T> {
        void onSuccess(T data);
        void onFailure(String error);
    }
}
