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

/**
 * LoginActivity provides the user interface and logic for user authentication.
 * <p>
 * This activity allows users to enter their email and password to log into the application.
 * It uses Firebase Authentication to verify credentials. Upon successful login,
 * it navigates the user to the {@link com.example.roniproject.HomeScreen}.
 * If authentication fails, or if input fields are empty, it displays an error message
 * to the user via an AlertDialog. Users can also navigate to
 * {@link com.example.roniproject.Activities.RegisertActivity} to create a new account.
 *
 * @see androidx.appcompat.app.AppCompatActivity
 * @see com.google.firebase.auth.FirebaseAuth
 * @see com.example.roniproject.HomeScreen
 * @see com.example.roniproject.Activities.RegisertActivity
 */
public class LoginActivity extends AppCompatActivity {

    /**
     * EditText field for the user to input their email address.
     */
    EditText etEmail;
    /**
     * EditText field for the user to input their password.
     */
    EditText etPassword;
    /**
     * Button that triggers the login process when clicked.
     */
    Button btLogin;
    /**
     * TextView that, when clicked, navigates the user to the registration screen.
     */
    TextView tvClickRegister;
    /**
     * ProgressBar shown to indicate ongoing operations, such as network requests during login.
     */
    ProgressBar progressBar;
    /**
     * Context of this activity, used for creating Intents and Dialogs.
     */
    Context context = this;
    /**
     * AlertDialog.Builder used to construct and display alert dialogs for error messages.
     */
    AlertDialog.Builder adb;
    /**
     * FirebaseAuth instance used for handling user authentication with Firebase.
     */
    FirebaseAuth mAuth;


    /**
     * Called when the activity is first created.
     * <p>
     * Initializes the UI components, sets up click listeners, and initializes
     * Firebase Authentication.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState(Bundle)}.
     *     <b><i>Note: Otherwise it is null.</i></b>
     */
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

        // Sets a click listener on the "Register Now" TextView to navigate to RegisertActivity.
        tvClickRegister.setOnClickListener(new View.OnClickListener() {
            /**
             * Called when the "Register Now" TextView is clicked.
             * Starts the {@link RegisertActivity}.
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, RegisertActivity.class);
                startActivity(intent);
            }
        });


        // Sets a click listener on the login button to attempt user authentication.
        btLogin.setOnClickListener(new View.OnClickListener() {
            /**
             * Called when the login button (btLogin) is clicked.
             * <p>
             * Retrieves the email and password from the EditText fields.
             * Validates that the fields are not empty. If they are, an error dialog is shown.
             * Otherwise, it attempts to sign in the user with Firebase Authentication.
             * On successful sign-in, it navigates to {@link HomeScreen}.
             * On failure, it shows an error dialog.
             * The ProgressBar is shown during the authentication attempt and hidden afterwards.
             *
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                String email, password;
                progressBar.setVisibility(View.VISIBLE);
                email = etEmail.getText().toString().trim();
                password = etPassword.getText().toString().trim();

                // Check if email or password fields are empty
                if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                    adb.setMessage("Incorrect email or password"); // Consider more specific message for empty fields
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
                    // Attempt to sign in with Firebase Authentication
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                /**
                                 * Called when the Firebase signInWithEmailAndPassword task is complete.
                                 *
                                 * @param task The task that has completed. If successful, the user is signed in.
                                 *             If not successful, {@link Task#getException()} will provide details.
                                 */
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    progressBar.setVisibility(View.GONE);
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d("Login", "signInWithEmail:success");
                                        FirebaseUser user = mAuth.getCurrentUser();
                                        Intent intent = new Intent(context, HomeScreen.class);
                                        startActivity(intent);
                                        // Optional: finish(); // to prevent user from going back to login screen
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w("Login", "signInWithEmail:failure", task.getException());
                                        adb.setMessage("Incorrect email or password");
                                        adb.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                                // progressBar is already GONE here, no need to set again.
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