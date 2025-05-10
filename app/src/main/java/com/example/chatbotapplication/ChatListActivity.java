package com.example.chatbotapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import com.example.chatbotapplication.SessionAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatListActivity extends AppCompatActivity implements SessionAdapter.OnSessionClickListener {

    private RecyclerView recyclerView;
    private SessionAdapter sessionAdapter;
    private Button newChatBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        recyclerView = findViewById(R.id.recyclerView);
        newChatBtn = findViewById(R.id.newChatBtn);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadSessions();

        newChatBtn.setOnClickListener(v -> {
            new Thread(() -> {
                int count = ChatDatabase.getInstance(getApplicationContext())
                        .chatSessionDao()
                        .getSessionCount();

                String title = "Chat " + (count + 1);
                ChatSession newSession = new ChatSession(title, System.currentTimeMillis());

                long sessionId = ChatDatabase.getInstance(getApplicationContext())
                        .chatSessionDao()
                        .insertSession(newSession);

                runOnUiThread(() -> {
                    Intent intent = new Intent(ChatListActivity.this, MainActivity.class);
                    intent.putExtra("session_id", (int) sessionId);
                    startActivity(intent);
                });
            }).start();
        });
    }

    private void loadSessions() {
        new Thread(() -> {
            List<ChatSession> sessions = ChatDatabase.getInstance(getApplicationContext())
                    .chatSessionDao()
                    .getAllSessions();

            runOnUiThread(() -> {
                sessionAdapter = new SessionAdapter(sessions, this);
                recyclerView.setAdapter(sessionAdapter);
            });
        }).start();
    }

    @Override
    public void onSessionClick(int sessionId) {
        Intent intent = new Intent(ChatListActivity.this, MainActivity.class);
        intent.putExtra("session_id", sessionId);
        startActivity(intent);
    }
}
