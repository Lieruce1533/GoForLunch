package com.lieruce.goforlunch.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.lieruce.goforlunch.model.Message;

/**
 * Repository for persistent real-time chat data.
 * Manages the "chat_messages" Firestore collection and provides sorted message queries.
 */
public class ChatRepository {

    private static final String COLLECTION_NAME = "chat_messages";
    private static volatile ChatRepository instance;
    private final CollectionReference chatCollection;

    private ChatRepository() {
        this.chatCollection = FirebaseFirestore.getInstance().collection(COLLECTION_NAME);
    }

    public static ChatRepository getInstance() {
        if (instance == null) {
            synchronized(ChatRepository.class) {
                if (instance == null) {
                    instance = new ChatRepository();
                }
            }
        }
        return instance;
    }

    public Task<Void> sendMessage(Message message) {
        return chatCollection.document().set(message);
    }

    public Query getChatMessages() {
        return chatCollection.orderBy("timestamp", Query.Direction.ASCENDING);
    }
}
