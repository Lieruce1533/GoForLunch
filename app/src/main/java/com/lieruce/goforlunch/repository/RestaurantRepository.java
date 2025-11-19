package com.lieruce.goforlunch.repository;

import android.content.Context;
import android.location.Location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.api.net.SearchNearbyRequest;
import com.lieruce.goforlunch.BuildConfig;
import com.lieruce.goforlunch.model.Restaurant;

import java.util.ArrayList;
import java.util.Arrays;
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

        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LOCATION, Place.Field.RATING, Place.Field.PHOTO_METADATAS);

        SearchNearbyRequest request = SearchNearbyRequest.builder(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()), 1500) // 1500 meters radius
                .addPlaceType(Place.Type.RESTAURANT)
                .build();

        placesClient.searchNearby(request).addOnSuccessListener((response) -> {
            List<Restaurant> restaurants = new ArrayList<>();
            for (Place place : response.getPlaces()) {
                LatLng latLng = place.getLatLng();
                if (latLng != null) {
                    Restaurant restaurant = new Restaurant(
                            place.getId(),
                            place.getName(),
                            place.getAddress(),
                            place.getRating() != null ? place.getRating() : 0.0,
                            place.getPhotoMetadatas(), // Correctly pass the PhotoMetadata list
                            latLng.latitude,
                            latLng.longitude
                    );
                    restaurants.add(restaurant);
                }
            }
            nearbyRestaurantsLiveData.setValue(restaurants);
        }).addOnFailureListener(Throwable::printStackTrace);
    }
}
