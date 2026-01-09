package com.example.skillverse_android.utils;
import com.example.skillverse_android.models.Course;
import com.example.skillverse_android.models.Instructor;
import com.example.skillverse_android.models.Module;
import com.example.skillverse_android.models.ModuleResource;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class FirestoreRepository {
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    public interface DataCallback<T> {
        void onSuccess(T data);
        void onFailure(String error);
    }
    public static void getCourses(DataCallback<List<Course>> callback) {
        android.util.Log.d("FirestoreRepo", "getCourses: Starting query...");
        db.collection("courses")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                android.util.Log.d("FirestoreRepo", "getCourses: Found " + queryDocumentSnapshots.size() + " documents");
                List<Course> courses = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    android.util.Log.d("FirestoreRepo", "Processing doc: " + document.getId() + " data: " + document.getData());
                    Course course = documentToCourse(document);
                    if (course != null) {
                        courses.add(course);
                        android.util.Log.d("FirestoreRepo", "Successfully parsed course: " + course.getTitle());
                    } else {
                        android.util.Log.e("FirestoreRepo", "Failed to parse course: " + document.getId());
                    }
                }
                android.util.Log.d("FirestoreRepo", "getCourses: Returning " + courses.size() + " courses");
                callback.onSuccess(courses);
            })
            .addOnFailureListener(e -> {
                android.util.Log.e("FirestoreRepo", "getCourses: Query failed: " + e.getMessage());
                callback.onFailure(e.getMessage());
            });
    }
    public static void getCourse(String courseDocId, DataCallback<Course> callback) {
        db.collection("courses")
            .document(courseDocId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Course course = documentToCourse(documentSnapshot);
                    if (course != null) {
                        callback.onSuccess(course);
                    } else {
                        callback.onFailure("Failed to parse course");
                    }
                } else {
                    callback.onFailure("Course not found");
                }
            })
            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    public static void getModulesForCourse(String courseId, DataCallback<List<Module>> callback) {
        db.collection("courses")
            .document(courseId)
            .collection("modules")
            .orderBy("order")
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
    public static void getModulesForCourse(int courseId, DataCallback<List<Module>> callback) {
        db.collection("courses")
            .document(String.valueOf(courseId))
            .collection("modules")
            .orderBy("order")
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
    public static void getResourcesForModule(String courseDocId, String moduleDocId, DataCallback<List<ModuleResource>> callback) {
        db.collection("courses")
            .document(courseDocId)
            .collection("modules")
            .document(moduleDocId)
            .collection("resources")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<ModuleResource> resources = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    ModuleResource resource = document.toObject(ModuleResource.class);
                    resource.setDocumentId(document.getId());
                    resources.add(resource);
                }
                callback.onSuccess(resources);
            })
            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    public static void getResourcesForModule(int courseId, int moduleId, DataCallback<List<ModuleResource>> callback) {
        db.collection("courses")
            .document(String.valueOf(courseId))
            .collection("modules")
            .document(String.valueOf(moduleId))
            .collection("resources")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<ModuleResource> resources = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    ModuleResource resource = documentToResource(document);
                    if (resource != null) {
                        resources.add(resource);
                    }
                }
                callback.onSuccess(resources);
            })
            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    public static void validateAndRedeemEnrollmentKey(String userId, String keyCode, DataCallback<String> callback) {
        db.collection("enrollmentKeys")
            .whereEqualTo("key", keyCode)
            .limit(1)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                if (querySnapshot.isEmpty()) {
                    callback.onFailure("Invalid enrollment key");
                    return;
                }
                var keyDoc = querySnapshot.getDocuments().get(0);
                String keyId = keyDoc.getId();
                Boolean isUsed = keyDoc.getBoolean("isUsed");
                if (isUsed == null) {
                    isUsed = keyDoc.getBoolean("used");
                }
                String courseId = keyDoc.getString("courseId");
                Long expiresAt = keyDoc.getLong("expiresAt");
                if (isUsed != null && isUsed) {
                    callback.onFailure("This enrollment key has already been used");
                    return;
                }
                if (expiresAt != null && expiresAt > 0 && System.currentTimeMillis() > expiresAt) {
                    callback.onFailure("This enrollment key has expired");
                    return;
                }
                if (courseId == null) {
                    callback.onFailure("Invalid key: no course associated");
                    return;
                }
                int courseIdInt = 1;
                try {
                    courseIdInt = Integer.parseInt(courseId);
                } catch (NumberFormatException e) {
                    courseIdInt = Math.abs(courseId.hashCode());
                }
                isEnrolled(userId, courseIdInt, new DataCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean enrolled) {
                        if (enrolled) {
                            callback.onFailure("You are already enrolled in this course");
                            return;
                        }
                        redeemKeyAndEnroll(userId, keyId, courseId, callback);
                    }
                    @Override
                    public void onFailure(String error) {
                        callback.onFailure("Error checking enrollment: " + error);
                    }
                });
            })
            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    private static void redeemKeyAndEnroll(String userId, String keyId, String courseId, DataCallback<String> callback) {
        Map<String, Object> keyUpdate = new HashMap<>();
        keyUpdate.put("isUsed", true);
        keyUpdate.put("usedBy", userId);
        keyUpdate.put("usedAt", System.currentTimeMillis());
        db.collection("enrollmentKeys").document(keyId)
            .update(keyUpdate)
            .addOnSuccessListener(aVoid -> {
                Map<String, Object> enrollment = new HashMap<>();
                enrollment.put("userId", userId);
                enrollment.put("courseId", courseId);
                enrollment.put("enrolledAt", System.currentTimeMillis());
                enrollment.put("keyId", keyId);
                db.collection("enrollments")
                    .add(enrollment)
                    .addOnSuccessListener(docRef -> {
                        db.collection("courses").document(courseId)
                            .update("enrollmentCount", com.google.firebase.firestore.FieldValue.increment(1))
                            .addOnSuccessListener(v -> callback.onSuccess(courseId))
                            .addOnFailureListener(e -> callback.onSuccess(courseId));
                    })
                    .addOnFailureListener(e -> {
                        Map<String, Object> rollback = new HashMap<>();
                        rollback.put("isUsed", false);
                        rollback.put("usedBy", null);
                        rollback.put("usedAt", null);
                        db.collection("enrollmentKeys").document(keyId).update(rollback);
                        callback.onFailure("Enrollment failed: " + e.getMessage());
                    });
            })
            .addOnFailureListener(e -> callback.onFailure("Failed to redeem key: " + e.getMessage()));
    }
    public static void generateEnrollmentKey(String courseId, String adminUserId, long expiresAt, DataCallback<String> callback) {
        String key = generateRandomKey();
        Map<String, Object> keyData = new HashMap<>();
        keyData.put("key", key);
        keyData.put("courseId", courseId);
        keyData.put("isUsed", false);
        keyData.put("usedBy", null);
        keyData.put("usedAt", null);
        keyData.put("createdAt", System.currentTimeMillis());
        keyData.put("createdBy", adminUserId);
        keyData.put("expiresAt", expiresAt);
        db.collection("enrollmentKeys")
            .add(keyData)
            .addOnSuccessListener(docRef -> callback.onSuccess(key))
            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    private static String generateRandomKey() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder key = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 12; i++) {
            if (i > 0 && i % 4 == 0) {
                key.append("-");
            }
            key.append(chars.charAt(random.nextInt(chars.length())));
        }
        return key.toString();
    }
    public static void isEnrolled(String userId, String courseId, DataCallback<Boolean> callback) {
        db.collection("enrollments")
            .whereEqualTo("userId", userId)
            .whereEqualTo("courseId", courseId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                callback.onSuccess(!queryDocumentSnapshots.isEmpty());
            })
            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    public static void getUserEnrollments(String userId, DataCallback<List<Course>> callback) {
        db.collection("enrollments")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<String> courseIds = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    String courseId = document.getString("courseId");
                    if (courseId != null) {
                        courseIds.add(courseId);
                    }
                }
                if (courseIds.isEmpty()) {
                    callback.onSuccess(new ArrayList<>());
                    return;
                }
                db.collection("courses")
                    .whereIn(com.google.firebase.firestore.FieldPath.documentId(), courseIds)
                    .get()
                    .addOnSuccessListener(courseDocs -> {
                        List<Course> courses = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : courseDocs) {
                            Course course = documentToCourse(doc);
                            if (course != null) {
                                course.setEnrolled(true);
                                courses.add(course);
                            }
                        }
                        callback.onSuccess(courses);
                    })
                    .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
            })
            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    public static void isEnrolled(String userId, int courseId, DataCallback<Boolean> callback) {
        db.collection("enrollments")
            .whereEqualTo("userId", userId)
            .whereEqualTo("courseId", String.valueOf(courseId))
            .get()
            .addOnSuccessListener(queryDocumentSnapshots ->
                callback.onSuccess(!queryDocumentSnapshots.isEmpty()))
            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    public static void updateModuleProgress(String userId, String courseId, String moduleId, boolean completed, DataCallback<Boolean> callback) {
        String progressPath = "progress/" + userId + "/courses/" + courseId + "/modules/" + moduleId;
        Map<String, Object> progress = new HashMap<>();
        progress.put("completed", completed);
        if (completed) {
            progress.put("completedAt", System.currentTimeMillis());
        }
        db.document(progressPath)
            .set(progress)
            .addOnSuccessListener(aVoid -> callback.onSuccess(true))
            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    public static void getModuleProgress(String userId, String courseId, String moduleId, DataCallback<Boolean> callback) {
        String progressPath = "progress/" + userId + "/courses/" + courseId + "/modules/" + moduleId;
        db.document(progressPath)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Boolean completed = documentSnapshot.getBoolean("completed");
                    callback.onSuccess(completed != null && completed);
                } else {
                    callback.onSuccess(false);
                }
            })
            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    public static void getCourseProgress(String userId, String courseId, DataCallback<Integer> callback) {
        getCourseProgress(userId, courseId, 0, callback);
    }

    public static void getCourseProgress(String userId, String courseId, int totalModulesKnown, DataCallback<Integer> callback) {
        String progressPath = "progress/" + userId + "/courses/" + courseId + "/modules";
        db.collection(progressPath)
            .whereEqualTo("completed", true)
            .get()
            .addOnSuccessListener(completedDocs -> {
                int completedCount = completedDocs.size();
                
                if (totalModulesKnown > 0) {
                     int progress = (completedCount * 100) / totalModulesKnown;
                     callback.onSuccess(progress);
                     return;
                }

                getModulesForCourse(courseId, new DataCallback<List<Module>>() {
                    @Override
                    public void onSuccess(List<Module> modules) {
                        int totalModules = modules.size();
                        int progress = totalModules > 0 ? (completedCount * 100) / totalModules : 0;
                        callback.onSuccess(progress);
                    }
                    @Override
                    public void onFailure(String error) {
                        callback.onFailure(error);
                    }
                });
            })
            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    private static Course documentToCourse(DocumentSnapshot doc) {
        try {
            String idField = doc.getString("id");
            String id = idField != null && !idField.isEmpty() ? idField : doc.getId();
            String title = doc.getString("title");
            List<String> instructorIds = (List<String>) doc.get("instructorIds");
            String difficulty = doc.getString("difficulty");
            Double rating = doc.getDouble("rating");
            String enrollmentKey = doc.getString("enrollmentKey");
            Double price = doc.getDouble("price");
            String category = doc.getString("category");
            String description = doc.getString("description");
            String imageUrl = doc.getString("imageUrl");
            Boolean published = doc.getBoolean("published");
            if (published == null) published = doc.getBoolean("isPublished");
            int durationVal = parseIntField(doc.get("duration"));
            int lessonsCountVal = parseIntField(doc.get("lessonsCount"));
            float ratingVal = rating != null ? rating.floatValue() : 0.0f;
            double priceVal = price != null ? price : 0.0;
            List<String> finalInstructorIds = instructorIds != null ? instructorIds : new ArrayList<>();
            Course course = new Course(
                id,
                title != null ? title : "",
                description != null ? description : "",
                finalInstructorIds,
                category != null ? category : "",
                difficulty != null ? difficulty : "",
                durationVal,
                lessonsCountVal,
                ratingVal,
                imageUrl != null ? imageUrl : "",
                enrollmentKey != null ? enrollmentKey : "",
                priceVal
            );
            course.setPublished(published != null ? published : false); 
            return course;
        } catch (Exception e) {
            android.util.Log.e("FirestoreRepo", "documentToCourse ERROR for doc " + doc.getId() + ": " + e.getMessage(), e);
            return null;
        }
    }
    private static int parseIntField(Object value) {
        if (value == null) return 0;
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
    private static Module documentToModule(DocumentSnapshot doc) {
        try {
            Long idLong = doc.getLong("id");
            String title = doc.getString("title");
            Long durationLong = doc.getLong("duration");
            Boolean unlocked = doc.getBoolean("unlocked");
            Long orderLong = doc.getLong("order");
            String youtubeVideoId = doc.getString("youtubeVideoId");
            int id = idLong != null ? idLong.intValue() : 0;
            int duration = durationLong != null ? durationLong.intValue() : 0;
            boolean isUnlocked = unlocked != null ? unlocked : false;
            int order = orderLong != null ? orderLong.intValue() : 0;
            return new Module(id, title, "", duration, isUnlocked, order, youtubeVideoId);
        } catch (Exception e) {
            return null;
        }
    }
    private static ModuleResource documentToResource(DocumentSnapshot doc) {
        try {
            Long idLong = doc.getLong("id");
            String title = doc.getString("title");
            String type = doc.getString("type");
            String url = doc.getString("url");
            String size = doc.getString("size");
            int id = idLong != null ? idLong.intValue() : 0;
            return new ModuleResource(id, title, type, url, size);
        } catch (Exception e) {
            return null;
        }
    }
    public static void getInstructor(String instructorId, DataCallback<Instructor> callback) {
        db.collection("instructors")
            .document(instructorId)
            .get()
            .addOnSuccessListener(document -> {
                if (document.exists()) {
                    Instructor instructor = documentToInstructor(document);
                    callback.onSuccess(instructor);
                } else {
                    callback.onFailure("Instructor not found");
                }
            })
            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    public static void getInstructorsForCourse(List<String> instructorIds, DataCallback<List<Instructor>> callback) {
        if (instructorIds == null || instructorIds.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }
        List<Instructor> instructors = new ArrayList<>();
        final int[] completed = {0};
        for (String instructorId : instructorIds) {
            getInstructor(instructorId, new DataCallback<Instructor>() {
                @Override
                public void onSuccess(Instructor instructor) {
                    instructors.add(instructor);
                    completed[0]++;
                    if (completed[0] == instructorIds.size()) {
                        callback.onSuccess(instructors);
                    }
                }
                @Override
                public void onFailure(String error) {
                    completed[0]++;
                    if (completed[0] == instructorIds.size()) {
                        callback.onSuccess(instructors);
                    }
                }
            });
        }
    }
    private static Instructor documentToInstructor(DocumentSnapshot doc) {
        try {
            String id = doc.getString("id");
            String name = doc.getString("name");
            String title = doc.getString("title");
            String bio = doc.getString("bio");
            String profileImageUrl = doc.getString("profileImageUrl");
            String email = doc.getString("email");
            List<String> expertise = (List<String>) doc.get("expertise");
            Double rating = doc.getDouble("rating");
            Long coursesCount = doc.getLong("coursesCount");
            Long createdAt = doc.getLong("createdAt");
            float ratingVal = rating != null ? rating.floatValue() : 0.0f;
            int coursesCountVal = coursesCount != null ? coursesCount.intValue() : 0;
            long createdAtVal = createdAt != null ? createdAt : 0;
            return new Instructor(
                id != null ? id : "",
                name != null ? name : "",
                title != null ? title : "",
                bio != null ? bio : "",
                profileImageUrl != null ? profileImageUrl : "",
                email != null ? email : "",
                expertise != null ? expertise : new ArrayList<>(),
                ratingVal,
                coursesCountVal,
                createdAtVal
            );
        } catch (Exception e) {
            return null;
        }
    }
    public static void hasCertificate(String userId, String courseId, DataCallback<Boolean> callback) {
        db.collection("certificates")
            .whereEqualTo("userId", userId)
            .whereEqualTo("courseId", courseId)
            .limit(1)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                callback.onSuccess(!querySnapshot.isEmpty());
            })
            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    public static void createCertificate(String userId, String courseId, String courseName, String studentName, DataCallback<String> callback) {
        hasCertificate(userId, courseId, new DataCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean hasCert) {
                if (hasCert) {
                    callback.onFailure("Certificate already exists for this course");
                    return;
                }
                String certId = "CERT-" + System.currentTimeMillis() + "-" + userId.substring(0, Math.min(4, userId.length())).toUpperCase();
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault());
                String dateCompleted = sdf.format(new java.util.Date());
                Map<String, Object> certificate = new HashMap<>();
                certificate.put("userId", userId);
                certificate.put("courseId", courseId);
                certificate.put("courseName", courseName);
                certificate.put("studentName", studentName);
                certificate.put("dateCompleted", dateCompleted);
                certificate.put("certificateId", certId);
                certificate.put("score", 100.0f);
                certificate.put("createdAt", System.currentTimeMillis());
                db.collection("certificates")
                    .add(certificate)
                    .addOnSuccessListener(docRef -> callback.onSuccess(certId))
                    .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
            }
            @Override
            public void onFailure(String error) {
                callback.onFailure(error);
            }
        });
    }
    public static void getUserCertificates(String userId, DataCallback<List<com.example.skillverse_android.models.Certificate>> callback) {
        db.collection("certificates")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<com.example.skillverse_android.models.Certificate> certificates = new ArrayList<>();
                for (QueryDocumentSnapshot doc : querySnapshot) {
                    com.example.skillverse_android.models.Certificate cert = doc.toObject(com.example.skillverse_android.models.Certificate.class);
                    cert.setDocumentId(doc.getId());
                    certificates.add(cert);
                }
                certificates.sort((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()));
                callback.onSuccess(certificates);
            })
            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    public static void checkCourseCompletion(String userId, String courseId, DataCallback<Boolean> callback) {
        getModulesForCourse(courseId, new DataCallback<List<Module>>() {
            @Override
            public void onSuccess(List<Module> modules) {
                if (modules.isEmpty()) {
                    callback.onSuccess(false);
                    return;
                }
                int totalModules = modules.size();
                final int[] completedCount = {0};
                final int[] checkedCount = {0};
                for (Module module : modules) {
                    String moduleId = module.getDocumentId() != null ? module.getDocumentId() : String.valueOf(module.getId());
                    getModuleProgress(userId, courseId, moduleId, new DataCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean completed) {
                            checkedCount[0]++;
                            if (completed) {
                                completedCount[0]++;
                            }
                            if (checkedCount[0] == totalModules) {
                                callback.onSuccess(completedCount[0] == totalModules);
                            }
                        }
                        @Override
                        public void onFailure(String error) {
                            checkedCount[0]++;
                            if (checkedCount[0] == totalModules) {
                                callback.onSuccess(completedCount[0] == totalModules);
                            }
                        }
                    });
                }
            }
            @Override
            public void onFailure(String error) {
                callback.onFailure(error);
            }
        });
    }

    public static void updateUserName(String userId, String newName, DataCallback<Boolean> callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("name", newName);
        db.collection("users").document(userId)
            .update(updates)
            .addOnSuccessListener(aVoid -> callback.onSuccess(true))
            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public static void updateLastAccessed(String userId, String courseId, String courseTitle, String moduleId, String moduleTitle, DataCallback<Boolean> callback) {
        Map<String, Object> lastAccessed = new HashMap<>();
        lastAccessed.put("courseId", courseId);
        lastAccessed.put("courseTitle", courseTitle);
        lastAccessed.put("moduleId", moduleId);
        lastAccessed.put("moduleTitle", moduleTitle);
        lastAccessed.put("timestamp", System.currentTimeMillis());

        Map<String, Object> updates = new HashMap<>();
        updates.put("lastAccessed", lastAccessed);

        db.collection("users").document(userId)
            .update(updates)
            .addOnSuccessListener(aVoid -> callback.onSuccess(true))
            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public static void getLastAccessed(String userId, DataCallback<Map<String, Object>> callback) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists() && documentSnapshot.contains("lastAccessed")) {
                    callback.onSuccess((Map<String, Object>) documentSnapshot.get("lastAccessed"));
                } else {
                    callback.onSuccess(null);
                }
            })
            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}
