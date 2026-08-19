package com.lieruce.goforlunch.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lieruce.goforlunch.model.User;
import com.lieruce.goforlunch.repository.AuthRepository;
import com.lieruce.goforlunch.repository.UserRepository;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for the global Workmates list.
 * Transforms raw Firestore user data into a sorted and filtered list based on restaurant choices.
 */
public class WorkmatesViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final MutableLiveData<List<User>> rawWorkmates = new MutableLiveData<>();
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MediatorLiveData<List<User>> filteredWorkmates = new MediatorLiveData<>();
    
    private ListenerRegistration workmatesListener;

    public WorkmatesViewModel(UserRepository userRepository, AuthRepository authRepository) {
        this.userRepository = userRepository;
        
        // Setup filtering
        filteredWorkmates.addSource(rawWorkmates, this::applyFilter);
        filteredWorkmates.addSource(searchQuery, query -> applyFilter(rawWorkmates.getValue()));

        // REACTIVE SYNC: Start listening automatically when authenticated
        authRepository.getUserLiveData().observeForever(firebaseUser -> {
            if (firebaseUser != null) {
                startFirestoreListeners();
            } else {
                stopFirestoreListeners();
            }
        });
    }

    public void startFirestoreListeners() {
        if (workmatesListener != null) return; 

        workmatesListener = userRepository.getAllUsers().addSnapshotListener((value, error) -> {
            if (error != null) {
                android.util.Log.e("WorkmatesViewModel", "Workmates listener failed: ", error);
                return;
            }
            if (value != null) {
                rawWorkmates.setValue(value.toObjects(User.class));
            }
        });
    }

    private void stopFirestoreListeners() {
        if (workmatesListener != null) {
            workmatesListener.remove();
            workmatesListener = null;
        }
    }

    private void applyFilter(List<User> users) {
        String query = searchQuery.getValue();
        if (users == null) {
            filteredWorkmates.setValue(null);
            return;
        }
        if (query == null || query.isEmpty()) {
            filteredWorkmates.setValue(new ArrayList<>(users));
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

    public void setSearchQuery(String query) { searchQuery.setValue(query); }
    public LiveData<List<User>> getWorkmates() { return filteredWorkmates; }

    @Override
    protected void onCleared() {
        super.onCleared();
        stopFirestoreListeners();
    }
}
