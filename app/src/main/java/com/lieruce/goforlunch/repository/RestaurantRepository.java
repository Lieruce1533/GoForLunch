package com.lieruce.goforlunch.repository;

import android.content.Context;
import android.location.Location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.LocationRestriction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.RectangularBounds;
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
            return;
        }

        LatLng center = new LatLng(location.getLatitude(), location.getLongitude());
        double radiusInMeters = 1500;
        double latOffset = radiusInMeters / 111111.1;
        double lngOffset = radiusInMeters / (111111.1 * Math.cos(Math.toRadians(center.latitude)));
        LatLng southWest = new LatLng(center.latitude - latOffset, center.longitude - lngOffset);
        LatLng northEast = new LatLng(center.latitude + latOffset, center.longitude + lngOffset);
        LocationRestriction locationRestriction = RectangularBounds.newInstance(southWest, northEast);

        SearchNearbyRequest searchRequest = SearchNearbyRequest.builder(locationRestriction, Collections.singletonList(Place.Field.ID))
                .setIncludedTypes(Collections.singletonList("restaurant"))
                .build();

        placesClient.searchNearby(searchRequest).addOnSuccessListener(searchResponse -> {
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
                            Place.Field.WEBSITE_URI
                    );
                    FetchPlaceRequest fetchRequest = FetchPlaceRequest.newInstance(minimalPlace.getId(), detailFields);
                    fetchPlaceTasks.add(placesClient.fetchPlace(fetchRequest));
                }
            }

            Tasks.whenAllSuccess(fetchPlaceTasks).addOnSuccessListener(responses -> {
                List<Task<Restaurant>> restaurantTasks = new ArrayList<>();

                for (Object response : responses) {
                    Place detailedPlace = ((FetchPlaceResponse) response).getPlace();
                    restaurantTasks.add(processPlace(detailedPlace));
                }

                Tasks.whenAllSuccess(restaurantTasks).addOnSuccessListener(processedRestaurants -> {
                    List<Restaurant> restaurants = new ArrayList<>();
                    for (Object r : processedRestaurants) {
                        restaurants.add((Restaurant) r);
                    }
                    nearbyRestaurantsLiveData.setValue(restaurants);
                });
            });
        }).addOnFailureListener(Throwable::printStackTrace);
    }

    private Task<Restaurant> processPlace(Place detailedPlace) {
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
