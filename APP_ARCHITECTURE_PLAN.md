# Go4Lunch - Architectural & Development Plan

This document outlines the architectural strategy and step-by-step plan for building the Go4Lunch application.

## 1. Core Principles & Tools

- **Architecture**: Model-View-ViewModel (MVVM) will be used to ensure a clean separation of concerns, making the app testable and maintainable.
- **Dependency Injection**: Hilt will be used to manage dependencies, simplifying the creation of objects and promoting decoupled code.
- **Networking**: Retrofit will be used to handle all network calls to the Google Places API.
- **Data Persistence & Backend**: Firebase Firestore will be used to store app-specific data like user profiles and restaurant choices.
- **API Cost & Performance**: API calls to paid services like Google Places will be optimized. We will implement a caching strategy to fetch data only once per session, reducing costs and data consumption.

## 2. Data Strategy: Combining Two Sources

The app will use two primary data sources. The **Repository Pattern** will be the core of our strategy to manage and combine them.

- **Google Places API (Public Data Source)**:
  - Provides static information about restaurants (name, address, photo, phone number, rating).
  - This data will **not** be stored in our database. It will be fetched fresh to ensure it's always up-to-date.

- **Firebase Firestore (Private Data Source)**:
  - Provides dynamic, app-specific data.
  - Answers questions like: "Who are my users?", "Which restaurant did User X choose today?"

- **The `Repository`'s Role**:
  - The ViewModel will ask the Repository for data (e.g., "Get me a list of restaurants with coworker info").
  - The Repository will intelligently fetch restaurant info from the Places API and user choice info from Firestore, then merge them into a single, unified data model for the UI.

## 3. Proposed Firestore Database Structure

- **`users` collection**:
  - Each document is a user, keyed by their Firebase Auth `uid`.
  - Fields: `username`, `avatarUrl`, `chosenRestaurantId` (the Google `place_id`), `chosenRestaurantName`.

- **`restaurants` collection (for tracking daily choices)**:
  - Each document ID is the Google `place_id` of a restaurant.
  - Will contain a subcollection named `goingUsers` that lists which users have decided to eat at that restaurant for the day.

## 4. Phased Development Plan

We will build the app feature by feature in the following phases.

### Phase 1: The Foundation - Displaying Restaurants on a Map

This is the most critical phase that sets up all the core components.

1.  **Dependencies & Setup**: Add Hilt, Retrofit, Google Maps, and Location Services libraries to the Gradle files.
2.  **Hilt Setup**: Create a custom `Application` class and necessary Hilt modules.
3.  **Location Service**: Create a `LocationProvider` to handle permissions and fetch the user's current GPS location.
4.  **Networking (Retrofit)**: Define a `RestaurantApiService` interface for the Places API and create the data classes (POJOs) to match the API's JSON response.
5.  **Data Layer**: Create the `RestaurantRepository` that will use the `RestaurantApiService` to fetch restaurant data.
6.  **ViewModel & UI**: Create a `MapViewModel` that gets data from the Repository and a `MapFragment` that observes the ViewModel to display restaurant markers.

### Phase 2: Restaurant List & Details View

1.  **Restaurant List**: Create a `RestaurantListFragment` with a `RecyclerView`. It will reuse the existing `MapViewModel` to display the same list of restaurants fetched in Phase 1.
2.  **Restaurant Details**: Create a `RestaurantDetailActivity` that shows detailed information about a restaurant when one is selected. This will involve a new API call to get specific details for that restaurant.

### Phase 3: Coworkers View

1.  **Firestore Setup**: Implement the `users` collection in Firestore.
2.  **Data Layer**: Create a `UserRepository` to fetch coworker data from Firestore.
3.  **ViewModel & UI**: Create a `CoworkersFragment` and a corresponding `CoworkersViewModel` to display a list of coworkers, their avatars, and their chosen restaurants for the day.

### Phase 4: Additional Features

- **Search**: Implement a search bar to filter restaurants.
- **UI/UX Polish**: Implement optional features like making phone calls or launching Google Maps for directions from the detail view.
