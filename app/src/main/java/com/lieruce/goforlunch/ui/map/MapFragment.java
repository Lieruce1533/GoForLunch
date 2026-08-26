package com.lieruce.goforlunch.ui.map;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
import com.google.android.gms.maps.model.BitmapDescriptor;
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

/**
 * Fragment responsible for displaying the interactive Google Maps.
 * Provides features like custom-branded markers, manual area searching, and location resetting.
 */
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

        // Observe User Location (only for initial camera move)
        viewModel.getLocationToUse().observe(getViewLifecycleOwner(), location -> {
            if (location != null) {
                // Enable standard blue dot only for real GPS locations
                boolean isRealGps = !"mock".equals(location.getProvider());
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    googleMap.setMyLocationEnabled(isRealGps);
                    googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                }

                if (!isInitialLocationSet) {
                    LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 13f));
                    isInitialLocationSet = true;
                }
                
                // If it's a mock location, we need to manually draw the user marker 
                // because setMyLocationEnabled(true) won't show it at the mock coordinates.
                if (!isRealGps) {
                    updateMapMarkers(viewModel.getNearbyRestaurants().getValue());
                }
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
        binding.fabMyLocation.setOnClickListener(v -> {
            viewModel.resetToCurrentLocation();
            isInitialLocationSet = false; // Allow re-centering once
        });
    }

    private void updateMapMarkers(List<Restaurant> restaurants) {
        if (googleMap == null) return;

        googleMap.clear();

        // 1. Add User Marker if in Mock Mode
        android.location.Location currentLocation = viewModel.getLocationToUse().getValue();
        if (currentLocation != null && "mock".equals(currentLocation.getProvider())) {
            LatLng userPos = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            int azureColor = Color.parseColor("#007FFF");
            googleMap.addMarker(new MarkerOptions()
                    .position(userPos)
                    .title(getString(R.string.me))
                    .zIndex(1.0f) // Keep user on top
                    .icon(getBitmapDescriptorFromVector(azureColor, R.drawable.ic_workmates, 1.2f)));
        }

        // 2. Add Restaurant Markers
        if (restaurants == null) return;
        for (Restaurant restaurant : restaurants) {
            LatLng position = new LatLng(restaurant.getLatitude(), restaurant.getLongitude());
            
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(position)
                    .title(restaurant.getName());

            // --- COLOR LOGIC ---
            int markerColor;
            if (restaurant.getWorkmatesCount() > 0) {
                markerColor = ContextCompat.getColor(requireContext(), R.color.colorMarkerGreen);
            } else {
                markerColor = ContextCompat.getColor(requireContext(), R.color.colorMarkerRed);
            }
            
            markerOptions.icon(getBitmapDescriptorFromVector(markerColor, R.drawable.ic_default_restaurant, 2.0f));

            Marker marker = googleMap.addMarker(markerOptions);
            
            if (marker != null) {
                marker.setTag(restaurant);
            }
        }
    }

    private void navigateToDetail(String restaurantId) {
        Bundle args = new Bundle();
        args.putString("restaurantId", restaurantId);
        Navigation.findNavController(requireView()).navigate(R.id.action_navigation_map_to_restaurantDetailFragment, args);
    }

    /**
     * Dynamically generates a custom Map Marker bitmap.
     * Layers a tinted Pin shape with a darker-tinted icon for a professional "stamped" look.
     * @param color The background color of the pin.
     * @param iconResId The resource ID of the icon to draw inside.
     * @param scale Overall size multiplier.
     * @return A BitmapDescriptor ready for use on the map.
     */
    private BitmapDescriptor getBitmapDescriptorFromVector(int color, int iconResId, float scale) {
        Drawable background = ContextCompat.getDrawable(requireContext(), R.drawable.ic_map_pin_shape);
        Drawable icon = ContextCompat.getDrawable(requireContext(), iconResId);
        
        if (background == null || icon == null) return BitmapDescriptorFactory.defaultMarker();

        // 1. Prepare background (the pin)
        background.setTint(color);
        int width = (int) (background.getIntrinsicWidth() * scale);
        int height = (int) (background.getIntrinsicHeight() * scale);
        background.setBounds(0, 0, width, height);

        // 2. Prepare icon (e.g., fork & knife or person)
        icon.setTint(darkenColor(color, 0.7f)); // 30% darker than background
        
        // Center it in the pin head (top part)
        int iconSize = (int) (width * 0.5);
        int left = (width - iconSize) / 2;
        int top = (int) (height * 0.15); // Slightly down from top
        icon.setBounds(left, top, left + iconSize, top + iconSize);

        // 3. Create bitmap and draw
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        background.draw(canvas);
        icon.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private int darkenColor(int color, float factor) {
        int a = Color.alpha(color);
        int r = Math.round(Color.red(color) * factor);
        int g = Math.round(Color.green(color) * factor);
        int b = Math.round(Color.blue(color) * factor);
        return Color.argb(a,
                Math.min(r, 255),
                Math.min(g, 255),
                Math.min(b, 255));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
