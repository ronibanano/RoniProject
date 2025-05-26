package com.example.roniproject;


import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;


import com.example.roniproject.Frag.ChatFragment;
import com.example.roniproject.Frag.HomeFragment;
import com.example.roniproject.Frag.ProfileFragment;
import com.example.roniproject.Frag.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * Main activity for the application's home screen, managing navigation between different
 * functional areas using a {@link com.google.android.material.bottomnavigation.BottomNavigationView}.
 * <p>
 * This activity hosts a {@link androidx.fragment.app.FragmentContainerView} which displays
 * different fragments based on the user's selection in the bottom navigation menu.
 * The default fragment displayed upon creation is {@link com.example.roniproject.Frag.HomeFragment}.
 * </p>
 * <p>
 * It initializes the bottom navigation view and sets up an
 * {@link com.google.android.material.navigation.NavigationBarView.OnItemSelectedListener}
 * to handle transitions between the following fragments:
 * <ul>
 *     <li>{@link com.example.roniproject.Frag.ProfileFragment} (R.id.profileFragment)</li>
 *     <li>{@link com.example.roniproject.Frag.HomeFragment} (R.id.homeFragment)</li>
 *     <li>{@link com.example.roniproject.Frag.SearchFragment} (R.id.searchFragment)</li>
 *     <li>{@link com.example.roniproject.Frag.ChatFragment} (R.id.chatFragment)</li>
 * </ul>
 * Fragment transactions are performed using {@link androidx.fragment.app.FragmentManager#beginTransaction()}
 * to replace the content of the {@code R.id.fragmentContainerView}.
 * </p>
 *
 * @see androidx.appcompat.app.AppCompatActivity
 * @see com.google.android.material.bottomnavigation.BottomNavigationView
 * @see com.example.roniproject.Frag.HomeFragment
 * @see com.example.roniproject.Frag.ProfileFragment
 * @see com.example.roniproject.Frag.SearchFragment
 * @see com.example.roniproject.Frag.ChatFragment
 */
public class HomeScreen extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;

    /**
     * Called when the activity is first created.
     * <p>
     * This method initializes the activity, sets its content view from the
     * {@code R.layout.activity_home_screen} layout resource, and configures the
     * {@link BottomNavigationView}.
     * It sets {@link HomeFragment} as the initial fragment displayed in the
     * {@code R.id.fragmentContainerView}.
     * It also sets up the item selection listener for the bottom navigation view
     * to switch between different fragments.
     * </p>
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState(Bundle)}.
     *                           <b>Note: Otherwise it is null.</b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        bottomNavigationView=findViewById(R.id.bottom_navigation);

        // Set the initial fragment to HomeFragment
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerView, new HomeFragment()).commit();

        // Handle bottom navigation item selections
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            // Determine which fragment to display based on the selected item ID
            if (item.getItemId() == R.id.profileFragment) {
                selectedFragment = new ProfileFragment();
            } else if (item.getItemId() == R.id.homeFragment) {
                selectedFragment = new HomeFragment();
            } else if (item.getItemId() == R.id.searchFragment) {
                selectedFragment = new SearchFragment();
            } else if (item.getItemId() == R.id.chatFragment) {
                selectedFragment = new ChatFragment();
            }

            // If a fragment was selected, replace the current fragment in the container
            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainerView, selectedFragment)
                        .commit();
            }
            return true; // Return true to display the item as the selected item
        });
    }
}