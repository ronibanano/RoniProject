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
import com.example.roniproject.Obj.User;
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

/**
 * RegisertActivity provides the user interface and logic for new user registration.
 * <p>
 * This activity allows users to create a new account by providing their email,
 * password, full name, and city. It uses Firebase Authentication to create the user
 * and Firebase Realtime Database to store additional user profile information.
 * <p>
 * Input validation is performed to ensure that all fields are filled and the password
 * meets a minimum length requirement. Upon successful registration, the user is
 * automatically logged in and navigated to the {@link com.example.roniproject.HomeScreen}.
 * If registration fails or inputs are invalid, an error message is displayed
 * to the user via an AlertDialog. Users can also navigate to the
 * {@link com.example.roniproject.Activities.LoginActivity} if they already have an account.
 *
 * @see androidx.appcompat.app.AppCompatActivity
 * @see com.google.firebase.auth.FirebaseAuth
 * @see com.google.firebase.database.FirebaseDatabase
 * @see com.example.roniproject.Obj.User
 * @see com.example.roniproject.HomeScreen
 * @see com.example.roniproject.Activities.LoginActivity
 */
public class RegisertActivity extends AppCompatActivity {

    /**
     * EditText field for the user to input their email address.
     */
    EditText etEmail;
    /**
     * EditText field for the user to input their desired password.
     */
    EditText etPassword;
    /**
     * EditText field for the user to input their full name.
     */
    EditText etFullName;
    /**
     * EditText field for the user to input their city.
     */
    EditText etCity;
    /**
     * TextView that, when clicked, navigates the user to the login screen.
     */
    TextView tvClickLogin;
    /**
     * Button that triggers the registration process when clicked.
     */
    Button btnRegister;
    /**
     * ProgressBar shown to indicate ongoing operations, such as network requests during registration.
     */
    ProgressBar progressBar;
    /**
     * AlertDialog.Builder used to construct and display alert dialogs for error or validation messages.
     */
    AlertDialog.Builder adb;
    /**
     * FirebaseAuth instance used for handling user account creation with Firebase.
     */
    FirebaseAuth mAuth;
    /**
     * Context of this activity, used for creating Intents and Dialogs.
     */
    Context context = this;
    /**
     * DatabaseReference pointing to the "Users" node in Firebase Realtime Database,
     * used for storing user profile information.
     */
    private DatabaseReference databaseReference;

    /**
     * Called when the activity is first created.
     * <p>
     * Initializes the UI components, Firebase services (Authentication and Database),
     * and sets up click listeners for the "Login Now" text view and the register button.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState(android.os.Bundle)}.
     *     <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regiser);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etFullName = findViewById(R.id.etFullName);
        etCity = findViewById(R.id.etCity);
        tvClickLogin = findViewById(R.id.loginNow);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);
        adb = new AlertDialog.Builder(this);
        mAuth = FirebaseAuth.getInstance();

        databaseReference = FirebaseDatabase.getInstance().getReference("Users");

        // Sets a click listener on the "Login Now" TextView to navigate to LoginActivity.
        tvClickLogin.setOnClickListener(new View.OnClickListener() {
            /**
             * Called when the "Login Now" TextView is clicked.
             * Starts the {@link LoginActivity}.
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(context, LoginActivity.class);
                startActivity(intent);
            }
        });

        // Sets a click listener on the register button to attempt new user creation.
        btnRegister.setOnClickListener(new View.OnClickListener() {
            /**
             * Called when the register button (btnRegister) is clicked.
             * <p>
             * Retrieves user input for email, password, full name, and city.
             * Validates the input: checks for empty fields and minimum password length.
             * If validation fails, an error dialog is shown.
             * Otherwise, it attempts to create a new user with Firebase Authentication.
             * If Firebase user creation is successful, it then calls {@link #registerUser(String, String, String, String)}
             * to save additional user details to the Firebase Realtime Database.
             * Finally, it navigates to the {@link HomeScreen}.
             * If any step fails, an appropriate error dialog is displayed.
             * The ProgressBar is shown during the registration process and hidden afterwards.
             *
             * @param v The view that was clicked.
             */
            @Override
            public void onClick(View v) {
                String email, password, fullName, city;
                progressBar.setVisibility(View.VISIBLE);
                email = String.valueOf(etEmail.getText());
                password = String.valueOf(etPassword.getText());
                fullName = String.valueOf(etFullName.getText());
                city = String.valueOf(etCity.getText());

                if (email.isEmpty() || password.isEmpty() || password.length() < 6 || fullName.isEmpty() || city.isEmpty()) {
                    adb.setMessage("Incorrect details");
                    adb.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                    adb.create().show();
                    return; // Return early as validation failed
                }

                // ProgressBar visibility is set again here, which is fine but could be optimized.
                // It was already set visible before the validation block.
                progressBar.setVisibility(View.VISIBLE);

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            /**
                             * Called when the Firebase createUserWithEmailAndPassword task is complete.
                             *
                             * @param task The task that has completed. If successful, the user is created.
                             *             If not successful, {@link com.google.android.gms.tasks.Task#getException()} will provide details.
                             */
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);

                                if (task.isSuccessful()) {
                                    Log.d("Register", "User created successfully!");
                                    FirebaseUser user = mAuth.getCurrentUser();

                                    if (user != null) {
                                        registerUser(user.getUid(), email, fullName, city);
                                    }

                                    Intent intent = new Intent(context, HomeScreen.class);
                                    startActivity(intent);
                                    finish(); // Finish RegisertActivity so user cannot navigate back to it
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
    }

    /**
     * Saves additional user information to the Firebase Realtime Database after successful
     * Firebase Authentication account creation.
     * <p>
     * Creates a {@link User} object with the provided details and saves it under the
     * "Users" node in the database, using the user's unique ID (UID) as the key.
     * Logs success or failure of this operation. Displays a Toast message on failure.
     *
     * @param userId The unique ID of the Firebase user.
     * @param email The email address of the user.
     * @param fullName The full name of the user.
     * @param city The city of the user.
     */
    private void registerUser(String userId, String email, String fullName, String city) {
        // This line re-initializes databaseReference, which might not be necessary
        // if it's already initialized in onCreate and remains valid.
        // However, it ensures it points to the correct "Users" path.
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        User user = new User(userId, email, fullName, city);

        databaseReference.child(userId).setValue(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    /**
                     * Called when the user data is successfully saved to the database.
                     * @param unused This parameter is not used.
                     */
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("Register", "User data saved successfully!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    /**
                     * Called when saving user data to the database fails.
                     * @param e The exception that caused the failure.
                     */
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Register", "Failed to save user data", e);
                        Toast.makeText(context, "Failed to save user data. Please try again.", Toast.LENGTH_LONG).show();
                    }
                });
    }
}
