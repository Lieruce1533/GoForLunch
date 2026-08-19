package com.lieruce.goforlunch.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.lieruce.goforlunch.model.Restaurant;
import com.lieruce.goforlunch.model.User;
import com.lieruce.goforlunch.repository.AuthRepository;
import com.lieruce.goforlunch.repository.RestaurantRepository;
import com.lieruce.goforlunch.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for the Restaurant Detail screen.
 * Aggregates on-demand place details with user-specific states (isLiked, isSelected).
 */
public class RestaurantDetailViewModel extends ViewModel {

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final AuthRepository authRepository;
    
    private final MutableLiveData<Restaurant> restaurantLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<User>> workmatesLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isRestaurantSelected = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isRestaurantLiked = new MutableLiveData<>();
    private final MutableLiveData<Integer> socialStars = new MutableLiveData<>(0);

    public RestaurantDetailViewModel(RestaurantRepository restaurantRepository, 
                                   UserRepository userRepository,
                                   AuthRepository authRepository) {
        this.restaurantRepository = restaurantRepository;
        this.userRepository = userRepository;
        this.authRepository = authRepository;
    }

    public void setRestaurantId(String restaurantId) {
        // 1. Fetch full restaurant details on-demand (expensive fields like phone/website)
        restaurantRepository.getRestaurantDetails(restaurantId).addOnSuccessListener(restaurant -> {
            restaurantLiveData.setValue(restaurant);
        });

        // 2. Fetch current user's data to check selection and likes
        FirebaseUser currentUser = authRepository.getCurrentUser();
        if (currentUser != null) {
            userRepository.getUserData(currentUser.getUid()).addOnSuccessListener(documentSnapshot -> {
                User user = documentSnapshot.toObject(User.class);
                if (user != null) {
                    // Check if this restaurant is currently selected
                    isRestaurantSelected.setValue(restaurantId.equals(user.getChosenRestaurantId()));
                    
                    // Check if this restaurant is in the liked list
                    List<String> liked = user.getLikedRestaurants();
                    isRestaurantLiked.setValue(liked != null && liked.contains(restaurantId));
                }
            });
        }

        // 3. Listen for workmates going to this restaurant
        userRepository.getUsersEatingAt(restaurantId).addSnapshotListener((value, error) -> {
            if (error == null && value != null) {
                workmatesLiveData.setValue(value.toObjects(User.class));
            }
        });

        // 4. Listen for total likes to calculate social stars (0-3)
        userRepository.getLikesForRestaurant(restaurantId).addSnapshotListener((value, error) -> {
            if (error == null && value != null) {
                int likes = value.size();
                int stars;
                if (likes == 0) stars = 0;
                else if (likes <= 2) stars = 1;
                else if (likes <= 5) stars = 2;
                else stars = 3;
                socialStars.setValue(stars);
            }
        });
    }

    // --- ACTIONS ---

    public void toggleSelection() {
        FirebaseUser currentUser = authRepository.getCurrentUser();
        Restaurant restaurant = restaurantLiveData.getValue();
        Boolean currentlySelected = isRestaurantSelected.getValue();

        if (currentUser != null && restaurant != null) {
            boolean newValue = (currentlySelected == null) || !currentlySelected;
            if (!newValue) {
                // Deselect: Clear the choice
                userRepository.updateChosenRestaurant(currentUser.getUid(), null, null, null);
            } else {
                // Select: Set the choice
                userRepository.updateChosenRestaurant(currentUser.getUid(), restaurant.getId(), restaurant.getName(), restaurant.getAddress());
            }
            isRestaurantSelected.setValue(newValue);
        }
    }

    public void toggleLike() {
        FirebaseUser currentUser = authRepository.getCurrentUser();
        Restaurant restaurant = restaurantLiveData.getValue();
        Boolean currentlyLiked = isRestaurantLiked.getValue();

        if (currentUser != null && restaurant != null) {
            boolean newLikeStatus = (currentlyLiked == null) || !currentlyLiked;
            userRepository.updateLikedRestaurant(currentUser.getUid(), restaurant.getId(), newLikeStatus);
            isRestaurantLiked.setValue(newLikeStatus);
        }
    }

    // --- GETTERS ---
    public LiveData<Restaurant> getRestaurant() { return restaurantLiveData; }
    public LiveData<List<User>> getWorkmates() { return workmatesLiveData; }
    public LiveData<Boolean> getIsRestaurantSelected() { return isRestaurantSelected; }
    public LiveData<Boolean> getIsRestaurantLiked() { return isRestaurantLiked; }
    public LiveData<Integer> getSocialStars() { return socialStars; }
}
