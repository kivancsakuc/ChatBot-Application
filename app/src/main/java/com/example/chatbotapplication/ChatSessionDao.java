package com.example.chatbotapplication;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ChatSessionDao {

    @Insert
    long insert(ChatSession session);

    @Query("SELECT * FROM ChatSession")
    List<ChatSession> getAllSessions();
}