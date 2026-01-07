package com.example.skillverse_android.utils;
import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class SampleDataSeeder {
    private static final String TAG = "SampleDataSeeder";
    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    public interface SeederCallback {
        void onComplete();
        void onError(String error);
    }
    public static void seedSampleCourses(SeederCallback callback) {
        Log.d(TAG, "Starting to seed 5 sample courses...");
        List<CourseData> courses = createSampleCourses();
        seedCoursesRecursive(courses, 0, callback);
    }
    private static List<CourseData> createSampleCourses() {
        List<CourseData> courses = new ArrayList<>();
        courses.add(new CourseData(
            "Android Development Masterclass",
            "Master Android app development from scratch. Learn Kotlin, Jetpack Compose, MVVM architecture, and build real-world applications.",
            "Mobile Development",
            "Intermediate",
            45,
            5,
            79.99,
            "https://images.unsplash.com/photo-1607252650355-f7fd0460ccdb",
            createAndroidModules()
        ));
        courses.add(new CourseData(
            "Complete Web Development Bootcamp",
            "Become a full-stack web developer. Learn HTML, CSS, JavaScript, React, Node.js, and MongoDB.",
            "Web Development",
            "Beginner",
            60,
            5,
            89.99,
            "https://images.unsplash.com/photo-1498050108023-c5249f4df085",
            createWebDevModules()
        ));
        courses.add(new CourseData(
            "Data Science Fundamentals",
            "Learn Python, data analysis, machine learning, and data visualization. Perfect for aspiring data scientists.",
            "Data Science",
            "Intermediate",
            50,
            4,
            99.99,
            "https://images.unsplash.com/photo-1551288049-bebda4e38f71",
            createDataScienceModules()
        ));
        courses.add(new CourseData(
            "UI/UX Design Essentials",
            "Master user interface and user experience design. Learn Figma, design principles, and create stunning designs.",
            "Design",
            "Beginner",
            35,
            4,
            59.99,
            "https://images.unsplash.com/photo-1561070791-2526d30994b5",
            createUIUXModules()
        ));
        courses.add(new CourseData(
            "Cloud Computing with AWS",
            "Learn Amazon Web Services from basics to advanced. Master EC2, S3, Lambda, and cloud architecture.",
            "Cloud Computing",
            "Advanced",
            55,
            5,
            119.99,
            "https://images.unsplash.com/photo-1451187580459-43490279c0fa",
            createAWSModules()
        ));
        return courses;
    }
    private static List<ModuleData> createAndroidModules() {
        List<ModuleData> modules = new ArrayList<>();
        modules.add(new ModuleData("Introduction to Android Development", "Get started with Android development", 60, 1, true, "fis26HvvDII", createAndroidResources1()));
        modules.add(new ModuleData("Kotlin Programming Basics", "Learn Kotlin fundamentals", 90, 2, true, "F9UC9DY-vIU", createAndroidResources2()));
        modules.add(new ModuleData("Building UIs with Jetpack Compose", "Create modern UIs", 120, 3, false, "cDabx3SjuOY", createAndroidResources3()));
        modules.add(new ModuleData("MVVM Architecture Pattern", "Implement clean architecture", 100, 4, false, "I5c7fBgvkNY", createAndroidResources4()));
        modules.add(new ModuleData("Firebase Integration", "Integrate Firebase services", 110, 5, false, "iosNuIdQoy8", createAndroidResources5()));
        return modules;
    }
    private static List<ResourceData> createAndroidResources1() {
        List<ResourceData> resources = new ArrayList<>();
        resources.add(new ResourceData("Android Studio Setup Guide", "PDF", "https://developer.android.com/studio/intro", "2.5 MB"));
        resources.add(new ResourceData("Your First Android App", "Video", "https://www.youtube.com/watch?v=fis26HvvDII", "15 min"));
        resources.add(new ResourceData("Official Android Documentation", "Link", "https://developer.android.com", "N/A"));
        return resources;
    }
    private static List<ResourceData> createAndroidResources2() {
        List<ResourceData> resources = new ArrayList<>();
        resources.add(new ResourceData("Kotlin Cheat Sheet", "PDF", "https://kotlinlang.org/docs/kotlin-reference.pdf", "1.8 MB"));
        resources.add(new ResourceData("Kotlin Best Practices", "Document", "https://kotlinlang.org/docs/coding-conventions.html", "N/A"));
        return resources;
    }
    private static List<ResourceData> createAndroidResources3() {
        List<ResourceData> resources = new ArrayList<>();
        resources.add(new ResourceData("Compose UI Components Guide", "PDF", "https://developer.android.com/jetpack/compose", "3.2 MB"));
        resources.add(new ResourceData("Jetpack Compose Samples", "Link", "https://github.com/android/compose-samples", "N/A"));
        return resources;
    }
    private static List<ResourceData> createAndroidResources4() {
        List<ResourceData> resources = new ArrayList<>();
        resources.add(new ResourceData("MVVM Architecture Guide", "PDF", "https://developer.android.com/topic/architecture", "2.1 MB"));
        resources.add(new ResourceData("ViewModel Best Practices", "Document", "https://developer.android.com/topic/libraries/architecture/viewmodel", "N/A"));
        return resources;
    }
    private static List<ResourceData> createAndroidResources5() {
        List<ResourceData> resources = new ArrayList<>();
        resources.add(new ResourceData("Firebase Setup Guide", "PDF", "https://firebase.google.com/docs/android/setup", "1.9 MB"));
        resources.add(new ResourceData("Firebase Documentation", "Link", "https://firebase.google.com/docs", "N/A"));
        return resources;
    }
    private static List<ModuleData> createWebDevModules() {
        List<ModuleData> modules = new ArrayList<>();
        modules.add(new ModuleData("HTML & CSS Fundamentals", "Learn the building blocks", 80, 1, true, "qz0aGYrrlhU", createWebResources1()));
        modules.add(new ModuleData("JavaScript Programming", "Master JavaScript fundamentals", 120, 2, true, "PkZNo7MFNFg", createWebResources2()));
        modules.add(new ModuleData("React.js Development", "Build dynamic UIs with React", 140, 3, false, "Ke90Tje7VS0", createWebResources3()));
        modules.add(new ModuleData("Backend with Node.js", "Create RESTful APIs", 130, 4, false, "Oe421EPjeBE", createWebResources4()));
        modules.add(new ModuleData("MongoDB & Database Design", "Work with NoSQL databases", 100, 5, false, "-56x56UppqQ", createWebResources5()));
        return modules;
    }
    private static List<ResourceData> createWebResources1() {
        List<ResourceData> resources = new ArrayList<>();
        resources.add(new ResourceData("HTML5 Complete Reference", "PDF", "https://www.w3.org/TR/html5/", "3.5 MB"));
        resources.add(new ResourceData("CSS3 Styling Guide", "PDF", "https://www.w3.org/Style/CSS/", "2.8 MB"));
        return resources;
    }
    private static List<ResourceData> createWebResources2() {
        List<ResourceData> resources = new ArrayList<>();
        resources.add(new ResourceData("JavaScript ES6 Guide", "PDF", "https://developer.mozilla.org/en-US/docs/Web/JavaScript", "2.3 MB"));
        resources.add(new ResourceData("Async/Await Explained", "Document", "https://javascript.info/async-await", "N/A"));
        return resources;
    }
    private static List<ResourceData> createWebResources3() {
        List<ResourceData> resources = new ArrayList<>();
        resources.add(new ResourceData("React Hooks Guide", "PDF", "https://react.dev/reference/react", "2.9 MB"));
        resources.add(new ResourceData("React Official Documentation", "Link", "https://react.dev", "N/A"));
        return resources;
    }
    private static List<ResourceData> createWebResources4() {
        List<ResourceData> resources = new ArrayList<>();
        resources.add(new ResourceData("Express.js API Guide", "PDF", "https://expressjs.com/en/guide/routing.html", "2.1 MB"));
        resources.add(new ResourceData("RESTful API Best Practices", "Document", "https://restfulapi.net", "N/A"));
        return resources;
    }
    private static List<ResourceData> createWebResources5() {
        List<ResourceData> resources = new ArrayList<>();
        resources.add(new ResourceData("MongoDB CRUD Operations", "PDF", "https://www.mongodb.com/docs/manual/crud/", "1.7 MB"));
        resources.add(new ResourceData("MongoDB University", "Link", "https://university.mongodb.com", "N/A"));
        return resources;
    }
    private static List<ModuleData> createDataScienceModules() {
        List<ModuleData> modules = new ArrayList<>();
        modules.add(new ModuleData("Python Programming Basics", "Learn Python fundamentals", 90, 1, true, "rfscVS0vtbw", createDSResources1()));
        modules.add(new ModuleData("Data Analysis with Pandas", "Master data manipulation", 110, 2, true, "vmEHCJofslg", createDSResources2()));
        modules.add(new ModuleData("Data Visualization", "Create compelling visualizations", 85, 3, false, "0P7QnIQDBJY", createDSResources3()));
        modules.add(new ModuleData("Introduction to Machine Learning", "Build your first ML models", 120, 4, false, "ukzFI9rgwfU", createDSResources4()));
        return modules;
    }
    private static List<ResourceData> createDSResources1() {
        List<ResourceData> resources = new ArrayList<>();
        resources.add(new ResourceData("Python Data Science Handbook", "PDF", "https://jakevdp.github.io/PythonDataScienceHandbook/", "4.2 MB"));
        resources.add(new ResourceData("Python Official Docs", "Link", "https://docs.python.org/3/", "N/A"));
        return resources;
    }
    private static List<ResourceData> createDSResources2() {
        List<ResourceData> resources = new ArrayList<>();
        resources.add(new ResourceData("Pandas Cheat Sheet", "PDF", "https://pandas.pydata.org/Pandas_Cheat_Sheet.pdf", "1.5 MB"));
        resources.add(new ResourceData("Data Cleaning Techniques", "Document", "https://pandas.pydata.org/docs/", "N/A"));
        return resources;
    }
    private static List<ResourceData> createDSResources3() {
        List<ResourceData> resources = new ArrayList<>();
        resources.add(new ResourceData("Visualization Best Practices", "PDF", "https://matplotlib.org/stable/tutorials/index.html", "2.7 MB"));
        resources.add(new ResourceData("Seaborn Gallery", "Link", "https://seaborn.pydata.org/examples/index.html", "N/A"));
        return resources;
    }
    private static List<ResourceData> createDSResources4() {
        List<ResourceData> resources = new ArrayList<>();
        resources.add(new ResourceData("ML Algorithms Guide", "PDF", "https://scikit-learn.org/stable/user_guide.html", "3.8 MB"));
        resources.add(new ResourceData("Model Evaluation Metrics", "Document", "https://scikit-learn.org/stable/modules/model_evaluation.html", "N/A"));
        return resources;
    }
    private static List<ModuleData> createUIUXModules() {
        List<ModuleData> modules = new ArrayList<>();
        modules.add(new ModuleData("Fundamental Design Principles", "Learn color theory and typography", 70, 1, true, "YqQx75OPRa0", createUIUXResources1()));
        modules.add(new ModuleData("Figma Design Tool", "Master Figma for UI/UX", 95, 2, true, "FTFaQWZBqQ8", createUIUXResources2()));
        modules.add(new ModuleData("User Research Methods", "Conduct user research", 80, 3, false, "Ovj4hFxko7c", createUIUXResources3()));
        modules.add(new ModuleData("Interactive Prototyping", "Create interactive prototypes", 90, 4, false, "KWZGCCBuols", createUIUXResources4()));
        return modules;
    }
    private static List<ResourceData> createUIUXResources1() {
        List<ResourceData> resources = new ArrayList<>();
        resources.add(new ResourceData("Design Principles Guide", "PDF", "https://www.interaction-design.org/literature", "3.1 MB"));
        resources.add(new ResourceData("Typography Resources", "Link", "https://fonts.google.com", "N/A"));
        return resources;
    }
    private static List<ResourceData> createUIUXResources2() {
        List<ResourceData> resources = new ArrayList<>();
        resources.add(new ResourceData("Figma Shortcuts Guide", "PDF", "https://www.figma.com/resources/", "1.2 MB"));
        resources.add(new ResourceData("Figma Community", "Link", "https://www.figma.com/community", "N/A"));
        return resources;
    }
    private static List<ResourceData> createUIUXResources3() {
        List<ResourceData> resources = new ArrayList<>();
        resources.add(new ResourceData("UX Research Methods", "PDF", "https://www.nngroup.com/articles/", "2.4 MB"));
        resources.add(new ResourceData("Creating User Personas", "Document", "https://www.uxdesigninstitute.com", "N/A"));
        return resources;
    }
    private static List<ResourceData> createUIUXResources4() {
        List<ResourceData> resources = new ArrayList<>();
        resources.add(new ResourceData("Prototyping Best Practices", "PDF", "https://www.figma.com/best-practices/", "1.9 MB"));
        resources.add(new ResourceData("Design System Examples", "Link", "https://designsystemsrepo.com", "N/A"));
        return resources;
    }
    private static List<ModuleData> createAWSModules() {
        List<ModuleData> modules = new ArrayList<>();
        modules.add(new ModuleData("Introduction to AWS", "Understand cloud computing", 75, 1, true, "ulprqHHWlng", createAWSResources1()));
        modules.add(new ModuleData("EC2 and Compute Services", "Deploy virtual servers", 100, 2, true, "iHX-jtKIVNA", createAWSResources2()));
        modules.add(new ModuleData("S3 and Storage Solutions", "Master AWS storage", 85, 3, false, "77lMCiiMilo", createAWSResources3()));
        modules.add(new ModuleData("Serverless with Lambda", "Build serverless applications", 110, 4, false, "eOBq__h4OJ4", createAWSResources4()));
        modules.add(new ModuleData("AWS Cloud Architecture", "Design scalable architectures", 120, 5, false, "dH0yz-Osy54", createAWSResources5()));
        return modules;
    }
    private static List<ResourceData> createAWSResources1() {
        List<ResourceData> resources = new ArrayList<>();
        resources.add(new ResourceData("AWS Services Overview", "PDF", "https://aws.amazon.com/getting-started/", "3.5 MB"));
        resources.add(new ResourceData("AWS Free Tier Guide", "Link", "https://aws.amazon.com/free/", "N/A"));
        return resources;
    }
    private static List<ResourceData> createAWSResources2() {
        List<ResourceData> resources = new ArrayList<>();
        resources.add(new ResourceData("EC2 Instance Types Guide", "PDF", "https://aws.amazon.com/ec2/instance-types/", "2.8 MB"));
        resources.add(new ResourceData("Auto Scaling Best Practices", "Document", "https://docs.aws.amazon.com/autoscaling/", "N/A"));
        return resources;
    }
    private static List<ResourceData> createAWSResources3() {
        List<ResourceData> resources = new ArrayList<>();
        resources.add(new ResourceData("S3 Security Guide", "PDF", "https://docs.aws.amazon.com/AmazonS3/latest/userguide/", "2.1 MB"));
        resources.add(new ResourceData("AWS Storage Comparison", "Link", "https://aws.amazon.com/products/storage/", "N/A"));
        return resources;
    }
    private static List<ResourceData> createAWSResources4() {
        List<ResourceData> resources = new ArrayList<>();
        resources.add(new ResourceData("Lambda Functions Guide", "PDF", "https://docs.aws.amazon.com/lambda/", "2.5 MB"));
        resources.add(new ResourceData("Serverless Architecture Patterns", "Document", "https://aws.amazon.com/serverless/", "N/A"));
        return resources;
    }
    private static List<ResourceData> createAWSResources5() {
        List<ResourceData> resources = new ArrayList<>();
        resources.add(new ResourceData("Well-Architected Framework", "PDF", "https://aws.amazon.com/architecture/well-architected/", "4.1 MB"));
        resources.add(new ResourceData("AWS Architecture Center", "Link", "https://aws.amazon.com/architecture/", "N/A"));
        return resources;
    }
    private static void seedCoursesRecursive(List<CourseData> courses, int index, SeederCallback callback) {
        if (index >= courses.size()) {
            Log.d(TAG, "All courses seeded successfully!");
            callback.onComplete();
            return;
        }
        CourseData course = courses.get(index);
        Map<String, Object> courseData = new HashMap<>();
        courseData.put("title", course.title);
        courseData.put("description", course.description);
        courseData.put("category", course.category);
        courseData.put("difficulty", course.difficulty);
        courseData.put("duration", course.duration);
        courseData.put("lessonsCount", course.lessonsCount);
        courseData.put("price", course.price);
        courseData.put("imageUrl", course.imageUrl);
        courseData.put("published", true);
        courseData.put("instructorIds", new ArrayList<>());
        courseData.put("rating", 4.5 + (Math.random() * 0.5));
        courseData.put("enrollmentCount", 0);
        courseData.put("createdAt", System.currentTimeMillis());
        courseData.put("updatedAt", System.currentTimeMillis());
        db.collection("courses")
            .add(courseData)
            .addOnSuccessListener(documentReference -> {
                String courseId = documentReference.getId();
                Log.d(TAG, "Seeded course: " + course.title + " (ID: " + courseId + ")");
                seedModules(courseId, course.modules, 0, new SeederCallback() {
                    @Override
                    public void onComplete() {
                        seedCoursesRecursive(courses, index + 1, callback);
                    }
                    @Override
                    public void onError(String error) {
                        callback.onError(error);
                    }
                });
            })
            .addOnFailureListener(e -> callback.onError("Failed to seed course: " + e.getMessage()));
    }
    private static void seedModules(String courseId, List<ModuleData> modules, int index, SeederCallback callback) {
        if (index >= modules.size()) {
            callback.onComplete();
            return;
        }
        ModuleData module = modules.get(index);
        Map<String, Object> moduleData = new HashMap<>();
        moduleData.put("title", module.title);
        moduleData.put("description", module.description);
        moduleData.put("duration", module.duration);
        moduleData.put("order", module.order);
        moduleData.put("unlocked", module.unlocked);
        moduleData.put("youtubeVideoId", module.youtubeVideoId);
        db.collection("courses").document(courseId)
            .collection("modules")
            .add(moduleData)
            .addOnSuccessListener(documentReference -> {
                String moduleId = documentReference.getId();
                Log.d(TAG, "  Seeded module: " + module.title);
                seedResources(courseId, moduleId, module.resources, 0, new SeederCallback() {
                    @Override
                    public void onComplete() {
                        seedModules(courseId, modules, index + 1, callback);
                    }
                    @Override
                    public void onError(String error) {
                        callback.onError(error);
                    }
                });
            })
            .addOnFailureListener(e -> callback.onError("Failed to seed module: " + e.getMessage()));
    }
    private static void seedResources(String courseId, String moduleId, List<ResourceData> resources, int index, SeederCallback callback) {
        if (index >= resources.size()) {
            callback.onComplete();
            return;
        }
        ResourceData resource = resources.get(index);
        Map<String, Object> resourceData = new HashMap<>();
        resourceData.put("title", resource.title);
        resourceData.put("type", resource.type);
        resourceData.put("url", resource.url);
        resourceData.put("size", resource.size);
        db.collection("courses").document(courseId)
            .collection("modules").document(moduleId)
            .collection("resources")
            .add(resourceData)
            .addOnSuccessListener(documentReference -> {
                Log.d(TAG, "    Seeded resource: " + resource.title);
                seedResources(courseId, moduleId, resources, index + 1, callback);
            })
            .addOnFailureListener(e -> callback.onError("Failed to seed resource: " + e.getMessage()));
    }
    static class CourseData {
        String title, description, category, difficulty, imageUrl;
        int duration, lessonsCount;
        double price;
        List<ModuleData> modules;
        CourseData(String title, String description, String category, String difficulty,
                   int duration, int lessonsCount, double price, String imageUrl, List<ModuleData> modules) {
            this.title = title;
            this.description = description;
            this.category = category;
            this.difficulty = difficulty;
            this.duration = duration;
            this.lessonsCount = lessonsCount;
            this.price = price;
            this.imageUrl = imageUrl;
            this.modules = modules;
        }
    }
    static class ModuleData {
        String title, description, youtubeVideoId;
        int duration, order;
        boolean unlocked;
        List<ResourceData> resources;
        ModuleData(String title, String description, int duration, int order, boolean unlocked,
                   String youtubeVideoId, List<ResourceData> resources) {
            this.title = title;
            this.description = description;
            this.duration = duration;
            this.order = order;
            this.unlocked = unlocked;
            this.youtubeVideoId = youtubeVideoId;
            this.resources = resources;
        }
    }
    static class ResourceData {
        String title, type, url, size;
        ResourceData(String title, String type, String url, String size) {
            this.title = title;
            this.type = type;
            this.url = url;
            this.size = size;
        }
    }
}
