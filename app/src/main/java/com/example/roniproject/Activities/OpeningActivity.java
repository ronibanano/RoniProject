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