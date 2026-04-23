# Google Cloud Setup Guide - Go4Lunch

Follow these steps to restore the Maps and Places functionality in your app.

## 1. Verify Billing Information
Google APIs require a valid billing account, even if you stay within the free tier limits.
1.  Go to the [Google Cloud Console Billing page](https://console.cloud.google.com/billing).
2.  Ensure your credit card is up to date.
3.  Make sure your project (**Go4Lunch**) is linked to an active billing account.

## 2. Enable Required APIs
1.  Go to the [Enabled APIs & Services](https://console.cloud.google.com/apis/dashboard) page.
2.  Click **+ ENABLE APIS AND SERVICES**.
3.  Search for and enable:
    *   **Maps SDK for Android** (For displaying the map).
    *   **Places API (New)** (For fetching restaurant data and photos).

## 3. Generate and Restrict your API Key
1.  Go to the [Credentials page](https://console.cloud.google.com/apis/credentials).
2.  Click **CREATE CREDENTIALS** > **API key**.
3.  **Copy the new key immediately.**
4.  (Highly Recommended) Click **EDIT API KEY** to restrict it:
    *   Under **Set an application restriction**, select **Android apps**.
    *   Click **ADD AN ITEM**.
    *   **Package name**: `com.lieruce.goforlunch`
    *   **SHA-1 certificate fingerprint**: `76:52:8B:28:A7:DB:1D:4F:3D:AC:A5:13:10:94:1C:60:59:8C:33:E2`
    *   Click **DONE** and then **SAVE**.

## 4. Update the Project
1.  Open your `local.properties` file in Android Studio.
2.  Replace the fake key with your new one:
    ```properties
    MAPS_API_KEY=YOUR_REAL_API_KEY_HERE
    ```
3.  **Note**: Do NOT put quotes around the key in `local.properties`. I've updated the build script to handle them automatically.

## 5. Test the App
1.  Clean the project: `Build > Clean Project`.
2.  Run the app again.
3.  Once logged in, the map should load, and you should see markers for nearby restaurants.
