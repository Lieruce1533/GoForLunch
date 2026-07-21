package com.lieruce.goforlunch.ui.workmates;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.lieruce.goforlunch.R;
import com.lieruce.goforlunch.databinding.ItemWorkmateBinding;
import com.lieruce.goforlunch.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WorkmateAdapter extends RecyclerView.Adapter<WorkmateAdapter.WorkmateViewHolder> {

    public interface OnWorkmateClickListener {
        void onWorkmateClick(User user);
    }

    private final OnWorkmateClickListener listener;
    private final String currentUserId;
    private List<User> workmates = new ArrayList<>();

    public WorkmateAdapter(String currentUserId, OnWorkmateClickListener listener) {
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    public void setWorkmates(List<User> newWorkmates) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new WorkmateDiffCallback(this.workmates, newWorkmates));
        this.workmates = new ArrayList<>(newWorkmates);
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public WorkmateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWorkmateBinding binding = ItemWorkmateBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new WorkmateViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkmateViewHolder holder, int position) {
        User user = workmates.get(position);
        holder.bind(user, currentUserId);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onWorkmateClick(user);
            }
        });
    }

    @Override
    public int getItemCount() {
        return workmates.size();
    }

    static class WorkmateViewHolder extends RecyclerView.ViewHolder {
        private final ItemWorkmateBinding binding;

        public WorkmateViewHolder(ItemWorkmateBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(User user, String currentUserId) {
            String username = user.getUsername() != null ? user.getUsername() : "A coworker";
            
            // Add (Me) label if it's the current user
            if (user.getUid() != null && user.getUid().equals(currentUserId)) {
                username += " (" + binding.getRoot().getContext().getString(R.string.me) + ")";
            }

            String status;
            
            // Get standard theme colors
            int colorOnSurface = com.google.android.material.color.MaterialColors.getColor(binding.workmateStatus, com.google.android.material.R.attr.colorOnSurface);
            int colorOnSurfaceVariant = com.google.android.material.color.MaterialColors.getColor(binding.workmateStatus, com.google.android.material.R.attr.colorOnSurfaceVariant);

            if (user.getChosenRestaurantId() != null && !user.getChosenRestaurantId().isEmpty()) {
                String restaurantName = user.getChosenRestaurantName() != null ? user.getChosenRestaurantName() : "a restaurant";
                status = binding.getRoot().getContext().getString(R.string.workmate_status_decided, username, restaurantName);
                binding.workmateStatus.setTextColor(colorOnSurface);
                binding.workmateStatus.setTypeface(null, Typeface.BOLD); // ACTIVE = BOLD
            } else {
                status = binding.getRoot().getContext().getString(R.string.workmate_status_undecided, username);
                binding.workmateStatus.setTextColor(colorOnSurfaceVariant);
                binding.workmateStatus.setTypeface(null, Typeface.ITALIC); // INACTIVE = ITALIC/GRAY
            }
            binding.workmateStatus.setText(status);

            Glide.with(binding.workmateAvatar.getContext())
                    .load(user.getAvatarUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .circleCrop()
                    .into(binding.workmateAvatar);
        }
    }

    private static class WorkmateDiffCallback extends DiffUtil.Callback {
        private final List<User> oldList;
        private final List<User> newList;

        public WorkmateDiffCallback(List<User> oldList, List<User> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() { return oldList.size(); }

        @Override
        public int getNewListSize() { return newList.size(); }

        @Override
        public boolean areItemsTheSame(int oldPos, int newPos) {
            return Objects.equals(oldList.get(oldPos).getUid(), newList.get(newPos).getUid());
        }

        @Override
        public boolean areContentsTheSame(int oldPos, int newPos) {
            User oldItem = oldList.get(oldPos);
            User newItem = newList.get(newPos);
            return Objects.equals(oldItem.getUsername(), newItem.getUsername()) &&
                   Objects.equals(oldItem.getAvatarUrl(), newItem.getAvatarUrl()) &&
                   Objects.equals(oldItem.getChosenRestaurantId(), newItem.getChosenRestaurantId()) &&
                   Objects.equals(oldItem.getChosenRestaurantName(), newItem.getChosenRestaurantName());
        }
    }
}
