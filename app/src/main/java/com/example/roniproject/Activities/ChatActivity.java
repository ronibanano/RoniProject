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

/**
 * Activity that manages a one-on-one chat interface between two users.
 * <p>
 * This activity is responsible for:
 * <ul>
 *     <li>Displaying messages exchanged between the current user and another user.</li>
 *     <li>Allowing the current user to send new messages.</li>
 *     <li>Loading existing messages from Firebase Realtime Database.</li>
 *     <li>Listening for new messages in real-time and updating the UI.</li>
 * </ul>
 * The chat is identified by a unique {@code chatId}, which is consistently generated
 * based on the UIDs of the two users involved to ensure both users access the same chat node.
 * </p>
 * <p>
 * It requires the {@code otherUserId} to be passed via an Intent extra to identify
 * the recipient of the messages.
 * </p>
 *
 * @see Message
 * @see MessageAdapter
 * @see FirebaseDatabase
 * @see FirebaseAuth
 */
public class ChatActivity extends AppCompatActivity {

    private ListView chatListView;
    private EditText etMessage;
    private Button btnSend;

    private MessageAdapter messageAdapter;
    private List<Message> messageList;

    private String chatId, receiverId;

    private DatabaseReference chatRef;

    private FirebaseUser currentUser;

    /**
     * Called when the activity is first created.
     * <p>
     * Initializes UI components, Firebase references, and the current user session.
     * It retrieves the {@code otherUserId} from the intent extras to establish the chat context.
     * It then calculates a unique {@code chatId} and sets up listeners for sending messages
     * and loading existing messages from the Firebase Realtime Database.
     * </p>
     * <p>
     * If the current user is not logged in or if {@code otherUserId} is not provided,
     * the activity will display a Toast message and finish.
     * </p>
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState(Bundle)}.
     *                           <b><i>Note: Otherwise it is null.</i></b>
     */
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

        // Calculate chatId consistently based on user UIDs
        if (currentUserId.compareTo(otherUserId) < 0) {
            chatId = currentUserId + "_" + otherUserId;
        } else {
            chatId = otherUserId + "_" + currentUserId;
        }

        chatRef = FirebaseDatabase.getInstance().getReference().child("Chats").child(chatId);

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messageList);
        chatListView.setAdapter(messageAdapter);

        // Load existing messages
        loadMessages();

        // Set up listener for sending a new message
        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                sendMessage(text);
                etMessage.setText("");
            }
        });
    }

    /**
     * Loads all existing messages for the current chat from Firebase Realtime Database.
     * <p>
     * Attaches a {@link ValueEventListener} to the chat's database reference ({@code chatRef}).
     * When data changes (e.g., new messages arrive or existing messages are modified/deleted),
     * it clears the local message list, repopulates it with the messages from the snapshot,
     * notifies the {@link MessageAdapter} to refresh the UI, and scrolls the ListView to the
     * latest message.
     * </p>
     * <p>
     * If there's an error loading messages, a Toast is displayed.
     * </p>
     */
    private void loadMessages() {
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot msgSnap : snapshot.getChildren()) {
                    Message msg = msgSnap.getValue(Message.class);
                    if (msg != null) {
                        messageList.add(msg);
                    }
                }
                messageAdapter.notifyDataSetChanged();
                chatListView.setSelection(messageList.size() - 1); // scroll to bottom
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, "Failed to load messages: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Sends a new message to the Firebase Realtime Database.
     * <p>
     * Creates a {@link Message} object containing the sender's ID (current user),
     * receiver's ID, the message text, and the current timestamp.
     * It then pushes this new message object to the chat's location in Firebase
     * under a unique message ID generated by {@code push().getKey()}.
     * </p>
     *
     * @param text The content of the message to be sent. Must not be empty.
     */
    private void sendMessage(String text) {
        if (currentUser == null) {
            Toast.makeText(this, "Cannot send message. User not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }
        String senderId = currentUser.getUid();
        Message msg = new Message(senderId, receiverId, text, System.currentTimeMillis());

        // Add the message to Firebase
        String messageId = chatRef.push().getKey();
        if (messageId != null) {
            chatRef.child(messageId).setValue(msg)
                    .addOnSuccessListener(aVoid -> {
                        // Optional: Log success or perform UI updates upon successful send
                        // Log.d("ChatActivity", "Message sent successfully!");
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ChatActivity.this, "Failed to send message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        // Optional: Implement retry logic or inform user more explicitly
                    });
        } else {
            Toast.makeText(ChatActivity.this, "Failed to generate message ID.", Toast.LENGTH_SHORT).show();
        }
    }
}