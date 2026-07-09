package com.lieruce.goforlunch.ui.restaurants;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.lieruce.goforlunch.R;
import com.lieruce.goforlunch.databinding.ItemWorkmateDetailBinding;
import com.lieruce.goforlunch.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WorkmateDetailAdapter extends RecyclerView.Adapter<WorkmateDetailAdapter.WorkmateViewHolder> {

    private List<User> workmates = new ArrayList<>();

    public void setWorkmates(List<User> newWorkmates) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new WorkmateDiffCallback(this.workmates, newWorkmates));
        this.workmates = new ArrayList<>(newWorkmates);
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public WorkmateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWorkmateDetailBinding binding = ItemWorkmateDetailBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new WorkmateViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkmateViewHolder holder, int position) {
        holder.bind(workmates.get(position));
    }

    @Override
    public int getItemCount() {
        return workmates.size();
    }

    public static class WorkmateViewHolder extends RecyclerView.ViewHolder {
        private final ItemWorkmateDetailBinding binding;

        public WorkmateViewHolder(ItemWorkmateDetailBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(User user) {
            String username = user.getUsername() != null ? user.getUsername() : "A coworker";
            String joiningText = username + " is joining!";
            binding.workmateName.setText(joiningText);

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
            return Objects.equals(oldList.get(oldPos).getUsername(), newList.get(newPos).getUsername()) &&
                   Objects.equals(oldList.get(oldPos).getAvatarUrl(), newList.get(newPos).getAvatarUrl());
        }
    }
}
