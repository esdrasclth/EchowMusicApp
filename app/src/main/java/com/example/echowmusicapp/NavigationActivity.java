package com.example.echowmusicapp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.echowmusicapp.Screen.Group.GroupFragment;
import com.example.echowmusicapp.Screen.Home.HomeFragment;
import com.example.echowmusicapp.Screen.Setting.SettingFragment;
import com.example.echowmusicapp.Screen.Upload.UploadFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NavigationActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_navigation);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        bottomNav.setOnNavigationItemSelectedListener(navListener);

        //I added this if statement to keep the selected fragment when rotating the device
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new HomeFragment()).commit();
        }
    }

    BottomNavigationView.OnNavigationItemSelectedListener navListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            Fragment selectedFragment = null;

            if (menuItem.getItemId() == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (menuItem.getItemId() == R.id.nav_upload) {
                selectedFragment = new UploadFragment();
            } else if (menuItem.getItemId() == R.id.nav_group) {
                selectedFragment = new GroupFragment();
            } else if (menuItem.getItemId() == R.id.nav_setting) {
                selectedFragment = new SettingFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        selectedFragment).commit();
                return true;
            }

            return false;
        }
    };
}