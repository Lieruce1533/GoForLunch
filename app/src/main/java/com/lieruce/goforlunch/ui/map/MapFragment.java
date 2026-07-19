package com.lieruce.goforlunch.ui.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.lieruce.goforlunch.R;
import com.lieruce.goforlunch.databinding.FragmentMapBinding;
import com.lieruce.goforlunch.model.Restaurant;
import com.lieruce.goforlunch.viewmodel.MapsViewModel;
import com.lieruce.goforlunch.viewmodel.ViewModelFactory;

import java.util.List;

public class MapFragment extends Fragment {

    private FragmentMapBinding binding;
    private MapsViewModel viewModel;
    private GoogleMap googleMap;
    private boolean isInitialLocationSet = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMapBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel using Activity scope so it's shared with RestaurantsFragment
        viewModel = new ViewModelProvider(requireActivity(), ViewModelFactory.getInstance(requireContext())).get(MapsViewModel.class);

        // Initialize Map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(map -> {
                googleMap = map;
                setupMap();
            });
        }
    }

    @SuppressLint("MissingPermission")
    private void setupMap() {
        if (googleMap == null) return;

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(false); // Using custom FAB instead
        }

        // Observe User Location (only for initial camera move)
        viewModel.getLocationToUse().observe(getViewLifecycleOwner(), location -> {
            if (location != null && !isInitialLocationSet) {
                LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 13f));
                isInitialLocationSet = true;
            }
        });

        // Observe Nearby Restaurants
        viewModel.getNearbyRestaurants().observe(getViewLifecycleOwner(), this::updateMapMarkers);

        // Handle Marker Clicks
        googleMap.setOnMarkerClickListener(marker -> {
            Restaurant restaurant = (Restaurant) marker.getTag();
            if (restaurant != null) {
                navigateToDetail(restaurant.getId());
            }
            return false;
        });

        // Handle Info Window Clicks (alternative way to reach details)
        googleMap.setOnInfoWindowClickListener(marker -> {
            Restaurant restaurant = (Restaurant) marker.getTag();
            if (restaurant != null) {
                navigateToDetail(restaurant.getId());
            }
        });

        // Manual Location Selection logic
        setupManualLocationListeners();
    }

    private void setupManualLocationListeners() {
        googleMap.setOnCameraMoveStartedListener(reason -> {
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                binding.btnSearchArea.setVisibility(View.VISIBLE);
            }
        });

        binding.btnSearchArea.setOnClickListener(v -> {
            LatLng center = googleMap.getCameraPosition().target;
            android.location.Location location = new android.location.Location("manual");
            location.setLatitude(center.latitude);
            location.setLongitude(center.longitude);
            viewModel.setManualLocation(location);
            binding.btnSearchArea.setVisibility(View.GONE);
        });

        binding.fabMyLocation.setOnClickListener(v -> {
            viewModel.resetToCurrentLocation();
            isInitialLocationSet = false; // Allow re-centering once
            binding.btnSearchArea.setVisibility(View.GONE);
        });
    }

    private void updateMapMarkers(List<Restaurant> restaurants) {
        if (googleMap == null || restaurants == null) return;

        googleMap.clear();
        for (Restaurant restaurant : restaurants) {
            LatLng position = new LatLng(restaurant.getLatitude(), restaurant.getLongitude());
            
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(position)
                    .title(restaurant.getName());

            // --- COLOR LOGIC ---
            if (restaurant.getWorkmatesCount() > 0) {
                // Someone is eating here!
                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            }

            Marker marker = googleMap.addMarker(markerOptions);
            
            if (marker != null) {
                marker.setTag(restaurant); // Store the restaurant object in the marker
            }
        }
    }

    private void navigateToDetail(String restaurantId) {
        Bundle args = new Bundle();
        args.putString("restaurantId", restaurantId);
        Navigation.findNavController(requireView()).navigate(R.id.action_navigation_map_to_restaurantDetailFragment, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
