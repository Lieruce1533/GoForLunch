package com.lieruce.goforlunch.ui.chat;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.lieruce.goforlunch.R;
import com.lieruce.goforlunch.databinding.ItemChatMessageBinding;
import com.lieruce.goforlunch.model.Message;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private final String currentUserId;
    private List<Message> messages = new ArrayList<>();

    public ChatAdapter(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    public void setMessages(List<Message> newMessages) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new ChatDiffCallback(this.messages, newMessages));
        this.messages = new ArrayList<>(newMessages);
        diffResult.dispatchUpdatesTo(this);
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemChatMessageBinding binding = ItemChatMessageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ChatViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        holder.bind(messages.get(position), currentUserId);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        private final ItemChatMessageBinding binding;

        public ChatViewHolder(ItemChatMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Message message, String currentUserId) {
            boolean isMe = message.getSenderId() != null && message.getSenderId().equals(currentUserId);
            
            binding.messageSenderName.setText(isMe ? binding.getRoot().getContext().getString(R.string.me) : message.getSenderName());
            binding.messageText.setText(message.getText());
            
            Glide.with(binding.messageAvatar.getContext())
                    .load(message.getSenderAvatarUrl())
                    .placeholder(R.drawable.ic_launcher_background)
                    .circleCrop()
                    .into(binding.messageAvatar);

            // Robust positioning logic
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) binding.messageBubble.getLayoutParams();
            
            if (isMe) {
                // Align Right
                params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
                params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
                params.horizontalBias = 1.0f;
                params.setMarginStart(64); // Big gap on left
                params.setMarginEnd(8);    // Small gap on right
                
                binding.messageBubble.setCardBackgroundColor(binding.getRoot().getContext().getColor(R.color.colorMarkerGreen));
                binding.messageText.setTextColor(binding.getRoot().getContext().getColor(android.R.color.white));
                binding.messageAvatar.setVisibility(ViewGroup.GONE);
                binding.messageSenderName.setVisibility(ViewGroup.GONE);
            } else {
                // Align Left
                params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
                params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
                params.horizontalBias = 0.0f;
                params.setMarginStart(48); // Room for avatar
                params.setMarginEnd(64);   // Big gap on right
                
                binding.messageBubble.setCardBackgroundColor(binding.getRoot().getContext().getColor(R.color.chat_bubble_other));
                binding.messageText.setTextColor(binding.getRoot().getContext().getColor(R.color.chat_text_other));
                binding.messageAvatar.setVisibility(ViewGroup.VISIBLE);
                binding.messageSenderName.setVisibility(ViewGroup.VISIBLE);
            }
            binding.messageBubble.setLayoutParams(params);
        }
    }

    private static class ChatDiffCallback extends DiffUtil.Callback {
        private final List<Message> oldList;
        private final List<Message> newList;

        public ChatDiffCallback(List<Message> oldList, List<Message> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override public int getOldListSize() { return oldList.size(); }
        @Override public int getNewListSize() { return newList.size(); }

        @Override
        public boolean areItemsTheSame(int oldPos, int newPos) {
            return Objects.equals(oldList.get(oldPos).getTimestamp(), newList.get(newPos).getTimestamp())
                    && Objects.equals(oldList.get(oldPos).getSenderId(), newList.get(newPos).getSenderId());
        }

        @Override
        public boolean areContentsTheSame(int oldPos, int newPos) {
            return Objects.equals(oldList.get(oldPos).getText(), newList.get(newPos).getText());
        }
    }
}
