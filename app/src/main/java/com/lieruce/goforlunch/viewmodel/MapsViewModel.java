package com.lieruce.goforlunch.viewmodel;

import android.location.Location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lieruce.goforlunch.model.Restaurant;
import com.lieruce.goforlunch.repository.LocationRepository;
import com.lieruce.goforlunch.repository.MockRestaurantRepository;
import com.lieruce.goforlunch.repository.RestaurantRepository;
import com.lieruce.goforlunch.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

public class MapsViewModel extends ViewModel {

    private final LocationRepository locationRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    
    private final MutableLiveData<Location> manualLocation = new MutableLiveData<>();
    private final MediatorLiveData<Location> locationToUse = new MediatorLiveData<>();
    private final MediatorLiveData<List<Restaurant>> nearbyRestaurants = new MediatorLiveData<>();
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MediatorLiveData<List<Restaurant>> filteredRestaurants = new MediatorLiveData<>();
    
    // Cache for social data (counts and likes) to prevent flickering
    private final java.util.Map<String, Integer> restaurantWorkmateCounts = new java.util.HashMap<>();
    private final java.util.Map<String, Integer> restaurantLikeCounts = new java.util.HashMap<>();

    public MapsViewModel(LocationRepository locationRepository, 
                         RestaurantRepository restaurantRepository,
                         UserRepository userRepository) {
        this.locationRepository = locationRepository;
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;

        // --- SIMULATION TRICK ---
        // Let's start at the Louvre (central to our mock restaurants)
        if (restaurantRepository instanceof MockRestaurantRepository) {
            Location louvre = new Location("mock");
            louvre.setLatitude(48.8606);
            louvre.setLongitude(2.3376);
            manualLocation.setValue(louvre);
        }

        // Combine GPS location and manual location
        locationToUse.addSource(locationRepository.getLocationLiveData(), location -> {
            if (manualLocation.getValue() == null && location != null) {
                locationToUse.setValue(location);
            }
        });
        locationToUse.addSource(manualLocation, location -> {
            if (location != null) {
                locationToUse.setValue(location);
            }
        });

        // 1. Automatically trigger restaurant fetch from Google when location to use changes
        nearbyRestaurants.addSource(locationToUse, location -> {
            if (location != null) {
                android.util.Log.d("MapsViewModel", "Fetching for location: " + location.getLatitude() + ", " + location.getLongitude());
                restaurantRepository.fetchNearbyRestaurants(location);
            }
        });

        // Trigger an initial fetch if we already have a location
        Location initialLocation = locationRepository.getLocationLiveData().getValue();
        if (initialLocation != null) {
            locationToUse.setValue(initialLocation);
        }

        // 2. When Google returns restaurants, enrich them with cached Firestore data
        nearbyRestaurants.addSource(restaurantRepository.getNearbyRestaurantsLiveData(), restaurants -> {
            if (restaurants != null) {
                applyEnrichment(restaurants);
                nearbyRestaurants.setValue(restaurants);
            }
        });

        // 3. Listen once for ALL user choices and likes to update counts in real-time
        userRepository.getAllUsers().addSnapshotListener((value, error) -> {
            if (error == null && value != null) {
                updateSocialData(value.toObjects(com.lieruce.goforlunch.model.User.class));
            }
        });

        // 4. Setup filtered restaurants based on nearby list and search query
        filteredRestaurants.addSource(nearbyRestaurants, this::applyFilter);
        filteredRestaurants.addSource(searchQuery, query -> applyFilter(nearbyRestaurants.getValue()));
    }

    private void updateSocialData(List<com.lieruce.goforlunch.model.User> users) {
        restaurantWorkmateCounts.clear();
        restaurantLikeCounts.clear();
        
        for (com.lieruce.goforlunch.model.User user : users) {
            // Update Workmate Counts (Who is going where today)
            String rid = user.getChosenRestaurantId();
            if (rid != null && !rid.isEmpty()) {
                int count = restaurantWorkmateCounts.containsKey(rid) ? restaurantWorkmateCounts.get(rid) : 0;
                restaurantWorkmateCounts.put(rid, count + 1);
            }
            
            // Update Like Counts (Who likes what in total)
            List<String> liked = user.getLikedRestaurants();
            if (liked != null) {
                for (String likedId : liked) {
                    int lCount = restaurantLikeCounts.containsKey(likedId) ? restaurantLikeCounts.get(likedId) : 0;
                    restaurantLikeCounts.put(likedId, lCount + 1);
                }
            }
        }
        
        // Re-apply enrichment to the current list
        List<Restaurant> current = nearbyRestaurants.getValue();
        if (current != null) {
            applyEnrichment(current);
            nearbyRestaurants.setValue(current);
        }
    }

    private void applyEnrichment(List<Restaurant> restaurants) {
        for (Restaurant r : restaurants) {
            // Apply Workmate counts
            Integer wCount = restaurantWorkmateCounts.get(r.getId());
            r.setWorkmatesCount(wCount != null ? wCount : 0);
            
            // Apply Hybrid Star Rating (Social-driven)
            Integer likes = restaurantLikeCounts.get(r.getId());
            int stars;
            if (likes == null || likes == 0) stars = 0;
            else if (likes <= 2) stars = 1;
            else if (likes <= 5) stars = 2;
            else stars = 3;
            
            r.setStars(stars);
        }
    }

    private void applyFilter(List<Restaurant> restaurants) {
        String query = searchQuery.getValue();
        if (restaurants == null) {
            filteredRestaurants.setValue(null);
            return;
        }
        if (query == null || query.isEmpty()) {
            filteredRestaurants.setValue(restaurants);
            return;
        }

        List<Restaurant> filtered = new ArrayList<>();
        for (Restaurant r : restaurants) {
            if (r.getName().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(r);
            }
        }
        filteredRestaurants.setValue(filtered);
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    public LiveData<Location> getUserLocation() {
        return locationRepository.getLocationLiveData();
    }

    public LiveData<List<Restaurant>> getNearbyRestaurants() {
        return filteredRestaurants; // Now returning filtered results
    }

    public void refreshLocation() {
        locationRepository.startLocationUpdates();
        // Force a refresh even if location hasn't changed
        Location current = locationToUse.getValue();
        if (current != null) {
            restaurantRepository.fetchNearbyRestaurants(current);
        }
    }

    public void setManualLocation(Location location) {
        manualLocation.setValue(location);
    }

    public void resetToCurrentLocation() {
        manualLocation.setValue(null);
        Location gps = locationRepository.getLocationLiveData().getValue();
        if (gps != null) {
            locationToUse.setValue(gps);
        }
    }

    public LiveData<Location> getLocationToUse() {
        return locationToUse;
    }
}
