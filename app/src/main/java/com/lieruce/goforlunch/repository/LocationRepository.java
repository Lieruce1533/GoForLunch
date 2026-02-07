package com.lieruce.goforlunch.repository;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class LocationRepository {

    private static volatile LocationRepository instance;
    private final FusedLocationProviderClient fusedLocationProviderClient;
    private final MutableLiveData<Location> locationLiveData = new MutableLiveData<>();

    private LocationRepository(Context context) {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public static LocationRepository getInstance(Context context) {
        if (instance == null) {
            synchronized (LocationRepository.class) {
                if (instance == null) {
                    instance = new LocationRepository(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    public LiveData<Location> getLocationLiveData() {
        return locationLiveData;
    }

    @SuppressLint("MissingPermission") // Permission is checked before calling this method
    public void startLocationUpdates() {
        // We use getCurrentLocation for a single, high-accuracy update.
        // This is battery efficient and prevents excessive API calls to Places.
        // Handle failure if necessary (e.g., log it)
        fusedLocationProviderClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        locationLiveData.setValue(location);
                    }
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }

    public void stopLocationUpdates() {
        // No-op: We are not listening to updates anymore.
        // Kept for interface compatibility if needed later.
    }
}
