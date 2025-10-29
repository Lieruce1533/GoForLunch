package com.lieruce.goforlunch.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainViewModel extends ViewModel {

    private final MutableLiveData<FirebaseUser> userLiveData = new MutableLiveData<>();

    public MainViewModel() {
        // Check the initial state when the ViewModel is created
        refreshUser();
    }

    public LiveData<FirebaseUser> getUserLiveData() {
        return userLiveData;
    }

    // Call this method to re-check the current user status
    public void refreshUser() {
        userLiveData.setValue(FirebaseAuth.getInstance().getCurrentUser());
    }
}
