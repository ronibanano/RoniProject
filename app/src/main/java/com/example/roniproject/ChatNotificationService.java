package com.example.roniproject;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.roniproject.Activities.ChatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ChatNotificationService extends Service {

    private static final String TAG = "ChatNotificationService";
    private static final String CHANNEL_ID = "chat_messages_channel";
    private static final int SERVICE_NOTIFICATION_ID = 1;

    private ValueEventListener messagesListener;
    private DatabaseReference messagesRef;
    private String currentUserId;
    private long lastNotifiedTimestamp = 0;

    @SuppressLint("ForegroundServiceType")
    @Override
    public void onCreate() {
        super.onCreate();

        // טען את זמן ההתראה האחרון מ־SharedPreferences
        lastNotifiedTimestamp = getSharedPreferences("AppPrefs", MODE_PRIVATE)
                .getLong("lastNotifiedTimestamp", 0);

        createNotificationChannel();
        startForeground(SERVICE_NOTIFICATION_ID, createServiceNotification("מאזין להודעות חדשות..."));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        currentUserId = intent.getStringExtra("userId");
        if (currentUserId != null) {
            listenForNewMessages(currentUserId);
        } else {
            Log.w(TAG, "No user ID provided to service");
        }

        return START_STICKY;
    }

    private void listenForNewMessages(String userId) {
        messagesRef = FirebaseDatabase.getInstance().getReference("Chats");

        messagesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot messageSnapshot : chatSnapshot.getChildren()) {
                        String receiverId = messageSnapshot.child("receiverId").getValue(String.class);
                        String senderId = messageSnapshot.child("senderId").getValue(String.class);
                        String messageText = messageSnapshot.child("text").getValue(String.class);
                        Long timestamp = messageSnapshot.child("timestamp").getValue(Long.class);

                        if (receiverId != null && receiverId.equals(currentUserId) &&
                                senderId != null && !senderId.equals(currentUserId) &&
                                timestamp != null && timestamp > lastNotifiedTimestamp) {

                            lastNotifiedTimestamp = timestamp;

                            // שמור את זמן ההתראה האחרון ב־SharedPreferences
                            getSharedPreferences("AppPrefs", MODE_PRIVATE)
                                    .edit()
                                    .putLong("lastNotifiedTimestamp", timestamp)
                                    .apply();

                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(senderId)
                                    .get()
                                    .addOnSuccessListener(userSnapshot -> {
                                        String senderName = userSnapshot.child("fullName").getValue(String.class);
                                        String senderCity = userSnapshot.child("city").getValue(String.class);

                                        String title = (senderName != null ? senderName : "משתמש") +
                                                (senderCity != null ? " (" + senderCity + ")" : "");
                                        String message = messageText != null ? messageText : "הודעה חדשה בצ'אט";

                                        sendNotification(title, message, chatSnapshot.getKey(), senderId);
                                    })
                                    .addOnFailureListener(e -> Log.e(TAG, "Failed to get sender user data", e));
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to listen for messages: " + error.getMessage());
            }
        };

        messagesRef.addValueEventListener(messagesListener);
    }

    private void sendNotification(String title, String message, String chatId, String otherUserId) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("chatId", chatId);
        intent.putExtra("otherUserId", otherUserId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_chat)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent) // 👈 לחיצה על ההתראה תוביל לצ'אט
                .build();

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify((int) System.currentTimeMillis(), notification);
        }
    }


    private Notification createServiceNotification(String contentText) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("BookSwap - שירות הודעות")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_chat)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "התראות הודעות צ'אט";
            String description = "ערוץ עבור הודעות חדשות בצ'אט";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (messagesRef != null && messagesListener != null) {
            messagesRef.removeEventListener(messagesListener);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
