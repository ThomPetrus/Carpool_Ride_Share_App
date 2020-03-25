package com.project.carpool_ride_share_app.models;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

/**
 *  Credit goes to CodingWithMitch. The Chat portion of his open source tutorial was used as a basis for the
 *  project.
 *
 *  Standard Java Object for each Chat Message
 *  Consisting of User details, message and the Firebase servertimestamp.
 */

public class ChatMessage {

    private User user;
    private String message;
    private String message_id;
    private @ServerTimestamp
    Date timestamp;

    public ChatMessage(User user, String message, String message_id, Date timestamp) {
        this.user = user;
        this.message = message;
        this.message_id = message_id;
        this.timestamp = timestamp;
    }

    public ChatMessage() {

    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage_id() {
        return message_id;
    }

    public void setMessage_id(String message_id) {
        this.message_id = message_id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "user=" + user +
                ", message='" + message + '\'' +
                ", message_id='" + message_id + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
