package com.lieruce.goforlunch.ui.restaurants;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.lieruce.goforlunch.R;
import com.lieruce.goforlunch.databinding.FragmentRestaurantsBinding;
import com.lieruce.goforlunch.model.Restaurant;
import com.lieruce.goforlunch.viewmodel.MapsViewModel;
import com.lieruce.goforlunch.viewmodel.ViewModelFactory;

public class RestaurantsFragment extends Fragment implements RestaurantAdapter.OnRestaurantClickListener {

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
        adapter = new RestaurantAdapter(this);
        binding.restaurantRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.restaurantRecyclerView.setAdapter(adapter);

        binding.swipeRefreshLayout.setOnRefreshListener(() -> {
            mapsViewModel.refreshLocation();
        });
    }

    private void observeData() {
        // Show loading initially
        if (mapsViewModel.getNearbyRestaurants().getValue() == null) {
            binding.loadingIndicator.setVisibility(View.VISIBLE);
        }

        // Observe user location to calculate distance
        mapsViewModel.getUserLocation().observe(getViewLifecycleOwner(), location -> {
            if (location != null) {
                adapter.setUserLocation(location);
            }
        });

        // Observe restaurant list
        mapsViewModel.getNearbyRestaurants().observe(getViewLifecycleOwner(), restaurants -> {
            binding.loadingIndicator.setVisibility(View.GONE);
            binding.swipeRefreshLayout.setRefreshing(false);
            if (restaurants != null) {
                adapter.setRestaurants(restaurants);
                if (restaurants.isEmpty()) {
                    binding.emptyView.setVisibility(View.VISIBLE);
                    binding.restaurantRecyclerView.setVisibility(View.GONE);
                } else {
                    binding.emptyView.setVisibility(View.GONE);
                    binding.restaurantRecyclerView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onRestaurantClick(Restaurant restaurant) {
        Bundle args = new Bundle();
        args.putString("restaurantId", restaurant.getId());
        Navigation.findNavController(requireView()).navigate(R.id.action_navigation_restaurants_to_restaurantDetailFragment, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
