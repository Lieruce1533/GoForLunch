package com.lieruce.goforlunch.ui.restaurants;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.lieruce.goforlunch.databinding.FragmentRestaurantsBinding;
import com.lieruce.goforlunch.viewmodel.MapsViewModel;
import com.lieruce.goforlunch.viewmodel.ViewModelFactory;

public class RestaurantsFragment extends Fragment {

    private FragmentRestaurantsBinding binding;
    private MapsViewModel mapsViewModel;
    private RestaurantAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRestaurantsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupViewModel();
        setupRecyclerView();
        observeData();
    }

    private void setupViewModel() {
        ViewModelFactory factory = ViewModelFactory.getInstance(requireContext());
        mapsViewModel = new ViewModelProvider(requireActivity(), factory).get(MapsViewModel.class);
    }

    private void setupRecyclerView() {
        adapter = new RestaurantAdapter();
        binding.restaurantRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.restaurantRecyclerView.setAdapter(adapter);
    }

    private void observeData() {
        // Observe user location to calculate distance
        mapsViewModel.getUserLocation().observe(getViewLifecycleOwner(), location -> {
            if (location != null) {
                adapter.setUserLocation(location);
            }
        });

        // Observe restaurant list
        mapsViewModel.getNearbyRestaurants().observe(getViewLifecycleOwner(), restaurants -> {
            if (restaurants != null) {
                adapter.setRestaurants(restaurants);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
