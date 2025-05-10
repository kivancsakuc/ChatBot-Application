package com.example.chatbotapplication;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ChatSessionDao {

    @Query("DELETE FROM chat_sessions WHERE sessionId = :id")
    void deleteSession(int id);

    @Query("DELETE FROM chat_messages WHERE sessionId = :id")
    void deleteMessagesForSession(int id);

    @Update
    void updateSession(ChatSession session);

    @Query("SELECT COUNT(*) FROM chat_sessions")
    int getSessionCount();

    @Insert
    long insertSession(ChatSession session);

    @Query("SELECT * FROM chat_sessions ORDER BY timestamp DESC")
    List<ChatSession> getAllSessions();
}
