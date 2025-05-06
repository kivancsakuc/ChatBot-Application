package com.example.chatbotapplication;


import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

    EditText inputText;
    Button sendBtn;
    TextView responseText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        inputText = findViewById(R.id.inputText);
        sendBtn = findViewById(R.id.sendBtn);
        responseText = findViewById(R.id.responseText);

        sendBtn.setOnClickListener(v -> {
            String message = inputText.getText().toString();
            sendMessage(message);
        });
    }

    private void sendMessage(String message) {
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
                        responseText.setText(reply);
                    } catch (Exception e) {
                        responseText.setText("parse error");
                    }
                } else {
                    responseText.setText("no response. code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GeminiResponse> call, Throwable t) {
                responseText.setText("fail: " + t.getMessage());
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
