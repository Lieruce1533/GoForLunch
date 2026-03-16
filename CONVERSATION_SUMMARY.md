# Go4Lunch - Conversation Summary & Key Decisions

This document summarizes the key technical discussions, troubleshooting steps, and architectural decisions made for the Go4Lunch project.

## Part 1: Initial Setup & Troubleshooting

### 1.1. Firebase Authentication & App Launch Issues

- **Problem**: The app would not launch after adding Firebase Authentication.
- **Troubleshooting Steps & Solutions**:
    1.  **AndroidManifest.xml**: The file was missing the `<uses-permission android:name="android.permission.INTERNET" />` permission and the `<activity>` declaration for the `com.firebase.ui.auth.AuthUIActivity`. Both were added.
    2.  **Theme Incompatibility**: The app theme inherited from `NoActionBar`, which caused the `AuthUIActivity` to crash. The parent theme in `themes.xml` and `themes-night.xml` was changed to `Theme.Material3.DayNight` to provide the required `ActionBar`.
    3.  **Emulator Installation Failure (`INSTALL_FAILED_VERIFICATION_FAILURE`)**: This was traced to a corrupted or untrusted debug signing key (`debug.keystore`), likely caused by an Android Studio update. The solution was to delete the `~/.android/debug.keystore` file and let Android Studio generate a new, clean one.

### 1.2. The "SHA-1 Hell" - Creating a Permanent Fix

- **Problem**: Google Sign-In failed on a physical device because the app's SHA-1 fingerprint did not match the one registered in the Firebase console. The debug key changed after IDE updates, making this a recurring issue.
- **The Permanent Solution**:
    1.  **Create a Dedicated Keystore**: We generated a new `debug.keystore` file *inside the project's `app` folder*.
    2.  **Configure `build.gradle`**: The app-level `build.gradle` was modified to include a `signingConfigs.debug` block that explicitly points to this new, project-local `debug.keystore` file. This ensures every developer uses the same debug key, making the SHA-1 stable and consistent across all machines and updates.
    3.  **Final Update**: The new, stable SHA-1 was registered in the Firebase console one last time.

### 1.3. Gradle Build Failure (`Unsupported class file major version 69`)

- **Problem**: The `./gradlew signingReport` command failed with a Java version error.
- **Explanation**: Gradle was being run with an incompatible, newer version of the Java JDK.
- **Solution**: We forced Gradle to use a stable, compatible JDK by adding the `org.gradle.java.home` property to the `gradle.properties` file, pointing it to the JDK embedded within Android Studio (e.g., jbr-17).

### 1.4. Git Issues (`git rm`)

- **Problem**: The `gradle.properties` file was accidentally removed from the project via `git rm`.
- **Solution**: The file was restored by first unstaging the deletion (`git restore --staged gradle.properties`) and then restoring the file itself (`git restore gradle.properties`).
- **Correct Way to Ignore Local Changes**: To ignore local-only changes to a tracked file (like adding the `org.gradle.java.home` property), the correct command is `git update-index --assume-unchanged <file>`. The `.gitignore` file is for files that should never be tracked at all.

## Part 2: Architecture & App Planning

### 2.1. Core Architecture (MVVM)

- **Decision**: The app will follow the **Model-View-ViewModel (MVVM)** pattern.
- **Implementation Steps**:
    1.  **Dependencies**: Added `lifecycle-viewmodel-ktx` and `lifecycle-livedata-ktx` to the project.
    2.  **ViewModel Creation**: A `MainViewModel` was created to hold the app's UI logic.
    3.  **LiveData**: The ViewModel now exposes the user's authentication state via `LiveData<FirebaseUser>`.
    4.  **Activity as Observer**: The `MainActivity` was refactored to be a passive View. It simply observes the `LiveData` from the ViewModel and updates the UI accordingly (either shows a welcome message or launches the sign-in flow). The complex logic was moved out of the Activity.

### 2.2. Full App Blueprint & Feature Plan

- **Core Tools**: Hilt (Dependency Injection) and Retrofit (Networking).
- **High-Level Features**:
    1.  **Map View**: A map displaying nearby restaurants.
    2.  **List View**: A `RecyclerView` listing the same restaurants.
    3.  **Coworkers View**: A `RecyclerView` listing coworkers and their lunch choices.
    4.  **Details View**: A detail screen for a selected restaurant.
    5.  **Search**: A search bar to find specific restaurants.

### 2.3. Detailed Firestore Schema Design

- **`users` Collection**: Stores user-specific profile data.
    - *Document ID*: `user.uid`
    - *Fields*: `uid`, `username`, `avatarUrl`, `chosenRestaurantId`, `chosenRestaurantName`.
    - *Subcollection*: `likedRestaurants` (stores a user's favorite restaurants).

- **`dailyChoices` Collection**: Tracks real-time lunch choices for the current day.
    - *Document ID*: `restaurant.place_id`
    - *Subcollection*: `attendees` (lists all users going to that restaurant).
    - *Denormalization*: The `attendees` documents will duplicate user `username` and `avatarUrl` for highly efficient queries, avoiding the need for multiple lookups.

### 2.4. Location & Places Integration (Feature Branch: `feature/location-services`)

- **Location Services**: Implemented `LocationRepository` using `FusedLocationProviderClient`.
    - Handles runtime permissions (`ACCESS_FINE_LOCATION`).
    - Exposes user location via `LiveData`.
- **Restaurant Data**: Implemented `RestaurantRepository` using Google Places API.
    - **API Update**: Updated to use the new Places API (`SearchNearbyRequest`, `FetchPlaceRequest`) to handle deprecations.
    - **Strategy**: 2-step fetch (Search IDs -> Fetch Details in parallel) to get photos and ratings.
    - **Data Flow**: `LocationRepository` -> User Location -> `RestaurantRepository` -> Nearby Restaurants.

### 2.5. Reactive Data Flow & Automated Search (Session: 2026-01-26)

- **Problem**: Restaurant search had to be triggered manually, which was not a good user experience.
- **The Reactive Solution**:
    1.  **Refactored `RestaurantRepository`**: The `fetchNearbyRestaurants` method was changed to accept a `Location` object as a parameter, making it more explicit and easier to test.
    2.  **`MapsViewModel` Enhancement**: We implemented a `MediatorLiveData` for the restaurant list. This mediator now "listens" to the user's location LiveData.
    3.  **The Trigger**: As soon as the `LocationRepository` provides a valid location, the `MapsViewModel` automatically calls the repository's search function.
    4.  **The Benefit**: The UI (Map) stays completely passive. It just observes the restaurant list, and the data flows automatically from GPS -> Repository -> ViewModel -> UI without manual intervention.

## Part 3: Collaboration Ground Rules

- **Agent Collaboration Rules (`AGENT_RULES.md`)**: To ensure a smooth working relationship, we established a set of rules for the AI Agent:
    - **Rejection Protocol**: STOP and ask "why" if a change is rejected.
    - **Educational Mandate**: Always explain the "why" and "how" of technical choices (Mentorship).
    - **Accuracy**: Never guess dates or project states; always verify with the developer.
