package com.example.chatt_application.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatt_application.R;
import com.example.chatt_application.utilites.Constants;
import com.example.chatt_application.utilites.PreferenceManager;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash);

        // Initialize PreferenceManager
        PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());

        // Show splash screen for 2 seconds, then check login status
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent;
            if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, SignInActivity.class);
            }
            startActivity(intent);
            finish(); // Close SplashActivity
        }, 2000); // 2000ms = 2 seconds
    }
}
