package com.lieruce.goforlunch.repository;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.CircularBounds;
import com.google.android.libraries.places.api.model.LocationRestriction;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchResolvedPhotoUriRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.net.SearchNearbyRequest;
import com.lieruce.goforlunch.BuildConfig;
import com.lieruce.goforlunch.model.Restaurant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Real-world implementation of RestaurantRepository using the Google Places SDK.
 * Implements tiered data fetching and resolved photo management to optimize API quotas.
 */
public class GooglePlacesRepository implements RestaurantRepository {

    @SuppressLint("StaticFieldLeak")
    private static volatile GooglePlacesRepository instance;
    private final Context context;
    private final PlacesClient placesClient;
    private final MutableLiveData<List<Restaurant>> nearbyRestaurantsLiveData = new MutableLiveData<>();
    
    // Cache to avoid re-fetching details for the same restaurant during the session
    private final Map<String, Restaurant> restaurantCache = new HashMap<>();

    private GooglePlacesRepository(Context context) {
        this.context = context.getApplicationContext();
        Places.initializeWithNewPlacesApiEnabled(context, BuildConfig.MAPS_API_KEY);
        placesClient = Places.createClient(context);
    }

    public static GooglePlacesRepository getInstance(Context context) {
        if (instance == null) {
            synchronized (GooglePlacesRepository.class) {
                if (instance == null) {
                    instance = new GooglePlacesRepository(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    @Override
    public LiveData<List<Restaurant>> getNearbyRestaurantsLiveData() {
        return nearbyRestaurantsLiveData;
    }

    /**
     * Searches for nearby food establishments.
     * Restricts results to relevant types (restaurant, bakery, meal_takeaway) to ensure data quality.
     */
    @Override
    public void fetchNearbyRestaurants(Location location) {
        if (location == null) return;

        LatLng center = new LatLng(location.getLatitude(), location.getLongitude());
        LocationRestriction locationRestriction = CircularBounds.newInstance(center, 2000);

        List<Place.Field> searchFields = Arrays.asList(
                Place.Field.ID,
                Place.Field.DISPLAY_NAME,
                Place.Field.FORMATTED_ADDRESS,
                Place.Field.LOCATION,
                Place.Field.RATING,
                Place.Field.TYPES,
                Place.Field.PHOTO_METADATAS
        );

        SearchNearbyRequest searchRequest = SearchNearbyRequest.builder(locationRestriction, searchFields)
                .setIncludedTypes(Arrays.asList("restaurant", "bakery", "meal_takeaway"))
                .setMaxResultCount(20)
                .build();

        placesClient.searchNearby(searchRequest).addOnSuccessListener(searchResponse -> {
            List<Restaurant> restaurants = new ArrayList<>();
            for (Place place : searchResponse.getPlaces()) {
                Restaurant restaurant = mapPlaceToRestaurant(place);
                restaurants.add(restaurant);
                restaurantCache.put(restaurant.getId(), restaurant);
            }
            nearbyRestaurantsLiveData.setValue(restaurants);
        }).addOnFailureListener(e -> {
            Log.e("GooglePlacesRepository", "Search failed: " + e.getMessage());
            android.os.Handler handler = new android.os.Handler(android.os.Looper.getMainLooper());
            handler.post(() -> android.widget.Toast.makeText(context, "Google Places Error: " + e.getMessage(), android.widget.Toast.LENGTH_LONG).show());
            nearbyRestaurantsLiveData.setValue(new ArrayList<>());
        });
    }

    @Override
    public Task<Restaurant> getRestaurantDetails(String placeId) {
        Restaurant cached = restaurantCache.get(placeId);
        if (cached != null && cached.getPhoneNumber() != null) {
            return Tasks.forResult(cached);
        }

        List<Place.Field> detailFields = Arrays.asList(
                Place.Field.ID,
                Place.Field.DISPLAY_NAME,
                Place.Field.FORMATTED_ADDRESS,
                Place.Field.LOCATION,
                Place.Field.RATING,
                Place.Field.PHOTO_METADATAS,
                Place.Field.CURRENT_OPENING_HOURS,
                Place.Field.BUSINESS_STATUS,
                Place.Field.NATIONAL_PHONE_NUMBER,
                Place.Field.WEBSITE_URI
        );

        FetchPlaceRequest request = FetchPlaceRequest.newInstance(placeId, detailFields);
        return placesClient.fetchPlace(request).continueWithTask(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Place place = task.getResult().getPlace();
                Restaurant fullRestaurant = mapPlaceToRestaurant(place);
                
                if (place.getPhotoMetadatas() != null && !place.getPhotoMetadatas().isEmpty()) {
                    return resolvePhotoAndCache(fullRestaurant, place.getPhotoMetadatas().get(0));
                }
                
                restaurantCache.put(placeId, fullRestaurant);
                return Tasks.forResult(fullRestaurant);
            }
            throw task.getException();
        });
    }

    private Task<Restaurant> resolvePhotoAndCache(Restaurant restaurant, PhotoMetadata metadata) {
        FetchResolvedPhotoUriRequest photoRequest = FetchResolvedPhotoUriRequest.builder(metadata)
                .setMaxWidth(800)
                .setMaxHeight(600)
                .build();

        return placesClient.fetchResolvedPhotoUri(photoRequest).continueWith(task -> {
            String photoUrl = null;
            if (task.isSuccessful() && task.getResult() != null) {
                photoUrl = task.getResult().getUri().toString();
            }
            Restaurant finalRestaurant = new Restaurant(
                    restaurant.getId(),
                    restaurant.getName(),
                    restaurant.getAddress(),
                    restaurant.getRating(),
                    restaurant.getPhotoMetadatas(),
                    restaurant.getLatitude(),
                    restaurant.getLongitude(),
                    restaurant.getOpeningHours(),
                    restaurant.getPhoneNumber(),
                    restaurant.getWebsiteUrl(),
                    photoUrl
            );
            restaurantCache.put(restaurant.getId(), finalRestaurant);
            return finalRestaurant;
        });
    }

    private Restaurant mapPlaceToRestaurant(Place place) {
        LatLng loc = place.getLocation();
        String name = "Unknown";
        
        if (place.getDisplayName() != null) {
            name = String.valueOf(place.getDisplayName());
            if (name.contains("text=")) {
                try {
                    name = name.substring(name.indexOf("text=") + 5, name.indexOf(","));
                } catch (Exception ignored) {}
            }
        } else if (place.getName() != null) {
            name = place.getName();
        }

        return new Restaurant(
                place.getId(),
                name,
                place.getFormattedAddress(),
                place.getRating() != null ? place.getRating() : 0.0,
                place.getPhotoMetadatas(),
                loc != null ? loc.latitude : 0,
                loc != null ? loc.longitude : 0,
                getOpeningHoursString(place),
                place.getNationalPhoneNumber(),
                place.getWebsiteUri() != null ? place.getWebsiteUri().toString() : null,
                null
        );
    }
    private String getOpeningHoursString(Place place) {
        if (place.getCurrentOpeningHours() != null && 
            place.getCurrentOpeningHours().getWeekdayText() != null && 
            !place.getCurrentOpeningHours().getWeekdayText().isEmpty()) {
            
            return place.getCurrentOpeningHours().getWeekdayText().get(0);
        }
        return "Check details for hours";
    }
}
