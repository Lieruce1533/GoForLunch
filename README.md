# Go4Lunch 🍽️🚀

Go4Lunch is a collaborative lunch-planning application designed for coworkers. It allows employees to discover nearby restaurants, see where their colleagues are eating, and coordinate lunch plans in real-time.

Built as part of a professional Android development certification, this project demonstrates a modern, scalable, and socially-aware architecture.

---

## 🌟 Key Features

- **Interactive Map**: Discover nearby restaurants with custom-branded markers. Markers turn green when coworkers have joined a location.
- **Restaurant Discovery**: Browse a detailed list of nearby food establishments with real-time distance, popularity ratings, and opening hours.
- **Social Integration**: See which colleagues are joining which restaurant today.
- **Real-Time Group Chat**: Discuss and coordinate plans with all coworkers through a dedicated, themed chat space.
- **Dynamic Rating System**: A hybrid system where restaurant stars are driven by real coworker "Likes" rather than just static API data.
- **Smart Reminders**: Receive a daily notification at 12:00 PM with your lunch choice and a list of colleagues joining you.
- **Bilingual Support**: Full localized experience in both **English** and **French**.
- **Presentation Mode**: A built-in toggle to switch between live GPS/Google data and a stable, simulated mock environment for demonstrations.

---

## 🏗️ Technical Architecture

The application follows **Clean Architecture** principles and Google's **Modern Android Development (MAD)** recommendations.

- **MVVM Pattern**: Clear separation of concerns between UI (View), Business Logic (ViewModel), and Data (Repository).
- **Single Activity Architecture**: Uses the **Jetpack Navigation Component** for all fragment transitions and backstack management.
- **Dependency Injection**: Manual DI via a central `ViewModelFactory` to demonstrate mastery of the Dependency Inversion Principle.
- **Data Layer**:
    - **Google Places SDK**: Official SDK for high-performance location data.
    - **Firebase Firestore**: Real-time NoSQL database for users, social choices, and chat.
    - **Firebase Auth**: Secure Google and Email authentication.
- **Background Tasks**: Powered by **WorkManager** for reliable daily notifications.
- **UI/UX**: 
    - **ViewBinding** for type-safe view access.
    - **Material 3** design components.
    - **Glide** for optimized image loading.
    - Custom Canvas-based Map Markers.

---

## 🧪 Quality Assurance & Security

- **Unit Testing**: 100% coverage of core business logic in Repositories and ViewModels using **JUnit 4** and **Mockito**.
- **Instrumented UI Testing**: Navigation flows and user interactions verified with **Espresso**.
- **Performance**: Disabled Jetifier and optimized build properties for faster execution.
- **Security**: Enabled **R8 Minification** and Obfuscation for the release build to protect intellectual property.
- **Stability**: Implemented a "Mock Infrastructure" to ensure 0% failure rate during live demonstrations.

---

## 🚀 Getting Started

1. **Clone the Repo**: `git clone https://github.com/Lieruce1533/GoForLunch.git`
2. **API Keys**: Add your `MAPS_API_KEY` to the `local.properties` file.
3. **Firebase**: Add your `google-services.json` to the `app/` directory.
4. **Build**: Open in Android Studio and run the `app` module.

---

*Developed with ❤️ as a showcase of modern Android capabilities.*
