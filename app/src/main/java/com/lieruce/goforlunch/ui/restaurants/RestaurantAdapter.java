package com.lieruce.goforlunch.ui.restaurants;

import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.lieruce.goforlunch.R;
import com.lieruce.goforlunch.databinding.ItemRestaurantBinding;
import com.lieruce.goforlunch.model.Restaurant;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class RestaurantAdapter extends RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder> {

    public interface OnRestaurantClickListener {
        void onRestaurantClick(Restaurant restaurant);
    }

    private final OnRestaurantClickListener listener;
    private List<Restaurant> restaurants = new ArrayList<>();
    private Location userLocation;

    public RestaurantAdapter(OnRestaurantClickListener listener) {
        this.listener = listener;
    }

    public void setRestaurants(List<Restaurant> newRestaurants) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new RestaurantDiffCallback(this.restaurants, newRestaurants));
        this.restaurants = new ArrayList<>(newRestaurants);
        diffResult.dispatchUpdatesTo(this);
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
        Restaurant restaurant = restaurants.get(position);
        holder.bind(restaurant, userLocation);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRestaurantClick(restaurant);
            }
        });
    }

    @Override
    public int getItemCount() {
        return restaurants.size();
    }

    public static class RestaurantViewHolder extends RecyclerView.ViewHolder {
        private final ItemRestaurantBinding binding;

        public RestaurantViewHolder(ItemRestaurantBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Restaurant restaurant, Location userLocation) {
            binding.restaurantName.setText(restaurant.getName());
            binding.restaurantAddress.setText(restaurant.getAddress());
            binding.restaurantRating.setRating((float) restaurant.getRating());
            binding.restaurantHours.setText(restaurant.getOpeningHours());

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

            // --- Workmates Count (Firestore) ---
            if (restaurant.getWorkmatesCount() > 0) {
                binding.workmatesCount.setVisibility(View.VISIBLE);
                binding.iconWorkmates.setVisibility(View.VISIBLE);
                binding.workmatesCount.setText(String.format(Locale.getDefault(), "(%d)", restaurant.getWorkmatesCount()));
            } else {
                binding.workmatesCount.setVisibility(View.GONE);
                binding.iconWorkmates.setVisibility(View.GONE);
            }

            // --- Photo Loading ---
            // Placeholder: Photo loading logic to be implemented with Places API photo URLs
            Glide.with(binding.restaurantPhoto.getContext())
                    .load(R.drawable.ic_launcher_background)
                    .centerCrop()
                    .into(binding.restaurantPhoto);
        }
    }

    private static class RestaurantDiffCallback extends DiffUtil.Callback {

        private final List<Restaurant> oldList;
        private final List<Restaurant> newList;

        public RestaurantDiffCallback(List<Restaurant> oldList, List<Restaurant> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList.size();
        }

        @Override
        public int getNewListSize() {
            return newList.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return Objects.equals(oldList.get(oldItemPosition).getId(), newList.get(newItemPosition).getId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Restaurant oldItem = oldList.get(oldItemPosition);
            Restaurant newItem = newList.get(newItemPosition);
            return Objects.equals(oldItem.getName(), newItem.getName()) &&
                    Objects.equals(oldItem.getAddress(), newItem.getAddress()) &&
                    oldItem.getRating() == newItem.getRating() &&
                    Objects.equals(oldItem.getOpeningHours(), newItem.getOpeningHours()) &&
                    oldItem.getWorkmatesCount() == newItem.getWorkmatesCount();
        }
    }
}
