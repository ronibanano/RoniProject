package com.example.roniproject;


import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;


import com.example.roniproject.Frag.ChatFragment;
import com.example.roniproject.Frag.HomeFragment;
import com.example.roniproject.Frag.ProfileFragment;
import com.example.roniproject.Frag.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeScreen extends AppCompatActivity {
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        bottomNavigationView=findViewById(R.id.bottom_navigation);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerView, new HomeFragment()).commit();

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.profileFragment) {
                selectedFragment = new ProfileFragment();

            } else if (item.getItemId() == R.id.homeFragment) {
                selectedFragment = new HomeFragment();
            } else if (item.getItemId() == R.id.searchFragment) {
                selectedFragment = new SearchFragment();
            } else if (item.getItemId() == R.id.chatFragment) {
                selectedFragment = new ChatFragment();
            }


            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainerView, selectedFragment)
                        .commit();
            }
            return true;
        });
    }
}