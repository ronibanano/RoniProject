package com.example.roniproject.Activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.roniproject.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity {

    Button btnChooseLogin, btnChooseRegister;
    Intent intent;
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // יצירת Notification Channel לאנדרואיד 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "chat_messages", // זהה למה שתשתמש בהתראה עצמה
                    "הודעות צ'אט",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("התראות עבור הודעות חדשות בצ'אט");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM", "Fetching FCM registration token failed", task.getException());
                        return;
                    }

                    String token = task.getResult();
                    Log.d("FCM", "Token: " + token);

                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    if (auth.getCurrentUser() != null) {
                        String uid = auth.getCurrentUser().getUid();
                        FirebaseDatabase.getInstance().getReference("Users").child(uid).child("fcmToken").setValue(token);
                    }
                });

        btnChooseLogin = findViewById(R.id.btnChooseLogin);
        btnChooseRegister = findViewById(R.id.btnChooseRegister);

        btnChooseLogin.setOnClickListener(v -> {
            intent = new Intent(context, LoginActivity.class);
            startActivity(intent);
        });

        btnChooseRegister.setOnClickListener(v -> {
            intent = new Intent(context, RegisertActivity.class);
            startActivity(intent);
        });
    }

}