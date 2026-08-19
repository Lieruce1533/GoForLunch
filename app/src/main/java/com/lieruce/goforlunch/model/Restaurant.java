package com.lieruce.goforlunch.model;

import com.google.android.libraries.places.api.model.PhotoMetadata;

import java.util.List;

/**
 * Data Model representing a Restaurant establishment.
 * Consolidates static data from Google Places with dynamic social data (attendance, stars) from Firestore.
 */
public class Restaurant {

    private final String id;
    private final String name;
    private final String address;
    private final double rating;
    private final List<PhotoMetadata> photoMetadata;
    private final double latitude;
    private final double longitude;
    private final String openingHours;
    private final String phoneNumber;
    private final String websiteUrl;
    private final String photoUrl;
    private int workmatesCount;
    private int stars;

    public Restaurant(String id, String name, String address, double rating, List<PhotoMetadata> photoMetadata, double latitude, double longitude, String openingHours, String phoneNumber, String websiteUrl, String photoUrl) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.rating = rating;
        this.photoMetadata = photoMetadata;
        this.latitude = latitude;
        this.longitude = longitude;
        this.openingHours = openingHours;
        this.phoneNumber = phoneNumber;
        this.websiteUrl = websiteUrl;
        this.photoUrl = photoUrl;
        this.workmatesCount = 0;
    }

    // --- GETTERS ---
    public String getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public double getRating() { return rating; }
    public List<PhotoMetadata> getPhotoMetadatas() { return photoMetadata; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getOpeningHours() { return openingHours; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getWebsiteUrl() { return websiteUrl; }
    public String getPhotoUrl() { return photoUrl; }
    public int getWorkmatesCount() { return workmatesCount; }
    public int getStars() { return stars; }

    // --- SETTERS ---
    public void setWorkmatesCount(int workmatesCount) {
        this.workmatesCount = workmatesCount;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }
}
