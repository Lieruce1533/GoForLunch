package com.lieruce.goforlunch.viewmodel;

import android.location.Location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.lieruce.goforlunch.model.Restaurant;
import com.lieruce.goforlunch.repository.LocationRepository;
import com.lieruce.goforlunch.repository.RestaurantRepository;

import java.util.List;

public class MapsViewModel extends ViewModel {

    private final LocationRepository locationRepository;
    private final RestaurantRepository restaurantRepository;
    private final MediatorLiveData<List<Restaurant>> nearbyRestaurants = new MediatorLiveData<>();

    public MapsViewModel(LocationRepository locationRepository, RestaurantRepository restaurantRepository) {
        this.locationRepository = locationRepository;
        this.restaurantRepository = restaurantRepository;

        // Automatically trigger restaurant fetch when location changes
        nearbyRestaurants.addSource(locationRepository.getLocationLiveData(), location -> {
            if (location != null) {
                restaurantRepository.fetchNearbyRestaurants(location);
            }
        });

        // Bridge the repository's results to the ViewModel's LiveData
        nearbyRestaurants.addSource(restaurantRepository.getNearbyRestaurantsLiveData(), nearbyRestaurants::setValue);
    }

    public LiveData<Location> getUserLocation() {
        return locationRepository.getLocationLiveData();
    }

    public LiveData<List<Restaurant>> getNearbyRestaurants() {
        return nearbyRestaurants;
    }

    public void refreshLocation() {
        locationRepository.startLocationUpdates();
    }
}
