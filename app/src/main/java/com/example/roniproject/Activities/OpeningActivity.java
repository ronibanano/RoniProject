package com.example.roniproject.Activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.roniproject.R;

/**
 * OpeningActivity serves as the initial screen of the application.
 * <p>
 * This activity presents the user with two choices: to log in or to register.
 * It contains two buttons, one for navigating to the
 * {@link com.example.roniproject.Activities.LoginActivity} and another for navigating
 * to the {@link com.example.roniproject.Activities.RegisertActivity}.
 * Additionally, upon creation, this activity clears a specific SharedPreferences flag
 * named "serviceStarted", likely to reset a state related to a background service
 * each time the app is freshly opened through this activity.
 *
 * @see androidx.appcompat.app.AppCompatActivity
 * @see com.example.roniproject.Activities.LoginActivity
 * @see com.example.roniproject.Activities.RegisertActivity
 */
public class OpeningActivity extends AppCompatActivity {

    /**
     * Button that, when clicked, navigates the user to the LoginActivity.
     */
    Button btnChooseLogin;
    /**
     * Button that, when clicked, navigates the user to the RegisertActivity.
     */
    Button btnChooseRegister;
    /**
     * Intent used for navigating to other activities.
     * This field is reused for both login and register navigation.
     */
    Intent intent;
    /**
     * Context of this activity, primarily used for creating Intents.
     */
    Context context = this;

    /**
     * Called when the activity is first created.
     * <p>
     * This method initializes the user interface, sets up the layout,
     * and configures the click listeners for the login and register buttons.
     * It also resets the "serviceStarted" flag in SharedPreferences to ensure
     * a clean state for a related service when the application is started.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState(Bundle)}.
     *     <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_opening);

        // Reset the "serviceStarted" flag in SharedPreferences.
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        prefs.edit().remove("serviceStarted").apply();


        btnChooseLogin = findViewById(R.id.btnChooseLogin);
        btnChooseRegister = findViewById(R.id.btnChooseRegister);

        // Set a click listener for the login button.
        // When clicked, it creates an Intent to start LoginActivity.
        btnChooseLogin.setOnClickListener(v -> {
            intent = new Intent(context, LoginActivity.class);
            startActivity(intent);
        });

        // Set a click listener for the register button.
        // When clicked, it creates an Intent to start RegisertActivity.
        btnChooseRegister.setOnClickListener(v -> {
            intent = new Intent(context, RegisertActivity.class);
            startActivity(intent);
        });
    }
}