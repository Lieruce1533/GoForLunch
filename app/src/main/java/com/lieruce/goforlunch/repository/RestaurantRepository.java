package com.lieruce.goforlunch.repository;

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
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.FetchResolvedPhotoUriRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.net.SearchNearbyRequest;
import com.lieruce.goforlunch.BuildConfig;
import com.lieruce.goforlunch.model.Restaurant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class RestaurantRepository {

    private static volatile RestaurantRepository instance;
    private final PlacesClient placesClient;
    private final MutableLiveData<List<Restaurant>> nearbyRestaurantsLiveData = new MutableLiveData<>();

    private RestaurantRepository(Context context) {
        Places.initializeWithNewPlacesApiEnabled(context, BuildConfig.MAPS_API_KEY);
        placesClient = Places.createClient(context);
    }

    public static RestaurantRepository getInstance(Context context) {
        if (instance == null) {
            synchronized (RestaurantRepository.class) {
                if (instance == null) {
                    instance = new RestaurantRepository(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    public LiveData<List<Restaurant>> getNearbyRestaurantsLiveData() {
        return nearbyRestaurantsLiveData;
    }

    public void fetchNearbyRestaurants(Location location) {
        if (location == null) {
            Log.d("RestaurantRepository", "Location is null, skipping search");
            return;
        }

        Log.d("RestaurantRepository", "Fetching restaurants for location: " + location.getLatitude() + ", " + location.getLongitude());
        LatLng center = new LatLng(location.getLatitude(), location.getLongitude());
        double radiusInMeters = 3000; // Increased radius to 3km
        LocationRestriction locationRestriction = CircularBounds.newInstance(center, radiusInMeters);

        SearchNearbyRequest searchRequest = SearchNearbyRequest.builder(locationRestriction, Collections.singletonList(Place.Field.ID))
                .setIncludedTypes(Arrays.asList("restaurant", "cafe", "bakery", "bar"))
                .setMaxResultCount(20)
                .build();

        placesClient.searchNearby(searchRequest).addOnSuccessListener(searchResponse -> {
            Log.d("RestaurantRepository", "Found " + searchResponse.getPlaces().size() + " nearby places");
            if (searchResponse.getPlaces().isEmpty()) {
                nearbyRestaurantsLiveData.setValue(new ArrayList<>());
                return;
            }
            List<Task<FetchPlaceResponse>> fetchPlaceTasks = new ArrayList<>();

            for (Place minimalPlace : searchResponse.getPlaces()) {
                if (minimalPlace.getId() != null) {
                    List<Place.Field> detailFields = Arrays.asList(
                            Place.Field.ID,
                            Place.Field.DISPLAY_NAME,
                            Place.Field.FORMATTED_ADDRESS,
                            Place.Field.LOCATION,
                            Place.Field.RATING,
                            Place.Field.PHOTO_METADATAS,
                            Place.Field.OPENING_HOURS,
                            Place.Field.CURRENT_OPENING_HOURS,
                            Place.Field.BUSINESS_STATUS,
                            Place.Field.NATIONAL_PHONE_NUMBER,
                            Place.Field.WEBSITE_URI,
                            Place.Field.TYPES,
                            Place.Field.PRIMARY_TYPE
                    );
                    FetchPlaceRequest fetchRequest = FetchPlaceRequest.newInstance(minimalPlace.getId(), detailFields);
                    fetchPlaceTasks.add(placesClient.fetchPlace(fetchRequest));
                }
            }

            Tasks.whenAllComplete(fetchPlaceTasks).addOnCompleteListener(allTasks -> {
                List<Task<Restaurant>> restaurantTasks = new ArrayList<>();

                for (Task<FetchPlaceResponse> task : fetchPlaceTasks) {
                    if (task.isSuccessful() && task.getResult() != null) {
                        Place detailedPlace = task.getResult().getPlace();
                        restaurantTasks.add(processPlace(detailedPlace));
                    }
                }

                if (restaurantTasks.isEmpty()) {
                    nearbyRestaurantsLiveData.setValue(new ArrayList<>());
                    return;
                }

                Tasks.whenAllComplete(restaurantTasks).addOnCompleteListener(allRestaurantTasks -> {
                    List<Restaurant> restaurants = new ArrayList<>();
                    for (Task<Restaurant> task : restaurantTasks) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            restaurants.add(task.getResult());
                        }
                    }
                    Log.d("RestaurantRepository", "Processed " + restaurants.size() + " restaurants");
                    nearbyRestaurantsLiveData.setValue(restaurants);
                });
            });
        }).addOnFailureListener(e -> {
            Log.e("RestaurantRepository", "Error searching nearby: " + (e.getMessage() != null ? e.getMessage() : "Unknown error"));
            nearbyRestaurantsLiveData.setValue(new ArrayList<>()); // Emit empty to stop loading
        });
    }

    private Task<Restaurant> processPlace(Place detailedPlace) {
        if (!isEstablishmentARestaurant(detailedPlace)) {
            return Tasks.forResult(null);
        }

        LatLng latLng = detailedPlace.getLocation();
        if (latLng == null) return Tasks.forResult(null);

        String openingHours = formatOpeningHours(detailedPlace);

        if (detailedPlace.getPhotoMetadatas() != null && !detailedPlace.getPhotoMetadatas().isEmpty()) {
            FetchResolvedPhotoUriRequest photoRequest = FetchResolvedPhotoUriRequest.builder(detailedPlace.getPhotoMetadatas().get(0))
                    .setMaxWidth(400)
                    .setMaxHeight(400)
                    .build();

            return placesClient.fetchResolvedPhotoUri(photoRequest).continueWith(task -> {
                String photoUrl = null;
                if (task.isSuccessful() && task.getResult() != null && task.getResult().getUri() != null) {
                    photoUrl = task.getResult().getUri().toString();
                }
                return new Restaurant(
                        detailedPlace.getId(),
                        detailedPlace.getDisplayName(),
                        detailedPlace.getFormattedAddress(),
                        detailedPlace.getRating() != null ? detailedPlace.getRating() : 0.0,
                        detailedPlace.getPhotoMetadatas(),
                        latLng.latitude,
                        latLng.longitude,
                        openingHours,
                        detailedPlace.getNationalPhoneNumber(),
                        detailedPlace.getWebsiteUri() != null ? detailedPlace.getWebsiteUri().toString() : null,
                        photoUrl
                );
            });
        } else {
            return Tasks.forResult(new Restaurant(
                    detailedPlace.getId(),
                    detailedPlace.getDisplayName(),
                    detailedPlace.getFormattedAddress(),
                    detailedPlace.getRating() != null ? detailedPlace.getRating() : 0.0,
                    detailedPlace.getPhotoMetadatas(),
                    latLng.latitude,
                    latLng.longitude,
                    openingHours,
                    detailedPlace.getNationalPhoneNumber(),
                    detailedPlace.getWebsiteUri() != null ? detailedPlace.getWebsiteUri().toString() : null,
                    null
            ));
        }
    }

    private boolean isEstablishmentARestaurant(Place place) {
        // 1. Check Primary Type (Most accurate)
        if (place.getPrimaryType() != null) {
            String primary = place.getPrimaryType().toLowerCase();
            List<String> explicitlyExcludedPrimary = Arrays.asList(
                    "gas_station", "supermarket", "grocery_or_supermarket", 
                    "convenience_store", "car_repair", "car_wash", "lodging", "store"
            );
            if (explicitlyExcludedPrimary.contains(primary)) return false;
        }

        // 2. Check all types as fallback
        if (place.getPlaceTypes() == null) return true;
        List<String> types = place.getPlaceTypes();

        List<String> validTypes = Arrays.asList("restaurant", "cafe", "bakery", "bar", "meal_takeaway", "food", "establishment", "point_of_interest");
        boolean hasValidType = false;
        for (String type : types) {
            if (validTypes.contains(type.toLowerCase())) {
                hasValidType = true;
                break;
            }
        }
        return hasValidType;
    }

    private String formatOpeningHours(Place place) {
        if (place.getBusinessStatus() != null && place.getBusinessStatus() != Place.BusinessStatus.OPERATIONAL) {
            return place.getBusinessStatus().name();
        }

        List<String> weekdayText = null;
        if (place.getCurrentOpeningHours() != null) {
            weekdayText = place.getCurrentOpeningHours().getWeekdayText();
        } else if (place.getOpeningHours() != null) {
            weekdayText = place.getOpeningHours().getWeekdayText();
        }

        if (weekdayText != null && !weekdayText.isEmpty()) {
            int dayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
            // Calendar.MONDAY = 2, ..., Calendar.SUNDAY = 1
            // WeekdayText usually starts with Monday (0) to Sunday (6)
            int index = (dayOfWeek + 5) % 7; // Convert Calendar day to 0-indexed Monday
            if (index < weekdayText.size()) {
                return weekdayText.get(index);
            }
            return weekdayText.get(0);
        }

        return "Opening info not available";
    }
}
