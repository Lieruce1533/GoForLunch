package com.lieruce.goforlunch.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lieruce.goforlunch.model.User;
import com.lieruce.goforlunch.repository.UserRepository;

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
                workmatesLiveData.setValue(value.toObjects(User.class));
            }
        });
    }

    public LiveData<List<User>> getWorkmates() {
        return workmatesLiveData;
    }
}
