package com.lieruce.goforlunch.repository;

import android.content.Context;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AuthRepository {

    private final FirebaseAuth firebaseAuth;

    // Singleton pattern
    private static volatile AuthRepository instance;

    private AuthRepository() {
        this.firebaseAuth = FirebaseAuth.getInstance();
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

    public Task<Void> signOut(Context context) {
        return AuthUI.getInstance().signOut(context);
    }
}
