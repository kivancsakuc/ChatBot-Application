package com.example.chatbotapplication;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ChatMessage {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public static final int TYPE_USER = 0;
    public static final int TYPE_BOT = 1;

    private String message;
    private int type;
    private int sessionId;

    public ChatMessage(String message, int type, int sessionId) {
        this.message = message;
        this.type = type;
        this.sessionId = sessionId;
    }

    public String getMessage() {
        return message;
    }

    public int getType() {
        return type;
    }

    public int getSessionId() {
        return sessionId;
    }
}