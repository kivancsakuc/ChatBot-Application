package com.example.chatbotapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Intent;
import android.widget.Button;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        chatList = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatList);

        chatRecycler.setLayoutManager(new LinearLayoutManager(this));
        chatRecycler.setAdapter(chatAdapter);

        sendBtn.setOnClickListener(v -> {
            String message = inputText.getText().toString().trim();
            if (!message.isEmpty()) {
                chatList.add(new ChatMessage(message, ChatMessage.TYPE_USER));
                chatAdapter.notifyItemInserted(chatList.size() - 1);
                chatRecycler.scrollToPosition(chatList.size() - 1);
                inputText.setText("");
                sendMessageToGemini(message);
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_date) {
            showDatePicker();
            return true;
        } else if (item.getItemId() == R.id.menu_time) {
            showTimePicker();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void showDatePicker() {
        DatePickerDialog datePicker = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    Toast.makeText(this, "Seçilen Tarih: " + dayOfMonth + "/" + (month + 1) + "/" + year, Toast.LENGTH_SHORT).show();
                }, 2024, 4, 20); // Başlangıç tarihi
        datePicker.show();
    }
    private void showTimePicker() {
        TimePickerDialog timePicker = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    Toast.makeText(this, "Seçilen Saat: " + hourOfDay + ":" + minute, Toast.LENGTH_SHORT).show();
                }, 12, 0, true);
        timePicker.show();
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
                        chatList.add(new ChatMessage(reply, ChatMessage.TYPE_BOT));
                        chatAdapter.notifyItemInserted(chatList.size() - 1);
                        chatRecycler.scrollToPosition(chatList.size() - 1);
                    } catch (Exception e) {
                        chatList.add(new ChatMessage("parse error", ChatMessage.TYPE_BOT));
                        chatAdapter.notifyItemInserted(chatList.size() - 1);
                    }
                } else {
                    chatList.add(new ChatMessage("no response. code: " + response.code(), ChatMessage.TYPE_BOT));
                    chatAdapter.notifyItemInserted(chatList.size() - 1);
                }
            }

            @Override
            public void onFailure(Call<GeminiResponse> call, Throwable t) {
                chatList.add(new ChatMessage("fail: " + t.getMessage(), ChatMessage.TYPE_BOT));
                chatAdapter.notifyItemInserted(chatList.size() - 1);
            }
        });
    }

    interface GeminiService {
        @Headers({
                "Content-Type: application/json",
                "X-Goog-Api-Key: AIzaSyB9LaKc3ym1T4aidtMvKYSXc2ujkgWwsL0"
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
}
