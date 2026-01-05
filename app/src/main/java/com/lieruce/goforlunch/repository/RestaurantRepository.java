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
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.net.SearchNearbyRequest;
import com.lieruce.goforlunch.BuildConfig;
import com.lieruce.goforlunch.model.Restaurant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RestaurantRepository {

    private static volatile RestaurantRepository instance;
    private final PlacesClient placesClient;
    private final LocationRepository locationRepository;
    private final MutableLiveData<List<Restaurant>> nearbyRestaurantsLiveData = new MutableLiveData<>();

    private RestaurantRepository(Context context) {
        Places.initializeWithNewPlacesApiEnabled(context, BuildConfig.MAPS_API_KEY);
        placesClient = Places.createClient(context);
        locationRepository = LocationRepository.getInstance(context);
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

    public void fetchNearbyRestaurants() {
        Location lastKnownLocation = locationRepository.getLocationLiveData().getValue();
        if (lastKnownLocation == null) {
            return; // Can't fetch restaurants without a location
        }

        // Define the search area
        LatLng center = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
        double radiusInMeters = 1500;
        double latOffset = radiusInMeters / 111111.1;
        double lngOffset = radiusInMeters / (111111.1 * Math.cos(Math.toRadians(center.latitude)));
        LatLng southWest = new LatLng(center.latitude - latOffset, center.longitude - lngOffset);
        LatLng northEast = new LatLng(center.latitude + latOffset, center.longitude + lngOffset);
        LocationRestriction locationRestriction = RectangularBounds.newInstance(southWest, northEast);

        // 1. Search for nearby restaurants (minimal data).
        SearchNearbyRequest searchRequest = SearchNearbyRequest.builder(locationRestriction, Collections.singletonList(Place.Field.ID))
                .setIncludedTypes(Collections.singletonList("restaurant"))
                .build();

        placesClient.searchNearby(searchRequest).addOnSuccessListener(searchResponse -> {
            List<Task<FetchPlaceResponse>> fetchPlaceTasks = new ArrayList<>();

            // 2. For each minimal place, create a task to fetch its full details.
            for (Place minimalPlace : searchResponse.getPlaces()) {
                if (minimalPlace.getId() != null) {
                    // **FIXED**: Using the correct Place.Field constants you found.
                    List<Place.Field> detailFields = Arrays.asList(Place.Field.ID, Place.Field.DISPLAY_NAME, Place.Field.FORMATTED_ADDRESS, Place.Field.LOCATION, Place.Field.RATING, Place.Field.PHOTO_METADATAS);
                    FetchPlaceRequest fetchRequest = FetchPlaceRequest.newInstance(minimalPlace.getId(), detailFields);
                    fetchPlaceTasks.add(placesClient.fetchPlace(fetchRequest));
                }
            }

            // 3. Wait for all the detail-fetch tasks to complete.
            Tasks.whenAllSuccess(fetchPlaceTasks).addOnSuccessListener(responses -> {
                List<Restaurant> restaurants = new ArrayList<>();
                for (Object response : responses) {
                    // **FIXED**: Correctly casting the response and getting the Place object.
                    Place detailedPlace = ((FetchPlaceResponse) response).getPlace();
                    LatLng latLng = detailedPlace.getLocation();
                    if (latLng != null) {
                        Restaurant restaurant = new Restaurant(
                                detailedPlace.getId(),
                                detailedPlace.getDisplayName(),      // Correct getter is still getName()
                                detailedPlace.getFormattedAddress(),   // Correct getter is still getAddress()
                                detailedPlace.getRating() != null ? detailedPlace.getRating() : 0.0,
                                detailedPlace.getPhotoMetadatas(),
                                latLng.latitude,
                                latLng.longitude
                        );
                        restaurants.add(restaurant);
                    }
                }
                nearbyRestaurantsLiveData.setValue(restaurants);
            });
        }).addOnFailureListener(Throwable::printStackTrace);
    }
}
