package com.lieruce.goforlunch.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Repository handling all Firebase Authentication logic.
 * Abstracts the FirebaseAuth SDK and provides a singleton access point for user state.
 */
public class AuthRepository {

    private final FirebaseAuth firebaseAuth;
    private final MutableLiveData<FirebaseUser> userLiveData = new MutableLiveData<>();

    // Singleton pattern
    private static volatile AuthRepository instance;

    private AuthRepository() {
        this.firebaseAuth = FirebaseAuth.getInstance();
        this.userLiveData.setValue(firebaseAuth.getCurrentUser());
        
        // Listener to keep LiveData in sync
        this.firebaseAuth.addAuthStateListener(auth -> {
            userLiveData.setValue(auth.getCurrentUser());
        });
    }

    public static AuthRepository getInstance() {
        if (instance == null) {
            synchronized(AuthRepository.class) {
                if (instance == null) {
                    instance = new AuthRepository();
                }
            }
        }
        return instance;
    }

    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public LiveData<FirebaseUser> getUserLiveData() {
        return userLiveData;
    }

    public void refreshUser() {
        userLiveData.setValue(firebaseAuth.getCurrentUser());
    }

    public Task<Void> signOut(Context context) {
        return AuthUI.getInstance().signOut(context);
    }
}
