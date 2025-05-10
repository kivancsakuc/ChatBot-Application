package com.example.chatbotapplication;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface ChatMessageDao {

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    List<ChatMessage> getMessagesForSession(int sessionId);

    @Insert
    void insertMessage(ChatMessage message);

    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    List<ChatMessage> getAllMessages();
}
