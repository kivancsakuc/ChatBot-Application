package com.example.chatbotapplication;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "chat_messages")
public class ChatMessage {

    @PrimaryKey(autoGenerate = true)
    private int id;

    public static final int TYPE_USER = 0;
    public static final int TYPE_BOT = 1;
    private int sessionId;

    public int getSessionId() { return sessionId; }
    public void setSessionId(int sessionId) { this.sessionId = sessionId; }

    private String message;
    private int type;
    private long timestamp;


    public ChatMessage(String message, int type, long timestamp) {
        this.message = message;
        this.type = type;
        this.timestamp = timestamp;
    }


    public ChatMessage() {}


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
