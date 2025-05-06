package com.example.roniproject.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.roniproject.HomeScreen;
import com.example.roniproject.Obj.Users;
import com.example.roniproject.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisertActivity extends AppCompatActivity {

    EditText etEmail, etPassword, etFullName, etCity, etPhoneNumber;
    TextView tvClickLogin;
    Button btnRegister;
    ProgressBar progressBar;
    AlertDialog.Builder adb;
    FirebaseAuth mAuth;
    Context context = this;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regiser);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etFullName = findViewById(R.id.etFullName);
        etCity = findViewById(R.id.etCity);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        tvClickLogin = findViewById(R.id.loginNow);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);
        adb = new AlertDialog.Builder(this);
        mAuth = FirebaseAuth.getInstance();

        databaseReference = FirebaseDatabase.getInstance().getReference("Users");


        tvClickLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, LoginActivity.class);
                startActivity(intent);
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email, password, fullName, city, phoneNumber;
                progressBar.setVisibility(View.VISIBLE);
                email = String.valueOf(etEmail.getText());
                password = String.valueOf(etPassword.getText());
                fullName = String.valueOf(etFullName.getText());
                city = String.valueOf(etCity.getText());
                phoneNumber = String.valueOf(etPhoneNumber.getText());

                if (email.isEmpty() || password.isEmpty() || password.length() < 6 || fullName.isEmpty() || city.isEmpty()|| phoneNumber.isEmpty()|| phoneNumber.length()!=10) {
                    adb.setMessage("Incorrect details");
                    adb.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                    adb.create().show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                // יצירת משתמש חדש בפיירבייס
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);

                                if (task.isSuccessful()) {
                                    Log.d("Register", "User created successfully!");
                                    FirebaseUser user = mAuth.getCurrentUser();

                                    if (user != null) {
                                        registerUser(user.getUid(), email, fullName, city, phoneNumber);
                                    }

                                    Intent intent = new Intent(context, HomeScreen.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Log.w("Register", "User creation failed", task.getException());
                                    adb.setMessage("Registration failed: " + task.getException().getMessage());
                                    adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                                    adb.create().show();
                                }
                            }
                        });

            }
        });

//        btnRegister.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String email, password, fullName, city;
//                progressBar.setVisibility(View.VISIBLE);
//                email = String.valueOf(etEmail.getText());
//                password = String.valueOf(etPassword.getText());
//                fullName = String.valueOf(etFullName.getText());
//                city = String.valueOf(etCity.getText());
//
//                if (email.isEmpty() || password.isEmpty() || password.length() < 6 || fullName.isEmpty() || city.isEmpty()) {
//                    adb.setMessage("Incorrect email or password");
//                    adb.setPositiveButton("ok", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.dismiss();
//                            progressBar.setVisibility(View.GONE);
//                        }
//                    });
//                    adb.create().show();
//                } else {
//                    // התנתקות מהמשתמש הנוכחי
//                    mAuth.signOut();
//
//                    mAuth.createUserWithEmailAndPassword(email, password)
//                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                                @Override
//                                public void onComplete(@NonNull Task<AuthResult> task) {
//                                    progressBar.setVisibility(View.GONE);
//                                    if (task.isSuccessful()) {
//                                        Log.d("Register", "createUserWithEmail:success");
//
//                                        // התחברות מפורשת למשתמש החדש
//                                        mAuth.signInWithEmailAndPassword(email, password)
//                                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                                                    @Override
//                                                    public void onComplete(@NonNull Task<AuthResult> signInTask) {
//                                                        if (signInTask.isSuccessful()) {
//                                                            FirebaseUser user = mAuth.getCurrentUser();
//                                                            if (user != null) {
//                                                                registerUser(email, fullName, city); // קריאה לפונקציה לשמירת הנתונים
//                                                            }
//                                                            Intent intent = new Intent(context, HomeScreen.class);
//                                                            startActivity(intent);
//                                                        } else {
//                                                            Log.w("Register", "signInWithEmail:failure", signInTask.getException());
//                                                            Toast.makeText(context, "Failed to log in the new user.", Toast.LENGTH_LONG).show();
//                                                        }
//                                                    }
//                                                });
//
//                                    } else {
//                                        Log.w("Register", "createUserWithEmail:failure", task.getException());
//                                        adb.setMessage("Registration failed: " + task.getException().getMessage());
//                                        adb.setPositiveButton("ok", new DialogInterface.OnClickListener() {
//                                            @Override
//                                            public void onClick(DialogInterface dialog, int which) {
//                                                dialog.dismiss();
//                                            }
//                                        });
//                                        adb.create().show();
//                                    }
//                                }
//                            });
//                }
//            }
//        });
//
    }

    private void registerUser(String userId,String email, String fullName, String city, String phoneNumber) {
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        Users user = new Users(email, fullName, city, phoneNumber);

        databaseReference.child(userId).setValue(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("Register", "User data saved successfully!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Register", "Failed to save user data", e);
                        Toast.makeText(context, "Failed to save user data. Please try again.", Toast.LENGTH_LONG).show();
                    }
                });

    }
}
