I've read the `APP_ARCHITECTURE_PLAN.md` file, and you're right to be cautious. We have made several significant **architectural shifts** during our development that are **not** currently reflected in that plan.

If you don't update this file before your IDE update, you might lose the "roadmap" of how we *actually* built the app. Here are the three main things that should be corrected in the plan:

### 1. Dependency Injection (Hilt vs. Factory)
*   **Plan says**: "Hilt will be used to manage dependencies."
*   **Reality**: We are currently using a manual **`ViewModelFactory`** (Singleton pattern). We decided to stick with this to keep things clear and educational without the complexity of Hilt annotations for now.

### 2. Networking (Retrofit vs. Places SDK)
*   **Plan says**: "Retrofit will be used to handle all network calls to the Google Places API."
*   **Reality**: We are using the official **Google Places SDK for Android** (`PlacesClient`). It handles the networking and JSON parsing for us, which is more robust and safer than manual Retrofit calls for this specific API.

### 3. Single-Activity vs. Multiple Activities
*   **Plan says**: "Create a `RestaurantDetailActivity`."
*   **Reality**: We implemented a **Single-Activity Architecture**. The details are in a `RestaurantDetailFragment`, and everything is managed by the **Jetpack Navigation Component**. This makes transitions smoother and the app more modern.