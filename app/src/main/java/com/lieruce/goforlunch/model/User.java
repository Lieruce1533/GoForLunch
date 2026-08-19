package com.lieruce.goforlunch.model;

import java.util.List;

/**
 * Data Model representing a User in the application and Firestore.
 * Contains profile information and social choice metadata.
 */
public class User {

    private String uid;
    private String username;
    private String avatarUrl;
    private String chosenRestaurantId;
    private String chosenRestaurantName;
    private String chosenRestaurantAddress;
    private List<String> likedRestaurants;

    // Firestore requires a public no-argument constructor
    public User() { }

    public User(String uid, String username, String avatarUrl) {
        this.uid = uid;
        this.username = username;
        this.avatarUrl = avatarUrl;
    }

    // --- GETTERS ---
    public String getUid() { return uid; }
    public String getUsername() { return username; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getChosenRestaurantId() { return chosenRestaurantId; }
    public String getChosenRestaurantName() { return chosenRestaurantName; }
    public String getChosenRestaurantAddress() { return chosenRestaurantAddress; }
    public List<String> getLikedRestaurants() { return likedRestaurants; }

    // --- SETTERS ---
    public void setUid(String uid) { this.uid = uid; }
    public void setUsername(String username) { this.username = username; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public void setChosenRestaurantId(String chosenRestaurantId) { this.chosenRestaurantId = chosenRestaurantId; }
    public void setChosenRestaurantName(String chosenRestaurantName) { this.chosenRestaurantName = chosenRestaurantName; }
    public void setChosenRestaurantAddress(String chosenRestaurantAddress) { this.chosenRestaurantAddress = chosenRestaurantAddress; }
    public void setLikedRestaurants(List<String> likedRestaurants) { this.likedRestaurants = likedRestaurants; }
}
