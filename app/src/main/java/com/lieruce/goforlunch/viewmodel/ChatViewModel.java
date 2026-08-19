package com.lieruce.goforlunch.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;
import com.lieruce.goforlunch.model.Message;
import com.lieruce.goforlunch.repository.AuthRepository;
import com.lieruce.goforlunch.repository.ChatRepository;

import java.util.List;

/**
 * ViewModel managing the group chat logic.
 * Handles message persistence and provides a real-time stream of the conversation.
 */
public class ChatViewModel extends ViewModel {

    private final ChatRepository chatRepository;
    private final AuthRepository authRepository;
    private final MutableLiveData<List<Message>> messagesLiveData = new MutableLiveData<>();

    public ChatViewModel(ChatRepository chatRepository, AuthRepository authRepository) {
        this.chatRepository = chatRepository;
        this.authRepository = authRepository;
        listenToMessages();
    }

    private void listenToMessages() {
        chatRepository.getChatMessages().addSnapshotListener((value, error) -> {
            if (error != null) {
                android.util.Log.e("ChatViewModel", "Listen failed: " + error.getMessage(), error);
                return;
            }
            if (value != null) {
                List<Message> messages = value.toObjects(Message.class);
                android.util.Log.d("ChatViewModel", "Received " + messages.size() + " messages from Firestore");
                messagesLiveData.setValue(messages);
            }
        });
    }

    public void sendMessage(String text) {
        FirebaseUser user = authRepository.getCurrentUser();
        if (user != null && !text.trim().isEmpty()) {
            Message message = new Message(
                    user.getUid(),
                    user.getDisplayName(),
                    user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null,
                    text
            );
            chatRepository.sendMessage(message);
        }
    }

    public LiveData<List<Message>> getMessages() {
        return messagesLiveData;
    }
}
