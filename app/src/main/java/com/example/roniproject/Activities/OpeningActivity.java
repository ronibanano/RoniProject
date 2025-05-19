package com.example.roniproject.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.roniproject.R;

public class OpeningActivity extends AppCompatActivity {

    Button btnChooseLogin, btnChooseRegister;
    Intent intent;
    Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opening);

        // איפוס הדגל של serviceStarted כשנכנסים מחדש לאפליקציה
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        prefs.edit().remove("serviceStarted").apply();

//        // יצירת Notification Channel לאנדרואיד 8+
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            NotificationChannel channel = new NotificationChannel(
//                    "chat_messages", // זהה למה שתשתמש בהתראה עצמה
//                    "הודעות צ'אט",
//                    NotificationManager.IMPORTANCE_HIGH
//            );
//            channel.setDescription("התראות עבור הודעות חדשות בצ'אט");
//
//            NotificationManager manager = getSystemService(NotificationManager.class);
//            if (manager != null) {
//                manager.createNotificationChannel(channel);
//            }
//        }
//
//        FirebaseMessaging.getInstance().getToken()
//                .addOnCompleteListener(task -> {
//                    if (!task.isSuccessful()) {
//                        Log.w("FCM", "Fetching FCM registration token failed", task.getException());
//                        return;
//                    }
//
//                    String token = task.getResult();
//                    Log.d("FCM", "Token: " + token);
//
//                    FirebaseAuth auth = FirebaseAuth.getInstance();
//                    if (auth.getCurrentUser() != null) {
//                        String uid = auth.getCurrentUser().getUid();
//                        FirebaseDatabase.getInstance().getReference("Users").child(uid).child("fcmToken").setValue(token);
//                    }
//                });

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