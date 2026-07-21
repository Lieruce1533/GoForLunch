package com.lieruce.goforlunch.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lieruce.goforlunch.model.User;
import com.lieruce.goforlunch.repository.UserRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WorkmatesViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final MutableLiveData<List<User>> rawWorkmates = new MutableLiveData<>();
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MediatorLiveData<List<User>> filteredWorkmates = new MediatorLiveData<>();

    public WorkmatesViewModel(UserRepository userRepository) {
        this.userRepository = userRepository;
        loadWorkmates();
        
        // Setup filtering
        filteredWorkmates.addSource(rawWorkmates, this::applyFilter);
        filteredWorkmates.addSource(searchQuery, query -> applyFilter(rawWorkmates.getValue()));
    }

    private void loadWorkmates() {
        // Use a SnapshotListener for real-time updates
        userRepository.getAllUsers().addSnapshotListener((value, error) -> {
            if (error == null && value != null) {
                List<User> users = value.toObjects(User.class);
                // Sort locally for API 23 compatibility
                Collections.sort(users, (u1, u2) -> {
                    String n1 = u1.getUsername() != null ? u1.getUsername() : "";
                    String n2 = u2.getUsername() != null ? u2.getUsername() : "";
                    return n1.compareToIgnoreCase(n2);
                });
                rawWorkmates.setValue(users);
            }
        });
    }

    private void applyFilter(List<User> users) {
        String query = searchQuery.getValue();
        if (users == null) {
            filteredWorkmates.setValue(null);
            return;
        }
        if (query == null || query.isEmpty()) {
            filteredWorkmates.setValue(users);
            return;
        }

        List<User> filtered = new ArrayList<>();
        for (User user : users) {
            String restaurantName = user.getChosenRestaurantName();
            if (restaurantName != null && restaurantName.toLowerCase().contains(query.toLowerCase())) {
                filtered.add(user);
            }
        }
        filteredWorkmates.setValue(filtered);
    }

    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    public LiveData<List<User>> getWorkmates() {
        return filteredWorkmates;
    }
}
