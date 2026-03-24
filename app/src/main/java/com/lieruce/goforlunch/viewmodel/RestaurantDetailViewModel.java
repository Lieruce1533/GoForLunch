package com.lieruce.goforlunch.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lieruce.goforlunch.model.Restaurant;
import com.lieruce.goforlunch.model.User;
import com.lieruce.goforlunch.repository.RestaurantRepository;
import com.lieruce.goforlunch.repository.UserRepository;

import java.util.List;

public class RestaurantDetailViewModel extends ViewModel {

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final MutableLiveData<Restaurant> restaurantLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<User>> workmatesLiveData = new MutableLiveData<>();

    public RestaurantDetailViewModel(RestaurantRepository restaurantRepository, UserRepository userRepository) {
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;
    }

    public void setRestaurantId(String restaurantId) {
        // Find the restaurant in the current list
        List<Restaurant> currentRestaurants = restaurantRepository.getNearbyRestaurantsLiveData().getValue();
        if (currentRestaurants != null) {
            for (Restaurant r : currentRestaurants) {
                if (r.getId().equals(restaurantId)) {
                    restaurantLiveData.setValue(r);
                    break;
                }
            }
        }

        // Listen for workmates going to this restaurant
        userRepository.getUsersEatingAt(restaurantId).addSnapshotListener((value, error) -> {
            if (error == null && value != null) {
                workmatesLiveData.setValue(value.toObjects(User.class));
            }
        });
    }

    public LiveData<Restaurant> getRestaurant() {
        return restaurantLiveData;
    }

    public LiveData<List<User>> getWorkmates() {
        return workmatesLiveData;
    }

    // TODO: Implement toggleSelection() and toggleLike()
}
