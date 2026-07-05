package com.lieruce.goforlunch.repository;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class LocationRepository {

    private static volatile LocationRepository instance;
    private final FusedLocationProviderClient fusedLocationProviderClient;
    private final MutableLiveData<Location> locationLiveData = new MutableLiveData<>();
    private LocationCallback locationCallback;

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

    @SuppressLint("MissingPermission")
    public void startLocationUpdates() {
        if (locationCallback != null) return; // Already listening

        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setMinUpdateIntervalMillis(5000)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        locationLiveData.setValue(location);
                    }
                }
            }
        };

        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    public void stopLocationUpdates() {
        if (locationCallback != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            locationCallback = null;
        }
    }
}
