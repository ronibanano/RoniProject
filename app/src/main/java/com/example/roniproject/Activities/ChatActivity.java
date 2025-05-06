package com.example.roniproject.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.example.roniproject.Obj.Message;
import com.example.roniproject.Obj.MessageAdapter;
import com.example.roniproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private ListView chatListView;
    private EditText etMessage;
    private Button btnSend;

    private MessageAdapter messageAdapter;
    private List<Message> messageList;

    private String chatId, receiverId;

    private DatabaseReference chatRef;

    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatListView = findViewById(R.id.chatListView);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);


        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String currentUserId = currentUser.getUid();
        String otherUserId = getIntent().getStringExtra("otherUserId");

        if (otherUserId == null) {
            Toast.makeText(this, "Missing other user info", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        receiverId = otherUserId;

        // חישוב chatId לפי סדר כדי שיהיה תמיד קבוע
        if (currentUserId.compareTo(otherUserId) < 0) {
            chatId = currentUserId + "_" + otherUserId;
        } else {
            chatId = otherUserId + "_" + currentUserId;
        }

        chatRef = FirebaseDatabase.getInstance().getReference().child("Chats").child(chatId);

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageList);
        chatListView.setAdapter(messageAdapter);

        // טוען את ההודעות
        loadMessages();

        // שליחת הודעה
        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                sendMessage(text);
                etMessage.setText("");
            }
        });
    }

    // טוען את כל ההודעות הקיימות מהצ'אט
    private void loadMessages() {
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot msgSnap : snapshot.getChildren()) {
                    Message msg = msgSnap.getValue(Message.class);
                    if (msg != null) {
                        // אם ההודעה מיועדת אליי והיא לא נקראה - נעדכן ל־true
                        if (msg.getReceiverId() != null && msg.getReceiverId().equals(currentUser.getUid()) && !msg.isRead()) {
                            msgSnap.getRef().child("isRead").setValue(true);
                        }
                        messageList.add(msg);
                    }
                }
                messageAdapter.notifyDataSetChanged();
                chatListView.setSelection(messageList.size() - 1); // scroll to bottom
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // שליחת הודעה ל־Firebase
    private void sendMessage(String text) {
        String senderId = currentUser.getUid();
        Message msg = new Message(senderId, receiverId, text, System.currentTimeMillis());

        // הוספת ההודעה ל־Firebase
        String messageId = chatRef.push().getKey();
        if (messageId != null) {
            chatRef.child(messageId).setValue(msg);
            sendNotification(receiverId, "New message", text); // שליחת התראה
        }
    }

    // שליחת התראה ל־FCM
    private void sendNotification(String receiverId, String title, String body) {
        // לקבל את ה־FCM token של המשתמש שמקבל את ההודעה
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(receiverId);
        userRef.child("fcmToken").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String token = task.getResult().getValue(String.class);

                if (token != null) {
                    try {
                        JSONObject json = new JSONObject();
                        JSONObject notification = new JSONObject();
                        notification.put("title", title);
                        notification.put("body", body);
                        json.put("to", token);
                        json.put("notification", notification);

                        // שליחת בקשה ל־FCM
                        RequestQueue queue = Volley.newRequestQueue(ChatActivity.this);
                        String url = "https://fcm.googleapis.com/fcm/send";

                        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, json,
                                response -> {},
                                error -> {}
                        ) {
                            @Override
                            public Map<String, String> getHeaders() throws AuthFailureError {
                                Map<String, String> headers = new HashMap<>();
                                headers.put("Content-Type", "application/json");
                                headers.put("Authorization", "key=BNxWyD7HbjhU69_AbWXzPNYCoeJ7wmxeUQ0aAec_WVuec7gNf6BK_Z3Fimj_iFN9thLxMfHYM7QaNL5YBMBp0Hw");
                                return headers;
                            }
                        };

                        queue.add(request);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
