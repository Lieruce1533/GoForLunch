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

import com.lieruce.goforlunch.model.User;

import java.util.HashMap;
import java.util.Map;

public class UserRepository {

    private static final String COLLECTION_NAME = "users";
    private static volatile UserRepository instance;
    private final CollectionReference usersCollection;

    // Visible for testing - allows injecting a mock CollectionReference
    public UserRepository(CollectionReference usersCollection) {
        this.usersCollection = usersCollection;
    }

    public static UserRepository getInstance() {
        if (instance == null) {
            synchronized(UserRepository.class) {
                if (instance == null) {
                    instance = new UserRepository(FirebaseFirestore.getInstance().collection(COLLECTION_NAME));
                }
            }
        }
        return instance;
    }

    // --- CREATE ---
    public Task<Void> createUser(FirebaseUser firebaseUser) {
        if (firebaseUser == null) return null;

        String uid = firebaseUser.getUid();
        String name = firebaseUser.getDisplayName();
        
        // Fallback: If no display name, use the part of email before '@'
        if ((name == null || name.isEmpty()) && firebaseUser.getEmail() != null) {
            name = firebaseUser.getEmail().split("@")[0];
        }
        
        if (name == null || name.isEmpty()) {
            name = "Anonymous";
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", uid);
        userData.put("username", name);
        userData.put("avatarUrl", (firebaseUser.getPhotoUrl() != null) ? firebaseUser.getPhotoUrl().toString() : null);

        // We use merge() so we don't overwrite chosenRestaurantId or likedRestaurants
        return usersCollection.document(uid).set(userData, SetOptions.merge());
    }

    // --- GET ---
    public Task<DocumentSnapshot> getUserData(String uid) {
        return usersCollection.document(uid).get();
    }

    public Query getUsersEatingAt(String restaurantId) {
        return usersCollection.whereEqualTo("chosenRestaurantId", restaurantId);
    }

    public Query getAllUsers() {
        return usersCollection;
    }

    // --- UPDATE ---
    
    // Updates the restaurant the user has chosen for today
    public Task<Void> updateChosenRestaurant(String uid, String restaurantId, String restaurantName, String restaurantAddress) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("chosenRestaurantId", restaurantId);
        updates.put("chosenRestaurantName", restaurantName);
        updates.put("chosenRestaurantAddress", restaurantAddress);
        
        return usersCollection.document(uid).set(updates, SetOptions.merge());
    }

    // Toggle liking a restaurant
    public Task<Void> updateLikedRestaurant(String uid, String restaurantId, boolean isLiked) {
        Map<String, Object> updates = new HashMap<>();
        if (isLiked) {
            updates.put("likedRestaurants", FieldValue.arrayUnion(restaurantId));
        } else {
            updates.put("likedRestaurants", FieldValue.arrayRemove(restaurantId));
        }
        return usersCollection.document(uid).set(updates, SetOptions.merge());
    }
}
