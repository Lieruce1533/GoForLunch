package com.lieruce.goforlunch.ui.restaurants;

import android.location.Location;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.lieruce.goforlunch.R;
import com.lieruce.goforlunch.databinding.ItemRestaurantBinding;
import com.lieruce.goforlunch.model.Restaurant;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder> {

    private List<Restaurant> restaurants = new ArrayList<>();
    private Location userLocation;

    public void setRestaurants(List<Restaurant> restaurants) {
        this.restaurants = restaurants;
        notifyDataSetChanged();
    }

    public void setUserLocation(Location location) {
        this.userLocation = location;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RestaurantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRestaurantBinding binding = ItemRestaurantBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new RestaurantViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RestaurantViewHolder holder, int position) {
        holder.bind(restaurants.get(position), userLocation);
    }

    @Override
    public int getItemCount() {
        return restaurants.size();
    }

    static class RestaurantViewHolder extends RecyclerView.ViewHolder {
        private final ItemRestaurantBinding binding;

        public RestaurantViewHolder(ItemRestaurantBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Restaurant restaurant, Location userLocation) {
            binding.restaurantName.setText(restaurant.getName());
            binding.restaurantAddress.setText(restaurant.getAddress());
            binding.restaurantRating.setRating((float) restaurant.getRating());

            // --- Distance Calculation ---
            if (userLocation != null) {
                float[] results = new float[1];
                Location.distanceBetween(
                        userLocation.getLatitude(), userLocation.getLongitude(),
                        restaurant.getLatitude(), restaurant.getLongitude(),
                        results
                );
                int distance = Math.round(results[0]);
                binding.restaurantDistance.setText(String.format(Locale.getDefault(), "%dm", distance));
            } else {
                binding.restaurantDistance.setText("");
            }

            // --- Photo Loading ---
            String photoUrl = null;
            if (restaurant.getPhotoMetadatas() != null && !restaurant.getPhotoMetadatas().isEmpty()) {
                // Construction of the URL would normally happen here or in the repository.
                // For now, we'll use a placeholder logic until the Repo provides the URL.
            }

            Glide.with(binding.restaurantPhoto.getContext())
                    .load(photoUrl)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .centerCrop()
                    .into(binding.restaurantPhoto);

            // TODO: Bind hours and workmates count
        }
    }
}
