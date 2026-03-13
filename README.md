# 🚀 Skillverse Android

Welcome to the **Skillverse Android** app! This is a modern, feature-rich e-learning application built natively for Android using Java. It provides a robust platform for both users and administrators to explore, manage, and engage with online courses.

## ✨ Features

### 👤 User Panel
* **Authentication**: Secure Login and Registration (powered by Firebase Auth).
* **Browse & Enroll**: Discover available courses, view details, and enroll.
* **My Courses**: Keep track of enrolled courses and daily progress.
* **Module Viewer**: Interactive UI to seamlessly read modules and resources.
* **Certificates**: Automatically earn and view certificates directly upon course completion.
* **Profile Management**: Update user profile information. 

### 🛡️ Admin Panel
* **Dashboard Overview**: Track app metrics and monitor activities.
* **Course Management**: Add, edit, remove, and manage courses, modules, and resources.
* **User & Instructor Management**: Control who has access to teach or learn.
* **Enrollment Keys**: Generate and manage course-specific enrollment keys for student to enroll courses.

## 🛠️ Tech Stack
* **Language**: Java
* **UI Components**: XML Views, RecyclerView, CardView, ViewBinding
* **Image Loading**: Glide
* **Backend Module**: 
  * Firebase Authentication
  * Firebase Firestore 
* **Architecture**: Core Android MVC structure 

## 📸 Screenshots

|<img src="screenshots/1.jpg" width="200"/>|<img src="screenshots/2.jpg" width="200"/>|<img src="screenshots/3.jpg" width="200"/>|<img src="screenshots/4.jpg" width="200"/>|
|:---:|:---:|:---:|:---:|
|<img src="screenshots/5.jpg" width="200"/>|<img src="screenshots/6.jpg" width="200"/>|<img src="screenshots/7.jpg" width="200"/>|<img src="screenshots/8.jpg" width="200"/>|
|<img src="screenshots/9.jpg" width="200"/>|<img src="screenshots/10.jpg" width="200"/>|<img src="screenshots/11.jpg" width="200"/>|<img src="screenshots/12.jpg" width="200"/>|
|<img src="screenshots/13.jpg" width="200"/>|<img src="screenshots/14.jpg" width="200"/>|<img src="screenshots/15.jpg" width="200"/>|<img src="screenshots/16.jpg" width="200"/>|
|<img src="screenshots/17.jpg" width="200"/>|<img src="screenshots/18.jpg" width="200"/>|<img src="screenshots/19.jpg" width="200"/>|<img src="screenshots/20.jpg" width="200"/>|

## 🚀 Getting Started

1. **Clone the repository**:
   ```bash
   git clone https://github.com/SABBIR-H0SSAIN/Skilllverse-Android.git
   ```
2. **Open in Android Studio**: Launch Android Studio and select `Open an existing Android Studio project`.
3. **Connect Firebase**:
   * Create a Firebase project.
   * Add an Android app and configure the package name (`com.example.skillverse_android`).
   * Download the `google-services.json` file and place it in the `app/` directory.
   * Enable Authentication (Email/Password), Firestore, and Storage on your Firebase console.
4. **Build and Run**: Click the Run button (`Shift + F10`) to compile the project and install it on an emulator or physical device.

