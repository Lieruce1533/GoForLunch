package com.lieruce.goforlunch.viewmodel;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.lieruce.goforlunch.repository.AuthRepository;
import com.lieruce.goforlunch.repository.UserRepository;

public class MainViewModel extends ViewModel {

    private final MutableLiveData<FirebaseUser> userLiveData = new MutableLiveData<>();
    private final AuthRepository authRepository;
    private final UserRepository userRepository;

    public MainViewModel(AuthRepository authRepository, UserRepository userRepository) {
        this.authRepository = authRepository;
        this.userRepository = userRepository;
        refreshUser();
    }

    public LiveData<FirebaseUser> getUserLiveData() {
        return userLiveData;
    }

    public void refreshUser() {
        userLiveData.setValue(authRepository.getCurrentUser());
    }

    public Task<Void> signOut(Context context) {
        return authRepository.signOut(context);
    }

    public Task<Void> createUser() {
        return userRepository.createUser(authRepository.getCurrentUser());
    }

    public Task<DocumentSnapshot> getCurrentUserData() {
        FirebaseUser user = authRepository.getCurrentUser();
        if (user != null) {
            return userRepository.getUserData(user.getUid());
        }
        return null;
    }
}
