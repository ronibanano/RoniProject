package com.example.roniproject.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.roniproject.HomeScreen;
import com.example.roniproject.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    EditText etEmail, etPassword;
    Button btLogin;
    TextView tvClickRegister;
    ProgressBar progressBar;
    Context context = this;
    AlertDialog.Builder adb;
    FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tvClickRegister = findViewById(R.id.registerNow);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);
        adb = new AlertDialog.Builder(this);
        mAuth = FirebaseAuth.getInstance();

        tvClickRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, RegisertActivity.class);
                startActivity(intent);
            }
        });


        btLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email, password;
                progressBar.setVisibility(View.VISIBLE);
                email = etEmail.getText().toString().trim();
                password = etPassword.getText().toString().trim();
                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    adb.setMessage("Incorrect email or password");
                    adb.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                    adb.create().show();
                }
                else {
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // המשתמש מחובר
                                        Log.d("Login", "signInWithEmail:success");
                                        progressBar.setVisibility(View.GONE);
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        Intent intent = new Intent(context, HomeScreen.class);
                                        startActivity(intent);
                                    } else {
                                        // המשתמש לא מחובר
                                        Log.w("Login", "signInWithEmail:failure", task.getException());
                                        adb.setMessage("Incorrect email or password");
                                        adb.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                                progressBar.setVisibility(View.GONE);
                                            }
                                        });
                                        adb.create().show();
                                    }
                                }
                            });

                }
            }
        });
    }
}