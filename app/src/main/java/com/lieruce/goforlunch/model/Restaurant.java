package com.lieruce.goforlunch.model;

public class Restaurant {

    private final String id;
    private final String name;
    private final String address;
    private final double rating;
    private final String photoUrl;
    private final double latitude;
    private final double longitude;

    public Restaurant(String id, String name, String address, double rating, String photoUrl, double latitude, double longitude) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.rating = rating;
        this.photoUrl = photoUrl;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // --- GETTERS ---
    public String getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public double getRating() { return rating; }
    public String getPhotoUrl() { return photoUrl; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
}
