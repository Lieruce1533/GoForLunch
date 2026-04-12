package com.lieruce.goforlunch.ui.map;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
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

    @SuppressLint("MissingPermission") // Permission checked in MainActivity
    private void setupMap() {
        if (googleMap == null) return;

        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);

        // Observe User Location
        viewModel.getUserLocation().observe(getViewLifecycleOwner(), location -> {
            if (location != null) {
                LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f));
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
    }

    private void updateMapMarkers(List<Restaurant> restaurants) {
        if (googleMap == null || restaurants == null) return;

        googleMap.clear();
        for (Restaurant restaurant : restaurants) {
            LatLng position = new LatLng(restaurant.getLatitude(), restaurant.getLongitude());
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(restaurant.getName()));
            
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
