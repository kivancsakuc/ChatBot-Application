package com.example.chatbotapplication;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class ChatSession {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;

    public ChatSession(String title) {
        this.title = title;
    }


}
