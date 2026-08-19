# Understanding Cloud Firestore Architecture - Go4Lunch 📂

This document provides a detailed overview of the final Firestore database structure used in Go4Lunch, including the strategic pivots made during development to optimize for performance, accuracy, and cost.

---

### 🏛️ The Architecture: User-Centric Data

While early plans suggested a separate `restaurants` collection, the final architecture uses a **User-Centric** model. App-specific social data is stored directly within the user profile.

#### 1. The `users` Collection
This is the primary source of social state in the app.
- **Document ID**: The Firebase Auth `uid`.
- **Fields**:
    - `username`: The name displayed to coworkers.
    - `avatarUrl`: Link to the profile picture.
    - `chosenRestaurantId`: The Google `place_id` for today's lunch.
    - `chosenRestaurantName`: Cached name to avoid redundant API calls.
    - `chosenRestaurantAddress`: Cached address for notifications and lists.
    - `likedRestaurants`: An array of IDs representing the user's favorites.

#### 2. The `chat_messages` Collection
Manages the real-time global conversation.
- **Document ID**: Auto-generated.
- **Fields**: `senderId`, `text`, `timestamp`, etc.

---

### 💡 Why we don't use a `restaurants` collection

During development, we decided **not** to mirror Google's restaurant data in our own Firestore. Here is the rationale for your jury presentation:

1.  **Data Integrity (Source of Truth)**: Restaurant info (hours, ratings, phone numbers) is volatile. By fetching live from the **Google Places API**, we ensure the user always sees the most accurate, up-to-date information. Mirroring this in Firestore would create "Stale Data" risks.
2.  **API Terms of Service**: Google's policies restrict the permanent caching of their place data. Our architecture complies with these rules by only caching minimal info (name/address) *temporarily* when a user makes an active choice.
3.  **Architectural Efficiency**: By storing the `chosenRestaurantName` directly in the User's document, we can display the "Workmates" list and send "Daily Notifications" using **one single database read**. We don't need to perform a second, slow lookup in a separate collection.

---

### 💰 Balancing Firestore vs. API Costs

Our data strategy is optimized for $0 development costs:

- **Tiered Fetching**: We use "Basic" (free/cheap) fields for the general list and only request "Advanced" (richer) data on the Detail screen when the user shows clear intent.
- **Mock Fallback**: The app includes a **Presentation Mode** that swaps these real-time sources for static local data, ensuring a 100% reliable demo for the jury without any financial risk.

---

### 🔄 Real-Time "Magic": Snapshot Listeners

The core of the social experience is driven by Firestore's `addSnapshotListener`. Instead of manual refreshes, the app "listens" to the `users` collection.
- When **User A** picks a restaurant, **User B**'s map instantly turns that pin **Green**.
- This is achieved by reactive ViewModels that transform these raw Firestore snapshots into live UI updates.
