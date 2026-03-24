package com.lieruce.goforlunch.ui.restaurants;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.lieruce.goforlunch.R;
import com.lieruce.goforlunch.databinding.FragmentRestaurantDetailBinding;
import com.lieruce.goforlunch.model.Restaurant;
import com.lieruce.goforlunch.viewmodel.RestaurantDetailViewModel;
import com.lieruce.goforlunch.viewmodel.ViewModelFactory;

public class RestaurantDetailFragment extends Fragment {

    private FragmentRestaurantDetailBinding binding;
    private RestaurantDetailViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRestaurantDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String restaurantId = getArguments() != null ? getArguments().getString("restaurantId") : null;

        setupViewModel();
        if (restaurantId != null) {
            viewModel.setRestaurantId(restaurantId);
        }

        observeRestaurant();
        setupClickListeners();
    }

    private void setupViewModel() {
        ViewModelFactory factory = ViewModelFactory.getInstance(requireContext());
        viewModel = new ViewModelProvider(this, factory).get(RestaurantDetailViewModel.class);
    }

    private void observeRestaurant() {
        viewModel.getRestaurant().observe(getViewLifecycleOwner(), restaurant -> {
            if (restaurant != null) {
                updateUI(restaurant);
            }
        });
    }

    private void updateUI(Restaurant restaurant) {
        binding.detail_restaurant_name.setText(restaurant.getName());
        binding.detail_restaurant_address.setText(restaurant.getAddress());
        binding.detail_restaurant_rating.setRating((float) restaurant.getRating());

        // Photo loading
        Glide.with(this)
                .load(R.drawable.ic_launcher_background) // Placeholder
                .centerCrop()
                .into(binding.detail_restaurant_photo);
        
        // Setup Call button
        binding.btn_call.setOnClickListener(v -> {
            if (restaurant.getPhoneNumber() != null) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + restaurant.getPhoneNumber()));
                startActivity(intent);
            } else {
                Toast.makeText(requireContext(), R.string.no_phone_number, Toast.LENGTH_SHORT).show();
            }
        });

        // Setup Website button
        binding.btn_website.setOnClickListener(v -> {
            if (restaurant.getWebsiteUrl() != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(restaurant.getWebsiteUrl()));
                startActivity(intent);
            } else {
                Toast.makeText(requireContext(), R.string.no_website, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners() {
        binding.detail_select_fab.setOnClickListener(v -> {
            // TODO: Call viewModel.toggleSelection()
        });

        binding.btn_like.setOnClickListener(v -> {
            // TODO: Call viewModel.toggleLike()
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
