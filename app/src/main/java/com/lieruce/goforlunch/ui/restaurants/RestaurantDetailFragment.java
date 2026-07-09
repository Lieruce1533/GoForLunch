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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.lieruce.goforlunch.R;
import com.lieruce.goforlunch.databinding.FragmentRestaurantDetailBinding;
import com.lieruce.goforlunch.model.Restaurant;
import com.lieruce.goforlunch.viewmodel.RestaurantDetailViewModel;
import com.lieruce.goforlunch.viewmodel.ViewModelFactory;

public class RestaurantDetailFragment extends Fragment {

    private FragmentRestaurantDetailBinding binding;
    private RestaurantDetailViewModel viewModel;
    private WorkmateDetailAdapter workmateAdapter;

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
        setupRecyclerView();
        if (restaurantId != null) {
            viewModel.setRestaurantId(restaurantId);
        }

        observeData();
        setupClickListeners();
    }

    private void setupViewModel() {
        ViewModelFactory factory = ViewModelFactory.getInstance(requireContext());
        viewModel = new ViewModelProvider(this, factory).get(RestaurantDetailViewModel.class);
    }

    private void setupRecyclerView() {
        workmateAdapter = new WorkmateDetailAdapter();
        binding.workmatesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.workmatesRecyclerView.setAdapter(workmateAdapter);
    }

    private void observeData() {
        // Observe restaurant details
        viewModel.getRestaurant().observe(getViewLifecycleOwner(), restaurant -> {
            if (restaurant != null) {
                updateUI(restaurant);
            }
        });

        // Observe selection state (The FAB)
        viewModel.getIsRestaurantSelected().observe(getViewLifecycleOwner(), isSelected -> {
            if (isSelected) {
                binding.detailSelectFab.setText(R.string.cancel_choice);
                binding.detailSelectFab.setIconResource(R.drawable.ic_check_circle);
                binding.detailSelectFab.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark));
            } else {
                binding.detailSelectFab.setText(R.string.pick_this_restaurant);
                binding.detailSelectFab.setIconResource(android.R.drawable.checkbox_off_background);
                binding.detailSelectFab.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_green_dark));
            }
        });

        // Observe like state
        viewModel.getIsRestaurantLiked().observe(getViewLifecycleOwner(), isLiked -> {
            MaterialButton btnLike = (MaterialButton) binding.btnLike;
            if (isLiked) {
                btnLike.setIconResource(android.R.drawable.btn_star_big_on);
            } else {
                btnLike.setIconResource(android.R.drawable.btn_star_big_off);
            }
        });

        // Observe workmates list
        viewModel.getWorkmates().observe(getViewLifecycleOwner(), workmates -> {
            if (workmates != null) {
                workmateAdapter.setWorkmates(workmates);
            }
        });
    }

    private void updateUI(Restaurant restaurant) {
        binding.detailRestaurantName.setText(restaurant.getName());
        binding.detailRestaurantAddress.setText(restaurant.getAddress());
        binding.detailRestaurantHours.setText(restaurant.getOpeningHours());
        
        // Normalize Google Rating (0-5) to App Stars (0-3)
        float normalizedRating = (float) (restaurant.getRating() * 3.0 / 5.0);
        binding.detailRestaurantRating.setRating(normalizedRating);

        // Photo loading
        Glide.with(this)
                .load(restaurant.getPhotoUrl())
                .placeholder(R.drawable.ic_default_restaurant)
                .error(R.drawable.ic_default_restaurant)
                .centerCrop()
                .into(binding.detailRestaurantPhoto);
        
        // Setup Call button
        binding.btnCall.setOnClickListener(v -> {
            if (restaurant.getPhoneNumber() != null) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + restaurant.getPhoneNumber()));
                startActivity(intent);
            } else {
                Toast.makeText(requireContext(), R.string.no_phone_number, Toast.LENGTH_SHORT).show();
            }
        });

        // Setup Website button
        binding.btnWebsite.setOnClickListener(v -> {
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
        binding.detailSelectFab.setOnClickListener(v -> {
            viewModel.toggleSelection();
            Toast.makeText(requireContext(), R.string.selection_updated, Toast.LENGTH_SHORT).show();
        });
        binding.btnLike.setOnClickListener(v -> {
            viewModel.toggleLike();
            Toast.makeText(requireContext(), R.string.likes_updated, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
