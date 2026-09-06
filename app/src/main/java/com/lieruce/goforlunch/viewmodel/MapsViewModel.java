package com.lieruce.goforlunch.viewmodel;

import android.location.Location;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lieruce.goforlunch.model.Restaurant;
import com.lieruce.goforlunch.model.User;
import com.lieruce.goforlunch.repository.AuthRepository;
import com.lieruce.goforlunch.repository.LocationRepository;
import com.lieruce.goforlunch.repository.MockRestaurantRepository;
import com.lieruce.goforlunch.repository.RestaurantRepository;
import com.lieruce.goforlunch.repository.UserRepository;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ViewModel for the Map and Restaurant List views.
 * Orchestrates location tracking, restaurant discovery, and real-time social data enrichment.
 */
public class MapsViewModel extends ViewModel {

    private final LocationRepository locationRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    
    private final MutableLiveData<Location> manualLocation = new MutableLiveData<>();
    private final MediatorLiveData<Location> locationToUse = new MediatorLiveData<>();
    private final MediatorLiveData<List<Restaurant>> nearbyRestaurants = new MediatorLiveData<>();
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MediatorLiveData<List<Restaurant>> filteredRestaurants = new MediatorLiveData<>();
    
    private boolean isInitialFetchDone = false;
    private ListenerRegistration socialListener;

    // Cache for social data (counts and likes) to prevent flickering
    private final Map<String, Integer> restaurantWorkmateCounts = new HashMap<>();
    private final Map<String, Integer> restaurantLikeCounts = new HashMap<>();

    public MapsViewModel(LocationRepository locationRepository, 
                         RestaurantRepository restaurantRepository,
                         UserRepository userRepository,
                         AuthRepository authRepository) {
        this.locationRepository = locationRepository;
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;

        // --- SIMULATION TRICK ---
        if (restaurantRepository instanceof MockRestaurantRepository) {
            Location louvre = new Location("mock");
            louvre.setLatitude(48.8591);
            louvre.setLongitude(2.3290);
            manualLocation.setValue(louvre);
        }

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

        // 1. Reactive Location Flow (Fetch ONCE)
        nearbyRestaurants.addSource(locationToUse, location -> {
            if (location != null && !isInitialFetchDone) {
                restaurantRepository.fetchNearbyRestaurants(location);
                isInitialFetchDone = true;
            }
        });

        // 2. Data Enrichment
        nearbyRestaurants.addSource(restaurantRepository.getNearbyRestaurantsLiveData(), restaurants -> {
            if (restaurants != null) {
                nearbyRestaurants.setValue(applyEnrichment(restaurants));
            }
        });

        // 4. Filtering
        filteredRestaurants.addSource(nearbyRestaurants, this::applyFilter);
        filteredRestaurants.addSource(searchQuery, query -> applyFilter(nearbyRestaurants.getValue()));

        // --- REACTIVE SYNC ---
        authRepository.getUserLiveData().observeForever(firebaseUser -> {
            if (firebaseUser != null) {
                startFirestoreListeners();
            } else {
                stopFirestoreListeners();
            }
        });
    }

    public void startFirestoreListeners() {
        if (socialListener != null) return;

        socialListener = userRepository.getAllUsers().addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("MapsViewModel", "Social sync listener failed: ", error);
                return;
            }
            if (value != null) {
                updateSocialData(value.toObjects(User.class));
            }
        });
    }

    private void stopFirestoreListeners() {
        if (socialListener != null) {
            socialListener.remove();
            socialListener = null;
        }
    }

    private void updateSocialData(List<User> users) {
        restaurantWorkmateCounts.clear();
        restaurantLikeCounts.clear();
        
        for (User user : users) {
            String rid = user.getChosenRestaurantId();
            if (rid != null && !rid.isEmpty()) {
                Integer count = restaurantWorkmateCounts.get(rid);
                restaurantWorkmateCounts.put(rid, (count != null ? count : 0) + 1);
            }
            
            List<String> liked = user.getLikedRestaurants();
            if (liked != null) {
                for (String likedId : liked) {
                    Integer lCount = restaurantLikeCounts.get(likedId);
                    restaurantLikeCounts.put(likedId, (lCount != null ? lCount : 0) + 1);
                }
            }
        }
        
        List<Restaurant> current = restaurantRepository.getNearbyRestaurantsLiveData().getValue();
        if (current != null) {
            nearbyRestaurants.setValue(applyEnrichment(current));
        }
    }

    private List<Restaurant> applyEnrichment(List<Restaurant> restaurants) {
        if (restaurants == null) return new ArrayList<>();
        List<Restaurant> enriched = new ArrayList<>();
        for (Restaurant r : restaurants) {
            Restaurant copy = new Restaurant(r.getId(), r.getName(), r.getAddress(), r.getRating(), r.getPhotoMetadatas(), r.getLatitude(), r.getLongitude(), r.getOpeningHours(), r.getPhoneNumber(), r.getWebsiteUrl(), r.getPhotoUrl());
            Integer wCount = restaurantWorkmateCounts.get(r.getId());
            copy.setWorkmatesCount(wCount != null ? wCount : 0);
            Integer likes = restaurantLikeCounts.get(r.getId());
            int stars = (likes == null || likes == 0) ? 0 : (likes <= 2 ? 1 : (likes <= 5 ? 2 : 3));
            copy.setStars(stars);
            enriched.add(copy);
        }
        return enriched;
    }

    private void applyFilter(List<Restaurant> restaurants) {
        String query = searchQuery.getValue();
        if (restaurants == null) {
            filteredRestaurants.setValue(null);
            return;
        }
        if (query == null || query.isEmpty()) {
            filteredRestaurants.setValue(new ArrayList<>(restaurants));
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

    public void setSearchQuery(String query) { searchQuery.setValue(query); }
    public LiveData<List<Restaurant>> getNearbyRestaurants() { return filteredRestaurants; }
    public void resetToCurrentLocation() {
        manualLocation.setValue(null);
        Location gps = locationRepository.getLocationLiveData().getValue();
        if (gps != null) locationToUse.setValue(gps);
    }
    public LiveData<Location> getLocationToUse() { return locationToUse; }

    public void refreshLocation() {
        locationRepository.startLocationUpdates();
        Location current = locationToUse.getValue();
        if (current != null) restaurantRepository.fetchNearbyRestaurants(current);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        stopFirestoreListeners();
    }
}
