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

/**
 * A background {@link Service} that listens for new chat messages in Firebase Realtime Database
 * and displays notifications to the user.
 * <p>
 * This service runs as a foreground service to ensure it continues operating even when the
 * application is in the background. It monitors the "Chats" node in Firebase for new messages
 * directed to the {@code currentUserId} (passed via an Intent when the service starts).
 * </p>
 * <p>
 * Key functionalities:
 * <ul>
 *     <li><b>Foreground Service:</b> Starts in the foreground with a persistent notification
 *         indicating its operation (e.g., "מאזין להודעות חדשות...").</li>
 *     <li><b>Message Listening:</b> Attaches a {@link com.google.firebase.database.ValueEventListener} to the "Chats" node in Firebase
 *         to detect new messages.</li>
 *     <li><b>Notification Display:</b> When a new message is received for the {@code currentUserId} (and
 *         it's newer than the {@code lastNotifiedTimestamp}), it fetches the sender's details ({@code fullName} and {@code city})
 *         from the "Users" node and constructs a notification.</li>
 *     <li><b>Notification Click Action:</b> Tapping the notification opens the {@link com.example.roniproject.Activities.ChatActivity}
 *         for the specific chat.</li>
 *     <li><b>Timestamp Tracking:</b> Uses {@link android.content.SharedPreferences} to store the {@code lastNotifiedTimestamp} of the
 *         last message for which a notification was shown. This prevents re-notifying for old messages
 *         when the service restarts or the listener re-evaluates.</li>
 *     <li><b>Notification Channel:</b> Creates a dedicated notification channel ({@code CHANNEL_ID})
 *         on Android Oreo (API 26) and above for chat message notifications.</li>
 * </ul>
 * </p>
 * <p>
 * The service expects the current user's ID to be passed as an extra in the start Intent with the
 * key "userId".
 * </p>
 * <p>
 * Firebase structure dependencies (based on field names used in this service):
 * <ul>
 *     <li>"Chats/{chatId}/{messageId}": Structure for messages, where each message contains
 *         {@code receiverId}, {@code senderId}, {@code text}, and {@code timestamp}.</li>
 *     <li>"Users/{userId}": Structure for user details, containing at least {@code fullName} and {@code city}.</li>
 * </ul>
 * </p>
 *
 */
public class ChatNotificationService extends Service {

    private static final String TAG = "ChatNotificationService";
    private static final String CHANNEL_ID = "chat_messages_channel";
    private static final int SERVICE_NOTIFICATION_ID = 1;

    private ValueEventListener messagesListener;
    private DatabaseReference messagesRef;
    private String currentUserId;
    private long lastNotifiedTimestamp = 0;

    /**
     * Called by the system when the service is first created.
     * <p>
     * Initializes {@link android.content.SharedPreferences} to load the {@code lastNotifiedTimestamp},
     * creates the notification channel for chat messages, and starts the service
     * in the foreground with an initial notification.
     * </p>
     */
    @SuppressLint("ForegroundServiceType")
    @Override
    public void onCreate() {
        super.onCreate();

        lastNotifiedTimestamp = getSharedPreferences("AppPrefs", MODE_PRIVATE)
                .getLong("lastNotifiedTimestamp", 0);

        createNotificationChannel();
        startForeground(SERVICE_NOTIFICATION_ID, createServiceNotification("מאזין להודעות חדשות..."));
    }

    /**
     * Called by the system every time a client starts the service using {@link android.content.Context#startService(Intent)}.
     * <p>
     * Retrieves the {@code userId} from the incoming {@link Intent}. If a valid ID is provided,
     * it initiates listening for new messages for that user via {@link #listenForNewMessages(String)}.
     * </p>
     *
     * @param intent The Intent supplied to {@link android.content.Context#startService(Intent)}.
     *               This intent should contain an extra "userId" with the {@code currentUserId}.
     * @param flags Additional data about this start request.
     * @param startId A unique integer representing this specific start request.
     * @return The return value indicates what semantics the system should use for the service's
     *         current started state. {@link Service#START_STICKY} is used here.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        currentUserId = intent.getStringExtra("userId");
        if (currentUserId != null) {
            listenForNewMessages(currentUserId);
        } else {
            Log.w(TAG, "No user ID provided to service");
            // Consider stopping the service if userId is crucial and missing.
            // stopSelf();
        }

        return START_STICKY;
    }

    /**
     * Sets up a {@link com.google.firebase.database.ValueEventListener} on the "Chats" node in Firebase Realtime Database
     * to listen for new messages for the {@code currentUserId}.
     * <p>
     * It iterates through chat snapshots. For each message, it checks if the {@code receiverId}
     * matches the {@code currentUserId}, the {@code senderId} is different, and the {@code timestamp}
     * is greater than {@code lastNotifiedTimestamp}. If conditions are met, it fetches the sender's
     * {@code fullName} and {@code city} to create and send a notification. The {@code lastNotifiedTimestamp}
     * is then updated in memory and {@link android.content.SharedPreferences}.
     * </p>
     *
     * @param userId The ID of the user for whom to listen for new messages (this is the {@code currentUserId}).
     */
    private void listenForNewMessages(String userId) {
        // If a listener already exists, remove it before adding a new one
        if (messagesRef != null && messagesListener != null) {
            messagesRef.removeEventListener(messagesListener);
        }

        messagesRef = FirebaseDatabase.getInstance().getReference("Chats");

        messagesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot chatSnapshot : snapshot.getChildren()) {
                    for (DataSnapshot messageSnapshot : chatSnapshot.getChildren()) {
                        // Directly access fields as strings/long from the snapshot
                        // based on the structure observed in the listenForNewMessages method.
                        String receiverId = messageSnapshot.child("receiverId").getValue(String.class);
                        String senderId = messageSnapshot.child("senderId").getValue(String.class);
                        String messageText = messageSnapshot.child("text").getValue(String.class);
                        Long timestamp = messageSnapshot.child("timestamp").getValue(Long.class);

                        if (receiverId != null && receiverId.equals(currentUserId) &&
                                senderId != null && !senderId.equals(currentUserId) &&
                                timestamp != null && timestamp > lastNotifiedTimestamp) {

                            lastNotifiedTimestamp = timestamp;

                            getSharedPreferences("AppPrefs", MODE_PRIVATE)
                                    .edit()
                                    .putLong("lastNotifiedTimestamp", timestamp)
                                    .apply();

                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(senderId)
                                    .get()
                                    .addOnSuccessListener(userSnapshot -> {
                                        // Field names used here: "fullName", "city"
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

    /**
     * Builds and displays a system notification for a new chat message.
     * <p>
     * The notification, when tapped, will open {@link com.example.roniproject.Activities.ChatActivity}
     * corresponding to the {@code chatId} and {@code otherUserId}.
     * </p>
     *
     * @param title       The title of the notification (sender's name/info).
     * @param message     The content text of the notification (message preview).
     * @param chatId      The ID of the chat this message belongs to.
     * @param otherUserId The ID of the other user in the chat (the sender of this message).
     */
    private void sendNotification(String title, String message, String chatId, String otherUserId) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("chatId", chatId);
        intent.putExtra("otherUserId", otherUserId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                (int) System.currentTimeMillis(), // Unique request code for each PendingIntent
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_chat) // Ensure this drawable exists
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build();

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify((int) System.currentTimeMillis(), notification); // Unique ID for each notification
        }
    }


    /**
     * Creates a {@link android.app.Notification} object for the foreground service itself.
     * <p>
     * This notification is shown persistently while the service is running in the foreground.
     * </p>
     *
     * @param contentText The text to display in the service's notification.
     * @return The configured {@link android.app.Notification}.
     */
    private Notification createServiceNotification(String contentText) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("BookSwap - שירות הודעות")
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_chat) // Ensure this drawable exists
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true) // Makes the notification non-dismissible by swipe
                .build();
    }

    /**
     * Creates a notification channel for chat messages on Android Oreo (API 26) and above.
     * <p>
     * This method creates a {@link android.app.NotificationChannel} with high importance.
     * This is required for notifications to appear on newer Android versions.
     * </p>
     */
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

    /**
     * Called by the system to notify a Service that it is no longer used and is being removed.
     * <p>
     * Cleans up resources by removing the Firebase {@link com.google.firebase.database.ValueEventListener}
     * ({@code messagesListener}) from {@code messagesRef}.
     * </p>
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (messagesRef != null && messagesListener != null) {
            messagesRef.removeEventListener(messagesListener);
        }
    }

    /**
     * Return the communication channel to the service.
     * <p>
     * This service does not provide binding, so it returns {@code null}.
     * </p>
     *
     * @param intent The Intent that was used to bind to this service.
     * @return Return an {@link android.os.IBinder} through which clients can call on to the
     *         service. Return {@code null} if clients cannot bind to the service.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}