package com.lieruce.goforlunch.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.SetOptions;
import com.lieruce.goforlunch.model.User;

import java.util.HashMap;
import java.util.Map;

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

    // --- CREATE ---
    public Task<Void> createUser(FirebaseUser firebaseUser) {
        String uid = firebaseUser.getUid();
        String username = firebaseUser.getDisplayName();
        String avatarUrl = (firebaseUser.getPhotoUrl() != null) ? firebaseUser.getPhotoUrl().toString() : null;

        User userToCreate = new User(uid, username, avatarUrl);

        return usersCollection.document(uid).set(userToCreate, SetOptions.merge());
    }

    // --- GET ---
    public Task<DocumentSnapshot> getUserData(String uid) {
        return usersCollection.document(uid).get();
    }

    public Query getUsersEatingAt(String restaurantId) {
        return usersCollection.whereEqualTo("chosenRestaurantId", restaurantId);
    }

    public Query getAllUsers() {
        return usersCollection.orderBy("username", Query.Direction.ASCENDING);
    }

    // --- UPDATE ---
    
    // Updates the restaurant the user has chosen for today
    public Task<Void> updateChosenRestaurant(String uid, String restaurantId, String restaurantName) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("chosenRestaurantId", restaurantId);
        updates.put("chosenRestaurantName", restaurantName);
        
        return usersCollection.document(uid).update(updates);
    }

    // Toggle liking a restaurant
    public Task<Void> updateLikedRestaurant(String uid, String restaurantId, boolean isLiked) {
        if (isLiked) {
            return usersCollection.document(uid).update("likedRestaurants", FieldValue.arrayUnion(restaurantId));
        } else {
            return usersCollection.document(uid).update("likedRestaurants", FieldValue.arrayRemove(restaurantId));
        }
    }
}
