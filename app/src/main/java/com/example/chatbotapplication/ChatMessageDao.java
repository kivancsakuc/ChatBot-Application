package com.example.chatbotapplication;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ChatMessageDao {

    @Insert
    void insert(ChatMessage message);

    @Query("SELECT * FROM ChatMessage WHERE sessionId = :sessionId")
    List<ChatMessage> getMessagesForSession(int sessionId);

    @Query("DELETE FROM ChatMessage WHERE sessionId = :sessionId")
    void deleteMessagesForSession(int sessionId);
}