package com.example.chatbotapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.Toast;

import android.graphics.Color;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.widget.Toolbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public class MainActivity extends AppCompatActivity {

    private RecyclerView chatRecycler;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatList;

    private EditText inputText;
    private Button sendBtn;

    private int sessionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        chatRecycler = findViewById(R.id.chatRecycler);
        inputText = findViewById(R.id.inputText);
        sendBtn = findViewById(R.id.sendBtn);

        Button infoBtn = findViewById(R.id.infoBtn);
        infoBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, InfoActivity.class);
            startActivity(intent);
        });

        Button chatListBtn = findViewById(R.id.btnChatList);
        chatListBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ChatListActivity.class);
            startActivity(intent);
            finish();
        });

        chatList = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatList);
        chatRecycler.setLayoutManager(new LinearLayoutManager(this));
        chatRecycler.setAdapter(chatAdapter);

        sessionId = getIntent().getIntExtra("session_id", -1);

        new Thread(() -> {
            List<ChatMessage> messages = ChatDatabase.getInstance(getApplicationContext())
                    .chatMessageDao()
                    .getMessagesForSession(sessionId);

            runOnUiThread(() -> {
                chatList.addAll(messages);
                chatAdapter.notifyDataSetChanged();
                chatRecycler.scrollToPosition(chatList.size() - 1);
            });
        }).start();

        sendBtn.setOnClickListener(v -> {
            String message = inputText.getText().toString().trim();
            if (!message.isEmpty()) {
                long timestamp = System.currentTimeMillis();
                ChatMessage chatMessage = new ChatMessage(message, ChatMessage.TYPE_USER, timestamp);
                chatMessage.setSessionId(sessionId);

                chatList.add(chatMessage);
                chatAdapter.notifyItemInserted(chatList.size() - 1);
                chatRecycler.scrollToPosition(chatList.size() - 1);
                inputText.setText("");

                new Thread(() -> {
                    ChatDatabase.getInstance(getApplicationContext())
                            .chatMessageDao()
                            .insertMessage(chatMessage);
                }).start();

                sendMessageToGemini(message);
            }
        });
    }

    private void sendMessageToGemini(String message) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://generativelanguage.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        GeminiService service = retrofit.create(GeminiService.class);

        Part part = new Part(message);
        Content content = new Content(Collections.singletonList(part));
        GeminiRequest request = new GeminiRequest(Collections.singletonList(content));

        service.sendMessage(request).enqueue(new Callback<GeminiResponse>() {
            @Override
            public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String reply = response.body().candidates.get(0).content.parts.get(0).text;

                        ChatMessage botMessage = new ChatMessage(reply, ChatMessage.TYPE_BOT, System.currentTimeMillis());
                        botMessage.setSessionId(sessionId);

                        chatList.add(botMessage);
                        chatAdapter.notifyItemInserted(chatList.size() - 1);
                        chatRecycler.scrollToPosition(chatList.size() - 1);

                        new Thread(() -> {
                            ChatDatabase.getInstance(getApplicationContext())
                                    .chatMessageDao()
                                    .insertMessage(botMessage);
                        }).start();

                    } catch (Exception e) {
                        chatList.add(new ChatMessage("parse error", ChatMessage.TYPE_BOT, System.currentTimeMillis()));
                        chatAdapter.notifyItemInserted(chatList.size() - 1);
                    }
                } else {
                    chatList.add(new ChatMessage("no response. code: " + response.code(), ChatMessage.TYPE_BOT, System.currentTimeMillis()));
                    chatAdapter.notifyItemInserted(chatList.size() - 1);
                }
            }

            @Override
            public void onFailure(Call<GeminiResponse> call, Throwable t) {
                chatList.add(new ChatMessage("fail: " + t.getMessage(), ChatMessage.TYPE_BOT, System.currentTimeMillis()));
                chatAdapter.notifyItemInserted(chatList.size() - 1);
            }
        });
    }

    interface GeminiService {
        @Headers({
                "Content-Type: application/json",
                "X-Goog-Api-Key: AIzaSyAs0xyxFBIfGXxuQybH-F_28UuqyV0ZGxo"
        })
        @POST("v1beta/models/gemini-2.0-flash:generateContent")
        Call<GeminiResponse> sendMessage(@Body GeminiRequest request);
    }

    class GeminiRequest {
        List<Content> contents;
        GeminiRequest(List<Content> contents) {
            this.contents = contents;
        }
    }

    class Content {
        List<Part> parts;
        Content(List<Part> parts) {
            this.parts = parts;
        }
    }

    class Part {
        String text;
        Part(String text) {
            this.text = text;
        }
    }

    class GeminiResponse {
        List<Candidate> candidates;
    }

    class Candidate {
        ContentResponse content;
    }

    class ContentResponse {
        List<PartResponse> parts;
    }

    class PartResponse {
        String text;
    }

    private void saveTheme(String mode) {
        getSharedPreferences("settings", MODE_PRIVATE)
                .edit()
                .putString("theme", mode)
                .apply();
    }

    private void loadTheme() {
        String theme = getSharedPreferences("settings", MODE_PRIVATE)
                .getString("theme", "light");

        if (theme.equals("dark")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_dark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            saveTheme("dark");
            recreate();
            return true;
        } else if (id == R.id.menu_light) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            saveTheme("light");
            recreate();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
