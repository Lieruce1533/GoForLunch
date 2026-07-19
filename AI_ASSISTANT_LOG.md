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

## Session: 2025-11-10

### Summary of Work:

*   **Git Workflow Management:** Enforced proper Git process by creating a new `feature/location-services` branch from `develop` before implementing new features. Resolved a `rebase conflict` in the Gradle files.

*   **Location Services Foundation (Step 1):** Implemented the core components required to get the user's location.
    *   **Action (Permission):** Added the `ACCESS_FINE_LOCATION` permission to `AndroidManifest.xml`.
    *   **Action (Dependency):** Added the `play-services-location` dependency to the project.
    *   **Action (Repository):** Created `LocationRepository.java` to abstract the complexity of the Fused Location Provider and expose location via `LiveData`.
    *   **Action (Runtime Permissions):** Implemented the full runtime permission request flow in `MainActivity.java`, which now asks the user for location access upon login and starts location updates when granted.

*   **Restaurant Data Layer (Step 2):** Built the data layer to fetch nearby restaurant information from the Google Places API.
    *   **Action (Dependency):** Added the `com.google.android.libraries.places:places` dependency.
    *   **Action (API Key):** Securely configured the Google Places API key by reading it from `local.properties` and exposing it to the app via `BuildConfig` and the `AndroidManifest.xml`.
    *   **Action (Model):** Created a `Restaurant.java` data model to represent a restaurant's data within the app.
    *   **Action (Repository):** Created `RestaurantRepository.java`, which uses the `LocationRepository` to get the user's position and then calls the Places API's `searchNearby` method to fetch a list of nearby restaurants, correctly deserializing the response into the app's `Restaurant` model.

## Session: 2025-11-19

### Summary of Work:

*   **Places API Deprecation Update:** Updated the `RestaurantRepository` to fix deprecated Places API usage and ensure correct data fetching.
    *   **Action:** Refactored `RestaurantRepository` to use `SearchNearbyRequest` and `FetchPlaceRequest`.
    *   **Action:** Implemented a two-step fetching process:
        1.  Search for nearby places (getting IDs).
        2.  Fetch details for each place (ID, Name, Address, Rating, Photo Metadata, Location) in parallel.
    *   **Action:** Corrected `Place.Field` usage (using `DISPLAY_NAME` and `FORMATTED_ADDRESS` as per newer API standards).
    *   **Action:** Implemented `Tasks.whenAllSuccess` to wait for all detail fetches before updating the LiveData.
    *   **Action:** Fixed a build error by replacing the deprecated `getLatLng()` method with `getLocation()` in `RestaurantRepository.java`, ensuring compatibility with the latest Places API client.

*   **Map Feature Implementation:** Connected the data layer to the UI to display the user's location and nearby restaurants on a Google Map.
    *   **Action (Architecture):** Created `MapsViewModel` to bridge `LocationRepository` and `RestaurantRepository` with the UI.
    *   **Action (Injection):** Updated `ViewModelFactory` to inject repositories into `MapsViewModel` and accept a `Context` for initialization.
    *   **Action (UI):** Implemented `MapFragment` with Google Maps SDK. It now observes user location to center the camera and adds markers for nearby restaurants.
    *   **Action (Optimization):** Refactored `LocationRepository` to use `getCurrentLocation` (single update) instead of `requestLocationUpdates` (continuous). This saves battery and prevents excessive/costly API calls to the Places API.
    *   **Action (Dependencies):** Added `play-services-maps` to `build.gradle` and `libs.versions.toml`.

## Session: 2026-01-26

### Summary of Work:

*   **Reactive Restaurant Fetching:** Automated the data flow between location updates and restaurant discovery.
    *   **Action (Repository):** Refactored `RestaurantRepository.fetchNearbyRestaurants()` to accept a `Location` parameter, allowing for explicit location-based searches.
    *   **Action (ViewModel):** Updated `MapsViewModel` using `MediatorLiveData` to observe the user's location. When a value is received, it now automatically triggers `fetchNearbyRestaurants(location)` in the repository.
    *   **Action (Architecture):** Improved the separation of concerns by ensuring the ViewModel manages the coordination between location updates and data fetching.

## Session: 2026-03-15

### Summary of Work:

*   **Session Start:** Commencing work on Phase 2 of the development plan.
*   **Current Goal:** Implement the Restaurant List view using a RecyclerView.

## Session: 2026-04-12

### Summary of Work:

*   **Restaurant List Implementation:** Built the foundational UI and logic for the restaurant list.
    *   **Action (UI):** Created `fragment_restaurants.xml` with a `RecyclerView` and `item_restaurant.xml` for the list items.
    *   **Action (Adapter):** Implemented `RestaurantAdapter.java` using **DiffUtil** for efficient list updates and **Glide** for image loading.
    *   **Action (Logic):** Integrated distance calculation and basic opening hours display in the adapter.
    *   **Action (ViewModel):** Refactored `RestaurantsFragment` to share the `MapsViewModel` with `MapFragment`, ensuring synchronized data.

*   **Hybrid Data Merging:** Combined Google Places data with Firestore social data.
    *   **Action (ViewModel):** Updated `MapsViewModel` to "enrich" restaurant data with a real-time workmates count fetched from Firestore using `addSnapshotListener`.
    *   **Action (Repository):** Updated `UserRepository` with queries to count users eating at a specific restaurant.

*   **Restaurant Detail Feature:** Built a comprehensive detail screen.
    *   **Action (UI):** Created `fragment_restaurant_detail.xml` (photo header, FAB, action buttons, workmates list) and `item_workmate_detail.xml`.
    *   **Action (Logic):** Implemented `RestaurantDetailViewModel.java` to handle restaurant details, user selection, and "likes" in Firestore.
    *   **Action (Actions):** Implemented phone calling (`ACTION_DIAL`) and website browsing (`ACTION_VIEW`) via Intents.
    *   **Action (Navigation):** Updated `nav_graph.xml` and implemented click listeners in `MapFragment` and `RestaurantsFragment` to navigate to the details screen using the `restaurantId` argument.
    *   **Action (Firestore):** Implemented `toggleSelection()` and `toggleLike()` logic to allow users to choose their lunch spot and favorite restaurants.
    *   **Action (Social):** Implemented `WorkmateDetailAdapter` to show a real-time list of coworkers joining a specific restaurant.

*   **Code Quality & Maintenance:**
    *   **Action:** Resolved visibility warnings in `RestaurantAdapter`.
    *   **Action:** Fixed a constructor error in `RestaurantsFragment` by correctly passing the click listener.
    *   **Action:** Updated `RestaurantRepository` to use the non-deprecated `NATIONAL_PHONE_NUMBER` field.
    *   **Action:** Improved project stability by ensuring Fragments share ViewModels at the Activity level.

## Session: 2026-04-15

### Summary of Work:

*   **Workmates Tab Implementation (Phase 3):** Built the global list of colleagues and their lunch choices.
    *   **Action (UI):** Created `item_workmate.xml` for the RecyclerView rows.
    *   **Action (ViewModel):** Created `WorkmatesViewModel.java` to observe the global list of users from Firestore in real-time.
    *   **Action (Adapter):** Implemented `WorkmateAdapter.java` with dynamic status messages (e.g., "John Doe hasn't decided yet" vs. "John Doe is eating at...").
    *   **Action (Fragment):** Updated `WorkmatesFragment.java` to display the list using the new adapter and ViewModel.
    *   **Action (Factory):** Updated `ViewModelFactory.java` to support the creation of `WorkmatesViewModel`.

## Session: 2026-04-16

### Summary of Work:

*   **Navigation & UI Polish:**
    *   **Action (Navigation Drawer):** Implemented a `DrawerLayout` with a `NavigationView` and a dynamic header (`nav_header_main.xml`).
    *   **Action (MainActivity):** Updated `MainActivity.java` to handle drawer menu clicks, Logout flow, and the "Your Lunch" direct link feature.
    *   **Action (Header):** Integrated **Glide** in the Activity to load the current user's Google profile info into the drawer header.
    *   **Action (Workmate Navigation):** Updated `WorkmateAdapter` and `WorkmatesFragment` to allow clicking on a colleague's choice to navigate directly to that restaurant's details.

*   **Search Functionality:**
    *   **Action (UI):** Integrated a `SearchView` into the toolbar using the modern **MenuProvider** API.
    *   **Action (ViewModel):** Implemented **Reactive Filtering** in `MapsViewModel` using `MediatorLiveData`. Typing in the search bar now filters both the Map markers and the Restaurant list locally.
    *   **Action (Polish):** Implemented dynamic menu visibility. The search bar automatically hides when on the Workmates tab and reappears on the Map/List tabs.

## Session: 2026-04-22

### Summary of Work:

*   **Real Restaurant Photos Implementation:**
    *   **Action (Model):** Expanded `Restaurant.java` with a `photoUrl` field to support dynamic image loading.
    *   **Action (Repository):** Updated `RestaurantRepository.java` to fetch **resolved Photo URIs** using the Google Places SDK (`fetchResolvedPhotoUri`). Implemented parallel fetching for performance.
    *   **Action (UI):** Updated `RestaurantAdapter` and `RestaurantDetailFragment` to load real photos using **Glide**, replacing placeholders.

*   **Opening Hours Refinement:**
    *   **Action (Logic):** Improved `RestaurantRepository` to determine the current day of the week using `Calendar` and extract the appropriate opening string from `weekdayText`.
    *   **Action (Status):** Integrated `BusinessStatus` checks to handle closed or non-operational restaurants.

## Session: 2026-04-23

### Summary of Work:

*   **Bug Fix - Application Crash at Startup:**
    *   **Action (Theme):** Resolved an `IllegalStateException` by updating `themes.xml` (day and night) to use `NoActionBar` variants. This avoids conflicts when setting a custom Toolbar as the SupportActionBar.
    *   **Action (Dependencies):** Resolved a `NoClassDefFoundError` by adding `com.google.android.gms:play-services-auth` to the project. This was required for the Firebase UI login flow.
    *   **Action (Code Safety):** Added explicit runtime permission checks in `MapFragment.setupMap()` to prevent crashes when accessing location features before permission is granted.

*   **Build & Configuration Polish:**
    *   **Action (API Key Injection):** Refactored `app/build.gradle` to strip quotes from `local.properties` values and correctly inject the `MAPS_API_KEY` into both `BuildConfig` and `AndroidManifest.xml` via `manifestPlaceholders`.
    *   **Action (Library Updates):** Updated `firebase-ui-auth` to version `9.1.1` and added `play-services-auth` version `21.5.1` for better compatibility.

*   **Documentation:**
    *   **Action:** Created `GOOGLE_CLOUD_SETUP_GUIDE.md` to help the developer fix their Google Cloud billing and API key issues.

*   **Current Status:** The app now launches successfully and reaches the login screen. The map is currently empty due to an invalid/fake API key.

## Session: 2026-07-05

### Summary of Work:

*   **UI Refinement - Restaurant Selection:**
    *   **Action (Layout):** Replaced the small circular FloatingActionButton with an **Extended FAB** at the bottom-right of the detail screen. This makes the primary action (selecting a restaurant) much more visible.
    *   **Action (Text):** Added dynamic labels: **"I'M EATING HERE"** (green) vs. **"I CHANGED MY MIND"** (red).
    *   **Action (UX):** Added padding to the workmates list to prevent the new button from overlapping the last list item.

*   **Bug Fix - Selection Persistence:**
    *   **Action (Repository):** Identified and fixed a bug in `UserRepository.createUser()` where restaurant selections were being accidentally overwritten with `null` during startup synchronization. Switched the logic to use a **Map-based merge**, which protects existing fields like `chosenRestaurantId`.

*   **Code Cleanup - Modernization:**
    *   **Action (Back Navigation):** Migrated from the deprecated `onBackPressed()` to the modern `OnBackPressedDispatcher`.
    *   **Action (Colors):** Replaced deprecated `getResources().getColor()` with `ContextCompat.getColor()`.
    *   **Action (Gradle):** Cleaned up `gradle.properties` and updated target SDK to **35**.

## Session: 2026-07-09

### Summary of Work:

*   **Bug Fix - User Profile Synchronization**:
    *   **Problem**: Coworkers list often showed "a coworker" instead of the user's name because Firestore sync only occurred during the sign-in result, not on every app start.
    *   **Action (MainActivity)**: Moved the `mainViewModel.createUser()` call into the `userLiveData` observer. This ensures that the user's profile (name and photo) is synced with Firestore on every app launch and whenever the user state changes.
    *   **Action (MainActivity)**: Cleaned up the redundant `createUser()` call in `onSignInResult`.

*   **Settings Feature - Foundation**:
    *   **Action (UI)**: Created `fragment_settings.xml` with a `MaterialSwitch` for lunch notifications, following Material 3 design.
    *   **Action (Fragment)**: Created `SettingsFragment.java` to handle the settings UI and persist the notification toggle state using `SharedPreferences`.
    *   **Action (Navigation)**: Integrated `SettingsFragment` into `nav_graph.xml`.
    *   **Action (MainActivity)**: Updated the Navigation Drawer listener to navigate to the new `SettingsFragment` when "Settings" is selected.
    *   **Action (Strings)**: Added necessary string resources for the settings screen.

*   **Robust Data Persistence & Manual Location**:
    *   **Fix (Bug)**: Addressed the issue where the restaurant list became empty after locking/unlocking the phone.
    *   **Action (ViewModel)**: Refactored `MapsViewModel` to hold and use the last known location immediately upon initialization, preventing "empty states" during Activity recreation.
    *   **Action (UI)**: Added `SwipeRefreshLayout` to the restaurant list, allowing users to manually trigger a data refresh ("actualize").
    *   **Action (Feature)**: Implemented "Manual Location Selection". Users can now pan the map and click "Search this area" to find restaurants in a different location.
    *   **Action (UI)**: Added a custom "My Location" FAB to the map to easily reset the view and search back to the user's GPS position.

*   **Lunch Notifications Implementation**:
    *   **Action (Dependency)**: Added `androidx.work:work-runtime` for reliable background task management.
    *   **Action (Worker)**: Created `NotificationWorker.java` to handle the logic of fetching lunch choices and workmates in the background.
    *   **Action (Helper)**: Created `WorkManagerHelper.java` to schedule the daily reminder at 12:00 PM.
    *   **Action (UI)**: Connected the Settings toggle to schedule or cancel notifications.
    *   **Action (Permission)**: Implemented `POST_NOTIFICATIONS` permission request flow for Android 13+.
    *   **Action (Data)**: Updated `User` model and Firestore logic to include restaurant addresses, making notifications more informative.

*   **Multi-Language Support (English/French)**:
    *   **Action (Resources)**: Created `values-fr/strings.xml` and translated all app strings into French.
    *   **Action (UI)**: Updated `fragment_settings.xml` with a "cursor switch" (MaterialSwitch) and flag emojis to toggle between English and French.
    *   **Action (Logic)**: Implemented runtime language switching in `SettingsFragment.java` using the modern `AppCompatDelegate.setApplicationLocales` API.
    *   **Action (Manifest)**: Added `AppLocalesMetadataHolderService` to support automatic locale persistence on Android 13+.

*   **UI Polish - Default Restaurant Image**:
    *   **Action (Resources)**: Created `ic_default_restaurant.xml`, a new vector drawable representing a restaurant icon (fork and knife).
    *   **Action (Adapter)**: Updated `RestaurantAdapter.java` to use the new icon as a placeholder and error image when a restaurant has no photo.
    *   **Action (Fragment)**: Updated `RestaurantDetailFragment.java` to use the same new icon for consistency in the detail view.

*   **Transition to Mock Data Strategy**:
    *   **Goal**: Enable continuous development and UI testing without incurring Google Places API costs ($0 development cost).
    *   **Action (Architecture)**: Introduced the `RestaurantRepository` interface to decouple the UI from the data source.
    *   **Action (Repository)**: Renamed the original repository to `GooglePlacesRepository`.
    *   **Action (Repository)**: Created `MockRestaurantRepository` providing a hardcoded list of 6 restaurants with complete data (Photos, Hours, Phone, Website).
    *   **Action (ViewModel)**: Updated `ViewModelFactory` with a `USE_MOCK` toggle. The app now defaults to the Mock repository.
    *   **Action (Simulation)**: Centered the mock simulation on the **Louvre, Paris** to match the mock data.
    *   **Action (UI)**: Updated `RestaurantsFragment` to use the simulated location for distance calculations, ensuring realistic "nearby" data.
    *   **Action (Map)**: Implemented **Dynamic Marker Coloring**. Markers now turn **Green** if at least one person (including the current user) has selected that restaurant, providing immediate visual feedback.
    *   **Benefit**: The developer can now build, test, and present the app with high-quality data and zero financial risk.

## Session: 2026-07-18

### Summary of Work:

*   **Session Start**: Initialized the development session and reviewed the project's ground rules (`AGENT_RULES.md`) and architectural status (`APP_ARCHITECTURE_PLAN.md`, `ARCHITECTURAL_SHIFTS.md`).
*   **Documentation Consolidation**: Merged `ARCHITECTURAL_SHIFTS.md` into `APP_ARCHITECTURE_PLAN.md`.
    *   **Action**: Updated `APP_ARCHITECTURE_PLAN.md` with modern rationales for Manual DI, Places SDK, and Single-Activity Architecture, preparing the documentation for jury review.
    *   **Action**: Deleted the redundant `ARCHITECTURAL_SHIFTS.md` file.
*   **Documentation**: Updated the development log to start the new session.
