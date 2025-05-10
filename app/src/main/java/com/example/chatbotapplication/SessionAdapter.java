package com.example.chatbotapplication;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SessionAdapter extends RecyclerView.Adapter<SessionAdapter.SessionViewHolder> {

    private List<ChatSession> sessionList;
    private OnSessionClickListener listener;

    public SessionAdapter(List<ChatSession> sessionList, OnSessionClickListener listener) {
        this.sessionList = sessionList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_session, parent, false);
        return new SessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SessionViewHolder holder, int position) {
        ChatSession session = sessionList.get(position);
        holder.titleText.setText(session.title);

        holder.itemView.setOnClickListener(v -> {
            listener.onSessionClick(session.sessionId);
        });

        holder.deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(holder.itemView.getContext())
                    .setTitle("Sohbeti Sil")
                    .setMessage("Bu sohbeti silmek istiyor musunuz?")
                    .setPositiveButton("Evet", (dialog, which) -> {
                        int sessionId = session.sessionId;

                        new Thread(() -> {
                            ChatDatabase db = ChatDatabase.getInstance(holder.itemView.getContext());
                            db.chatSessionDao().deleteMessagesForSession(sessionId);
                            db.chatSessionDao().deleteSession(sessionId);

                            sessionList.remove(position);

                            ((AppCompatActivity) holder.itemView.getContext()).runOnUiThread(() -> {
                                notifyItemRemoved(position);
                            });
                        }).start();
                    })
                    .setNegativeButton("Hayır", null)
                    .show();
        });
    }

    @Override
    public int getItemCount() {
        return sessionList.size();
    }

    public interface OnSessionClickListener {
        void onSessionClick(int sessionId);
    }

    public static class SessionViewHolder extends RecyclerView.ViewHolder {
        TextView titleText;
        Button deleteButton;

        public SessionViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.sessionTitle);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
