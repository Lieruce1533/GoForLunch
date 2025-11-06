package com.lieruce.goforlunch.viewmodel;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.lieruce.goforlunch.repository.AuthRepository;

public class MainViewModel extends ViewModel {

    private final MutableLiveData<FirebaseUser> userLiveData = new MutableLiveData<>();
    private final AuthRepository authRepository;

    public MainViewModel(AuthRepository authRepository) {
        this.authRepository = authRepository;
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
}
