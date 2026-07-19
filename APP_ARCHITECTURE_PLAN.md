# Go4Lunch - Architectural & Development Plan

This document outlines the architectural strategy, technical choices, and development roadmap for the Go4Lunch application.

## 1. Core Principles & Modern Tooling

- **Architecture**: **Model-View-ViewModel (MVVM)** is used to ensure a clean separation of concerns, making the app testable and maintainable.
- **Dependency Injection**: **Manual Dependency Injection** via a central `ViewModelFactory`. This approach prioritizes transparency and a deep understanding of object lifecycles over library-based "magic."
- **Networking & Data**: **Google Places SDK for Android**. Instead of manual REST calls, we leverage the official SDK for robust, type-safe, and optimized interaction with Google's location services.
- **UI Architecture**: **Single-Activity Architecture**. The entire application flows through a single `MainActivity` using the **Jetpack Navigation Component** to manage Fragments and back-stack logic.
- **Data Persistence & Backend**: **Firebase Firestore** is used for real-time, app-specific data (user profiles, daily restaurant choices, and social interactions).
- **API Cost & Performance**: Optimized via a **Repository Pattern** and a **Mock Data Strategy**. We implement caching and simulated data providers to ensure $0 development costs while maintaining high performance.

---

## 2. Architectural Rationales (Jury Presentation)

During development, several strategic decisions were made to modernize the project beyond the original 2022/2023 requirements. These choices demonstrate technical maturity and adaptation to the current Android ecosystem.

### A. Manual DI vs. Hilt/Dagger
*   **Choice**: Manual Injection using a Singleton `ViewModelFactory`.
*   **Rationale**: While Hilt is standard for enterprise scale, manual DI demonstrates a fundamental mastery of the **Dependency Inversion Principle**. By manually managing dependencies, we ensure full control over object creation and avoid the overhead/complexity of annotation processing for this specific use case. It makes the architecture "transparent" for educational and evaluation purposes.

### B. Google Places SDK vs. Retrofit
*   **Choice**: Official Google Places SDK.
*   **Rationale**: Retrofit is a tool for building clients for raw REST APIs. However, Google provides a dedicated SDK that offers higher-level abstractions (returning `Place` objects instead of JSON), built-in security, and optimized networking specifically for their services. Using the SDK is the "official" and more robust industry practice.

### C. Single-Activity Architecture
*   **Choice**: Jetpack Navigation Component.
*   **Rationale**: Multiple Activities are now considered legacy for standard apps. A Single-Activity approach provides smoother transitions, easier data sharing via Activity-scoped ViewModels, and follows Google's "Modern Android Development" (MAD) recommendations.

---

## 3. Data Strategy: Hybrid Repository Pattern

The app uses a **Repository Pattern** to unify two distinct data streams:

1.  **Public Data (Google Places)**: Static info (names, addresses, ratings). Fetched fresh or simulated via `MockRestaurantRepository` for cost efficiency.
2.  **Private Data (Firebase Firestore)**: Dynamic social info (Who is eating where? Who "liked" this place?).

The **Repository** merges these streams into a single UI-ready model, shielding the ViewModel from the complexity of the data sources.

---

## 4. Development Status & Phased Plan

### Phase 1: Foundation & Navigation [COMPLETED]
- [x] Single-Activity setup with `BottomNavigationView`.
- [x] Firebase Authentication (Google Sign-In) integration.
- [x] Base `UserRepository` with Firestore "upsert" logic.

### Phase 2: Location & Restaurants [COMPLETED]
- [x] `LocationRepository` for GPS management.
- [x] `RestaurantRepository` (Interface) with `GooglePlaces` and `Mock` implementations.
- [x] **Map View**: Reactive marker coloring based on social data.
- [x] **List View**: RecyclerView with real-time distance and opening hours.

### Phase 3: Social & Details [COMPLETED]
- [x] **Restaurant Details**: Real photos, phone/website intents, and workmate lists.
- [x] **Workmates Tab**: Global real-time list of colleagues and their choices.
- [x] Selection logic: "I'm eating here" vs. "I changed my mind."

### Phase 4: Polish & Advanced Features [IN PROGRESS]
- [/] **Search**: Reactive local filtering for maps and lists.
- [/] **Settings**: Notifications toggle and French/English localization.
- [ ] **Notifications**: Daily reminder at 12:00 PM (using WorkManager).

### Phase 5: Quality Assurance [IN PROGRESS]
- [/] **Unit Testing**: Repositories and ViewModels. (See [Testing Roadmap](file:///home/flint/.cache/Google/AndroidStudio2026.1.2/projects/go4lunch.cfea1f15/.artifacts/1a1e91c4-5a45-4afe-8c99-cb9926ebc82f/testing_roadmap.artifact.md))
- [ ] **Instrumented Testing**: Navigation flow and edge cases (offline mode).
