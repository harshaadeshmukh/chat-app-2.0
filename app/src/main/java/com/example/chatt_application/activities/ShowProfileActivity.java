package com.example.chatt_application.activities;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatt_application.R;
import com.example.chatt_application.utilites.Constants;
import com.example.chatt_application.utilites.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ShowProfileActivity extends AppCompatActivity {
    private PreferenceManager preferenceManager;
    private TextView showName, showEmail, showPassword;
    private ImageView imageProfile, togglePassword;
    private View imageBack;
    private boolean isPasswordVisible = false;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_profile);

        preferenceManager = new PreferenceManager(getApplicationContext());
        database = FirebaseFirestore.getInstance();

        showName = findViewById(R.id.showName);
        showEmail = findViewById(R.id.showEmail);
        showPassword = findViewById(R.id.showPassword);
        imageProfile = findViewById(R.id.imageProfile);
        togglePassword = findViewById(R.id.togglePassword);
        imageBack = findViewById(R.id.imageBack);
      //  buttonUpdate = findViewById(R.id.buttonUpdate);

        loadUserDetails();

        togglePassword.setOnClickListener(v -> togglePasswordVisibility());
        imageBack.setOnClickListener(v -> onBackPressed());
    }

    private void loadUserDetails() {
        String userId = preferenceManager.getString(Constants.KEY_USER_ID);

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        database.collection(Constants.KEY_COLLECTION_USERS).document(userId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot document = task.getResult();

                if (document.exists()) {
                    showName.setText(document.getString(Constants.KEY_NAME));
                    showEmail.setText(document.getString(Constants.KEY_EMAIL));

                    // Password is masked initially
                    showPassword.setText("************");

                    // Load profile image
                    String encodedImage = document.getString(Constants.KEY_IMAGE);
                    if (encodedImage != null && !encodedImage.isEmpty()) {
                        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        imageProfile.setImageBitmap(bitmap);
                    } else {
                        Toast.makeText(this, "No profile image found", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                Log.e("ProfileLoad", "Error: " + task.getException().getMessage());
            }
        });
    }

    private void togglePasswordVisibility() {
        if (!isPasswordVisible) {
            new AlertDialog.Builder(this).setTitle("Reveal Password").setMessage("For security reasons, avoid showing your password in public.\nDo you still want to proceed?").setPositiveButton("OK", (dialog, which) -> {

                String userId = preferenceManager.getString(Constants.KEY_USER_ID);
                database.collection(Constants.KEY_COLLECTION_USERS).document(userId).get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        showPassword.setText(documentSnapshot.getString(Constants.KEY_PASSWORD));
                        isPasswordVisible = true;

                        // Hide password after 7 seconds
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            showPassword.setText("************");
                            isPasswordVisible = false;
                        }, 6000);
                    }
                }).addOnFailureListener(e -> Toast.makeText(this, "Error fetching password", Toast.LENGTH_SHORT).show());
            }).setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss()).show();
        } else {
            showPassword.setText("*************");
            isPasswordVisible = false;
        }
    }

}
