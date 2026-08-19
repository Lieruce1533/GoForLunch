package com.lieruce.goforlunch.repository;

import android.location.Location;
import androidx.lifecycle.LiveData;
import com.google.android.gms.tasks.Task;
import com.lieruce.goforlunch.model.Restaurant;
import java.util.List;

/**
 * Core interface defining the capabilities for restaurant data retrieval.
 * Allows the app to swap between Real-world (Google) and Presentation (Mock) implementations seamlessly.
 */
public interface RestaurantRepository {
    LiveData<List<Restaurant>> getNearbyRestaurantsLiveData();
    void fetchNearbyRestaurants(Location location);
    Task<Restaurant> getRestaurantDetails(String placeId);
}
