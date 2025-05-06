package com.example.roniproject.Obj;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.roniproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    public static final String CHANNEL_ID = "chat_channel";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d("FCM", "Token: " + token);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        String uid = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;

        if (uid != null) {
            FirebaseDatabase.getInstance().getReference("Users")
                    .child(uid)
                    .child("fcmToken")
                    .setValue(token);
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (remoteMessage.getData().size() > 0) {
            String senderName = remoteMessage.getData().get("senderName");
            String city = remoteMessage.getData().get("city");
            String message = remoteMessage.getData().get("message");

            sendNotification(senderName, city, message);
        }
    }

    private void sendNotification(String senderName, String city, String message) {
        String title = "הודעה חדשה מ-" + senderName + " (" + city + ")";
        String content = message;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_chat) // ודא שקיים
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (manager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        "Chat Messages",
                        NotificationManager.IMPORTANCE_HIGH
                );
                channel.setDescription("התראות עבור הודעות צ'אט");
                manager.createNotificationChannel(channel);
            }

            manager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }
}


