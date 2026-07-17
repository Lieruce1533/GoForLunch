package com.lieruce.goforlunch.repository;

import android.location.Location;
import androidx.lifecycle.LiveData;
import com.google.android.gms.tasks.Task;
import com.lieruce.goforlunch.model.Restaurant;
import java.util.List;

public interface RestaurantRepository {
    LiveData<List<Restaurant>> getNearbyRestaurantsLiveData();
    void fetchNearbyRestaurants(Location location);
    Task<Restaurant> getRestaurantDetails(String placeId);
}
