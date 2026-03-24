package com.lieruce.goforlunch.viewmodel;

import android.location.Location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.lieruce.goforlunch.model.Restaurant;
import com.lieruce.goforlunch.repository.LocationRepository;
import com.lieruce.goforlunch.repository.RestaurantRepository;
import com.lieruce.goforlunch.repository.UserRepository;

import java.util.List;

public class MapsViewModel extends ViewModel {

    private final LocationRepository locationRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final MediatorLiveData<List<Restaurant>> nearbyRestaurants = new MediatorLiveData<>();

    public MapsViewModel(LocationRepository locationRepository, 
                         RestaurantRepository restaurantRepository,
                         UserRepository userRepository) {
        this.locationRepository = locationRepository;
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;

        // 1. Automatically trigger restaurant fetch from Google when location changes
        nearbyRestaurants.addSource(locationRepository.getLocationLiveData(), location -> {
            if (location != null) {
                restaurantRepository.fetchNearbyRestaurants(location);
            }
        });

        // 2. When Google returns restaurants, enrich them with Firestore data
        nearbyRestaurants.addSource(restaurantRepository.getNearbyRestaurantsLiveData(), restaurants -> {
            if (restaurants != null) {
                enrichRestaurantsWithWorkmates(restaurants);
            }
        });
    }

    private void enrichRestaurantsWithWorkmates(List<Restaurant> restaurants) {
        for (Restaurant restaurant : restaurants) {
            // Add a real-time listener for each restaurant's workmates count
            userRepository.getUsersEatingAt(restaurant.getId()).addSnapshotListener((value, error) -> {
                if (error == null && value != null) {
                    restaurant.setWorkmatesCount(value.size());
                    // Update the LiveData to trigger UI refresh
                    nearbyRestaurants.setValue(restaurants);
                }
            });
        }
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
