package com.example.chatbotapplication;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "chat_sessions")
public class ChatSession {

    @PrimaryKey(autoGenerate = true)
    public int sessionId;

    public String title;
    public long timestamp;

    public ChatSession(String title, long timestamp) {
        this.title = title;
        this.timestamp = timestamp;
    }


    public ChatSession() {}
}
