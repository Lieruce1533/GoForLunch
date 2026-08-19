package com.lieruce.goforlunch.model;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

/**
 * Data Model for a single chat message.
 * Utilizes Firebase ServerTimestamp for reliable cross-client chronological sorting.
 */
public class Message {
    private String id;
    private String senderId;
    private String senderName;
    private String senderAvatarUrl;
    private String text;
    @ServerTimestamp
    private Date timestamp;

    // Required for Firestore
    public Message() {}

    public Message(String senderId, String senderName, String senderAvatarUrl, String text) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderAvatarUrl = senderAvatarUrl;
        this.text = text;
    }

    // --- GETTERS ---
    public String getId() { return id; }
    public String getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public String getSenderAvatarUrl() { return senderAvatarUrl; }
    public String getText() { return text; }
    public Date getTimestamp() { return timestamp; }

    // --- SETTERS ---
    public void setId(String id) { this.id = id; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public void setSenderAvatarUrl(String senderAvatarUrl) { this.senderAvatarUrl = senderAvatarUrl; }
    public void setText(String text) { this.text = text; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}
