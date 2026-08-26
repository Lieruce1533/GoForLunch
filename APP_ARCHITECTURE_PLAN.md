# Go4Lunch Architecture & Reactive Data Strategy 🏛️📡

This document explains the technical solutions implemented to handle real-time synchronization, authentication race conditions, and API cost optimizations.

---

### 1. The "Fetch Once" Optimization 💰
**Problem**: Google Places API costs can escalate if the app re-fetches restaurants every time the user moves a few meters.
**Solution**: Implemented a "Session Lock" using an `isInitialFetchDone` flag in `MapsViewModel`. 
- The app fetches restaurants exactly **one time** per session (as soon as a valid location is found).
- Subsequent movements do not trigger new API calls, strictly adhering to project cost-management goals.

---

### 2. Solving the "Empty List" (The Timing Trap) 🕒
**Problem**: On first login, lists appeared empty because Firestore listeners were starting *before* authentication was finished. Firestore silently blocks unauthenticated requests, and the app never "retried" once the login was complete.
**Solution**: Implemented **Auth-Driven Reactive Listening**.
- Instead of starting listeners in the constructor or via manual Activity triggers, ViewModels now **observe the Auth state**.
- As soon as a valid `uid` is detected, the ViewModels automatically open the Firestore SnapshotListeners. 
- This guarantees that the "Permission Wall" is never hit.

---

### 3. Real-Time UI Refresh (The DiffUtil Trap) 🔄
**Problem**: When a user changed their lunch choice, the UI sometimes failed to refresh, showing "ancient" data.
**Solution**: **Deep Copying & Fresh State Emission**.
- Android's `DiffUtil` compares object references. If we modify an object "in-place," the memory address stays the same, and the UI thinks nothing changed.
- We refactored `applyEnrichment` to create **brand-new instances** of the Restaurant objects whenever social data changes.
- This forces a clean UI redraw and ensures 100% accurate real-time updates.

---

### 5. Automated Quality Assurance 🧪
**Status**: 100% Pass
- **Unit Testing**: 8 tests covering core logic, including the new reactive sync and social filtering.
- **Instrumented UI Testing**: 4 tests verifying navigation and main user flows on a physical device.
- **Verification Command**: `./gradlew test connectedAndroidTest`
