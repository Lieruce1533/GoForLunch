package com.lieruce.goforlunch.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.lieruce.goforlunch.model.User;

public class UserRepository {

    private static final String COLLECTION_NAME = "users";
    private static volatile UserRepository instance;
    private final CollectionReference usersCollection;

    private UserRepository() {
        this.usersCollection = FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    public static UserRepository getInstance() {
        if (instance == null) {
            synchronized(UserRepository.class) {
                if (instance == null) {
                    instance = new UserRepository();
                }
            }
        }
        return instance;
    }

    public Task<Void> createUser(FirebaseUser firebaseUser) {
        String uid = firebaseUser.getUid();
        String username = firebaseUser.getDisplayName();
        String avatarUrl = (firebaseUser.getPhotoUrl() != null) ? firebaseUser.getPhotoUrl().toString() : null;

        User userToCreate = new User(uid, username, avatarUrl);

        return usersCollection.document(uid).set(userToCreate);
    }
}
