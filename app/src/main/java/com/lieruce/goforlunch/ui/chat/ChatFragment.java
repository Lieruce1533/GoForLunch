package com.lieruce.goforlunch.ui.chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.lieruce.goforlunch.databinding.FragmentChatBinding;
import com.lieruce.goforlunch.repository.AuthRepository;
import com.lieruce.goforlunch.viewmodel.ChatViewModel;
import com.lieruce.goforlunch.viewmodel.ViewModelFactory;

public class ChatFragment extends Fragment {

    private FragmentChatBinding binding;
    private ChatViewModel viewModel;
    private ChatAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentChatBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        setupViewModel();
        setupRecyclerView();
        observeMessages();
        setupInput();
    }

    private void setupViewModel() {
        ViewModelFactory factory = ViewModelFactory.getInstance(requireContext());
        viewModel = new ViewModelProvider(this, factory).get(ChatViewModel.class);
    }

    private void setupRecyclerView() {
        String currentUserId = AuthRepository.getInstance().getCurrentUser() != null 
                ? AuthRepository.getInstance().getCurrentUser().getUid() 
                : "";
        adapter = new ChatAdapter(currentUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext());
        layoutManager.setStackFromEnd(true); // Always show latest messages
        binding.chatRecyclerView.setLayoutManager(layoutManager);
        binding.chatRecyclerView.setAdapter(adapter);
    }

    private void observeMessages() {
        viewModel.getMessages().observe(getViewLifecycleOwner(), messages -> {
            if (messages != null) {
                adapter.setMessages(messages);
                // Scroll to bottom on new message
                binding.chatRecyclerView.post(() -> binding.chatRecyclerView.smoothScrollToPosition(adapter.getItemCount()));
            }
        });
    }

    private void setupInput() {
        binding.btnSend.setOnClickListener(v -> {
            String text = binding.chatInput.getText().toString();
            if (!text.trim().isEmpty()) {
                viewModel.sendMessage(text);
                binding.chatInput.setText("");
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
