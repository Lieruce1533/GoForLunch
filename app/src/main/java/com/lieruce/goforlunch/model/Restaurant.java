package com.lieruce.goforlunch.model;

import com.google.android.libraries.places.api.model.PhotoMetadata;

import java.util.List;

public class Restaurant {

    private final String id;
    private final String name;
    private final String address;
    private final double rating;
    private final List<PhotoMetadata> photoMetadatas;
    private final double latitude;
    private final double longitude;

    public Restaurant(String id, String name, String address, double rating, List<PhotoMetadata> photoMetadatas, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.rating = rating;
        this.photoMetadatas = photoMetadatas;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // --- GETTERS ---
    public String getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public double getRating() { return rating; }
    public List<PhotoMetadata> getPhotoMetadatas() { return photoMetadatas; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
}
