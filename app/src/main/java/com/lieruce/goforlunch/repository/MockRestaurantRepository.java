package com.lieruce.goforlunch.repository;

import android.location.Location;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.lieruce.goforlunch.model.Restaurant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockRestaurantRepository implements RestaurantRepository {

    private final MutableLiveData<List<Restaurant>> nearbyRestaurantsLiveData = new MutableLiveData<>();
    private final Map<String, Restaurant> mockRestaurants = new HashMap<>();

    public MockRestaurantRepository() {
        initMockData();
    }

    private void initMockData() {
        addMock("mock_1", "Le Petit Gourmet", "12 Rue de la Paix, Paris", 4.5, 48.8690, 2.3310, "Open until 22:00", "01 23 45 67 89", "https://example.com/gourmet", "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=800");
        addMock("mock_2", "Pizzeria Napoli", "45 Avenue des Champs-Élysées, Paris", 4.2, 48.8710, 2.3020, "Open until 23:30", "01 98 76 54 32", "https://pizzeria-napoli.fr", "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=800");
        addMock("mock_3", "Sushi Master", "8 Boulevard Saint-Germain, Paris", 4.8, 48.8510, 2.3520, "Closed - Opens 11:30 Mon", "01 11 22 33 44", null, "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?w=800");
        addMock("mock_4", "The Burger Joint", "22 Rue de Rivoli, Paris", 3.9, 48.8560, 2.3530, "Open 24/7", "01 55 66 77 88", "https://burgerjoint.com", "https://images.unsplash.com/photo-1552566626-52f8b828add9?w=800");
        addMock("mock_5", "La Table d'Or", "5 Rue de Sevres, Paris", 4.9, 48.8520, 2.3220, "Open until 21:00", "01 44 33 22 11", "https://latable-dor.fr", "https://images.unsplash.com/photo-1514362545857-3bc16c4c7d1b?w=800");
        addMock("mock_6", "Bistro de la Mer", "14 Quai de la Tournelle, Paris", 4.3, 48.8500, 2.3550, "Open until 22:30", "01 77 88 99 00", null, "https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=800");
    }

    private void addMock(String id, String name, String addr, double rating, double lat, double lng, String hours, String phone, String web, String photo) {
        Restaurant r = new Restaurant(id, name, addr, rating, null, lat, lng, hours, phone, web, photo);
        mockRestaurants.put(id, r);
    }

    @Override
    public LiveData<List<Restaurant>> getNearbyRestaurantsLiveData() {
        return nearbyRestaurantsLiveData;
    }

    @Override
    public void fetchNearbyRestaurants(Location location) {
        // Return all mocks regardless of location for now
        nearbyRestaurantsLiveData.setValue(new ArrayList<>(mockRestaurants.values()));
    }

    @Override
    public Task<Restaurant> getRestaurantDetails(String placeId) {
        return Tasks.forResult(mockRestaurants.get(placeId));
    }
}
