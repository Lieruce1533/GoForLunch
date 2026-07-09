package com.lieruce.goforlunch.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lieruce.goforlunch.model.User;
import com.lieruce.goforlunch.repository.UserRepository;

import java.util.Collections;
import java.util.List;

public class WorkmatesViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final MutableLiveData<List<User>> workmatesLiveData = new MutableLiveData<>();

    public WorkmatesViewModel(UserRepository userRepository) {
        this.userRepository = userRepository;
        loadWorkmates();
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
                workmatesLiveData.setValue(users);
            }
        });
    }

    public LiveData<List<User>> getWorkmates() {
        return workmatesLiveData;
    }
}
