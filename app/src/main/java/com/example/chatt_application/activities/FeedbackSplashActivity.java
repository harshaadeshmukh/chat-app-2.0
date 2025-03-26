package com.example.chatt_application.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatt_application.databinding.ActivityFeedbackSplashBinding;
import com.example.chatt_application.utilites.PreferenceManager;
import com.example.chatt_application.R;

public class FeedbackSplashActivity extends AppCompatActivity {

    private ActivityFeedbackSplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityFeedbackSplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Show splash screen for 2 seconds, then navigate to MainActivity
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish(); // Close FeedbackSplashActivity
        }, 3000);
    }
}
