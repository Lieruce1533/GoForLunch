# AI Assistant Development Log

## Session: 2025-10-31

### Summary of Work:

*   **Initial Problem:** Diagnosed and fixed an "Application Not Responding" (ANR) error. The root cause was the Android emulator running out of system resources. 
    *   **Solution:** Recommended wiping the emulator data and performing a cold boot.

*   **Google Sign-In Issue:** Solved a `10: Caller not whitelisted` error during Google Sign-In.
    *   **Solution:** Explained that the app's SHA-1 signing certificate fingerprint was missing from the Firebase project settings and guided on how to retrieve it using the `./gradlew app:signingReport` task and add it to Firebase.

*   **New Feature - Sign Out:** Implemented a sign-out feature.
    *   **Action:** Added a sign-out button to `activity_main.xml` and implemented the sign-out logic in `MainActivity` using `AuthUI.getInstance().signOut()`.

*   **Architecture Rules Established:**
    1.  **Architecture:** We will follow MVVM.
    2.  **View Access:** We will use ViewBinding/DataBinding over `findViewById`, providing detailed explanations for the choice.
    3.  **Clarity:** I will provide detailed explanations and alternatives for tasks to ensure a clear understanding of the solutions.

*   **Refactoring to ViewBinding:** Updated the existing code to adhere to our new rules.
    *   **Action:** Enabled ViewBinding in the `app/build.gradle` file.
    *   **Action:** Refactored `MainActivity.java` to use the generated `ActivityMainBinding` class, removing `findViewById` calls.

*   **Project Synchronization:** Addressed how to keep the project and our conversation history synchronized across multiple machines.
    *   **Solution:** Confirmed the use of a central Git repository. The `.gitignore` file was checked to ensure it correctly excludes machine-specific files like `local.properties`. This log file itself will be committed to Git to sync our conversation history.

## Session: 2025-11-01

### Summary of Work:

*   **Build Warning Resolution:** Investigated and resolved several build warnings.
    *   **Action:** Updated outdated dependencies (`firebase-bom`, `lifecycle-viewmodel-ktx`, `lifecycle-livedata-ktx`) in `gradle/libs.versions.toml`.
    *   **Action:** Resolved a Java version deprecation warning by updating the `compileOptions` in `app/build.gradle` from Java 8 to Java 17.

*   **Repository Pattern Implementation:** Refactored the authentication logic to introduce a repository, improving the MVVM architecture.
    *   **Action:** Created `AuthRepository.java` to act as the single source of truth for authentication data, abstracting `FirebaseAuth` from the rest of the app.
    *   **Action:** Created `ViewModelFactory.java` to handle the creation of ViewModels with dependencies.
    *   **Action:** Updated `MainViewModel.java` to use the `AuthRepository` for all authentication-related tasks.
    *   **Action:** Refactored `MainActivity.java` to use the `ViewModelFactory` and communicate only with the `MainViewModel`, fully decoupling it from the authentication implementation.

## Session: 2025-11-02

### Summary of Work:

*   **Bug Fix - ViewModelFactory:** Corrected a runtime error in `ViewModelFactory.java`.
    *   **Action:** Fixed the `create` method signature, which was missing a parameter name, and corrected the class type comparison.

*   **Project Cleanup:** Reorganized the project structure for better maintainability.
    *   **Action:** Created a new `repository` package.
    *   **Action:** Moved `AuthRepository.java` to the new package.
    *   **Action:** Updated all import statements in `MainViewModel.java` and `ViewModelFactory.java` to reflect the new location.
    *   **Action:** Instructed on the manual deletion of the old, misplaced `AuthRepository.java` file and an incorrectly created empty file.

## Session: 2025-11-03

### Summary of Work:

*   **Firestore Integration - User Creation:** Implemented the logic to save user data to Cloud Firestore upon registration.
    *   **Documentation:** Created a `FIRESTORE_EXPLANATION.md` file detailing Firestore concepts, data structure, and real-time capabilities.
    *   **Action (Model):** Created a `User.java` data model to represent user data in the app.
    *   **Action (Repository):** Created `UserRepository.java` to handle all database operations for the `users` collection, including a `createUser` method.
    *   **Action (ViewModel):** Updated `ViewModelFactory` and `MainViewModel` to include and use the `UserRepository`.
    *   **Action (Activity):** Modified `MainActivity`'s `onSignInResult` method to check for new users and trigger the `createUser` flow, saving them to Firestore.

## Session: 2025-11-04

### Summary of Work:

*   **Git Workflow Cleanup:** Performed a major Git refactoring to establish a clean and professional `develop`-based workflow.
    *   **Documentation:** Created `GIT_REBASE_PROCEDURE.md` to document the complex rebase and cleanup process.
    *   **Action:** Stashed uncommitted work, created a `develop` branch from `main`, and rebased the `Authentication` branch onto `develop`.
    *   **Action:** Merged the rebased `Authentication` branch into `develop` and deleted it.
    *   **Action:** Created a new `feature/firestore-user-model` branch from `develop` and applied the stashed work to it, successfully isolating the feature.
    *   **Action:** Pushed the new branches and deleted the obsolete branch from the remote repository.

*   **Architectural Planning:** Defined a clear, modern architecture for the application's UI.
    *   **Decision:** We will use a **Single-Activity Architecture** with `MainActivity` hosting multiple Fragments.
    *   **Decision:** The UI will be composed of three main Fragments: `MapFragment`, `RestaurantsFragment`, and `WorkmatesFragment`, managed by a `BottomNavigationView` and the Jetpack Navigation Component.
    *   **Decision:** We will distinguish between **Data Models** (like `User`, which mirrors Firestore) and **UI Models** (like `Workmate`, which is tailored for display). The ViewModel will be responsible for transforming data models into UI models.

## Session: 2025-11-09

### Summary of Work:

*   **Single-Activity Architecture Setup:** Built the foundational structure for the app's UI using Fragments and the Jetpack Navigation Component.
    *   **Action (Dependencies):** Added `navigation-fragment` and `navigation-ui` dependencies to the project.
    *   **Action (Fragments):** Created placeholder Fragments and layouts for `MapFragment`, `RestaurantsFragment`, and `WorkmatesFragment`.
    *   **Action (Layout):** Updated `activity_main.xml` to include a `FragmentContainerView` (as a `NavHost`) and a `BottomNavigationView`.
    *   **Action (Navigation):** Created a `navigation/nav_graph.xml` to define the app's navigation destinations and a `menu/main_menu.xml` for the bottom navigation icons.
    *   **Action (Activity):** Refactored `MainActivity.java` to link the `NavController` with the `BottomNavigationView` and the toolbar, correctly setting up the navigation host.

*   **Robust User Creation Logic:** Refactored the user creation process to be more resilient and reliable.
    *   **Action (`UserRepository`):** Modified the `createUser` method to use `set(..., SetOptions.merge())`. This "upsert" strategy ensures that user data is safely created on the first login or updated on subsequent logins without overwriting existing fields (like a chosen restaurant).
    *   **Action (`MainActivity`):** Removed the conditional `isNewUser()` check to ensure the `createUser` (upsert) logic is called on every successful login, guaranteeing the database is always in sync.
