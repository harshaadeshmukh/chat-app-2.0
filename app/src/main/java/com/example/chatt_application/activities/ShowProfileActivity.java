package com.example.chatt_application.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import android.graphics.Typeface;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.chatt_application.R;
import com.example.chatt_application.utilites.Constants;
import com.example.chatt_application.utilites.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ShowProfileActivity extends AppCompatActivity {
    private PreferenceManager preferenceManager;
    private TextView showName, showEmail, showPassword;
    private ImageView imageProfile, togglePassword;
    private View imageBack;
    private boolean isPasswordVisible = false;
    private FirebaseFirestore database;
    private Button button;

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
        loadUserDetails();

        togglePassword.setOnClickListener(v -> togglePasswordVisibility());
        imageBack.setOnClickListener(v -> onBackPressed());

        button = findViewById(R.id.button);


        button.setOnClickListener(view -> {
            showAlertDialog();
        });
    }

    @SuppressLint("SetTextI18n")
    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Create a vertical LinearLayout
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);
        layout.setBackgroundColor(Color.WHITE);
        layout.setBackground(ContextCompat.getDrawable(this, R.drawable.background_content_rounded));
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundColor(Color.WHITE);


        ImageView imageView = new ImageView(this);
        imageView.setImageResource(R.drawable.meetme);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(200, 200)); // Set size


        // Create Title TextView
        TextView titleTextView = new TextView(this);
        titleTextView.setText("Give us Review");
        titleTextView.setTextSize(18);
        titleTextView.setTypeface(null, Typeface.BOLD);
        titleTextView.setGravity(Gravity.CENTER);
        titleTextView.setPadding(0, 10, 0, 20);

        // Create RatingBar
        RatingBar ratingBar = new RatingBar(this);
        ratingBar.setNumStars(5);
        ratingBar.setStepSize(1.0f);
        ratingBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Create TextView to show feedback message based on rating
        TextView ratingMessageTextView = new TextView(this);
        ratingMessageTextView.setText("Waiting for Review");
        ratingMessageTextView.setGravity(Gravity.CENTER);
        ratingMessageTextView.setTextSize(14);
        ratingMessageTextView.setTypeface(null, Typeface.ITALIC);
        ratingMessageTextView.setPadding(0, 10, 0, 10);

        // Create EditText for user input
        TextView feedbackPromptTextView = new TextView(this);
        feedbackPromptTextView.setText("\nWhat do you like about the app?");
        feedbackPromptTextView.setTextSize(16);
        feedbackPromptTextView.setPadding(0, 18, 0, 5);

        EditText input = new EditText(this);
        input.setHint("Enter your feedback...");
        input.setInputType(InputType.TYPE_CLASS_TEXT);

        // Add views to layout
        layout.addView(imageView);
        layout.addView(titleTextView);
        layout.addView(ratingBar);
        layout.addView(ratingMessageTextView);
        layout.addView(feedbackPromptTextView);
        layout.addView(input);

        builder.setView(layout);

        // Listen for rating changes
        ratingBar.setOnRatingBarChangeListener((bar, rating, fromUser) -> {
            ratingMessageTextView.setText(getRatingMessage(rating));
        });

        builder
                .setPositiveButton("Submit", (dialog, which) -> {
            float userRating = ratingBar.getRating();
            String feedback = input.getText().toString().trim();

            if (userRating == 0) {
                Toast.makeText(this, "Please provide a rating!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (feedback.isEmpty()) {
                Toast.makeText(this, "Feedback cannot be empty!", Toast.LENGTH_SHORT).show();
                return;
            }

        //    Toast.makeText(this, "Thank you for your feedback!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this , FeedbackSplashActivity.class);

            // Save rating and feedback to Firestore
            String userId = preferenceManager.getString(Constants.KEY_USER_ID);
            if (userId != null && !userId.isEmpty()) {
                Map<String, Object> reviewData = new HashMap<>();
                reviewData.put("rating", userRating);
                reviewData.put("feedback", feedback);

                database.collection(Constants.KEY_COLLECTION_USERS)
                        .document(userId).
                        update(reviewData)
                        .addOnSuccessListener(aVoid ->

                                //  Toast.makeText(this, "Review submitted!", Toast.LENGTH_SHORT).show());
                                startActivity(intent))
                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to submit review: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
            finish();
        }).setCancelable(false);

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss()).setCancelable(false);

        builder.show();
    }

    // Method to return rating message
    private String getRatingMessage(float rating) {
        if (rating >= 4.5) {
            return "Outstanding! 🤩";
        } else if (rating >= 3.5) {
            return "Excellent! 😇";
        } else if (rating >= 2.5) {
            return "Nice effort! 😎";
        } else if (rating >= 1.5) {
            return "Needs improvement! 🤔";
        } else if (rating > 0) {
            return "Could be better! 🙂";
        } else {
            return "No ratings yet! 😕";
        }
    }


//    @SuppressLint("SetTextI18n")
//    private void showAlertDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//   //     builder.setTitle(title);
//    //    builder.setMessage(message);
//
//        // Create a vertical LinearLayout
//        LinearLayout layout = new LinearLayout(this);
//        layout.setOrientation(LinearLayout.VERTICAL);
//        layout.setPadding(50, 20, 50, 20);
//
//        // Create TextView
//        TextView textView = new TextView(this);
//        textView.setText("Give us Review");
//        textView.setTextSize(16);
//        textView.setPadding(0, 10, 0, 10);
//
//        // Create RatingBar
//        RatingBar ratingBar = new RatingBar(this);
//        ratingBar.setNumStars(5);
//        ratingBar.setStepSize(1.0f);
//        ratingBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//
//        // Create EditText for user input
//        EditText input = new EditText(this);
//        textView.setText("What do you like about the app?");
//        input.setHint("Enter your feedback...");
//        input.setInputType(InputType.TYPE_CLASS_TEXT);
//
//        // Add views to layout
//        layout.addView(textView);
//        layout.addView(ratingBar);
//        layout.addView(input);
//
//        builder.setView(layout);
//
//        builder.setPositiveButton("Submit", (dialog, which) -> {
//            float userRating = ratingBar.getRating();
//            String feedback = input.getText().toString().trim();
//
//            if (userRating == 0) {
//                Toast.makeText(this, "Please provide a rating!", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            if (feedback.isEmpty()) {
//                Toast.makeText(this, "Feedback cannot be empty!", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            Toast.makeText(this, "Thank you for your feedback!", Toast.LENGTH_SHORT).show();
//
//            // Here, you can save the rating and feedback to Firestore
//            String userId = preferenceManager.getString(Constants.KEY_USER_ID);
//            if (userId != null && !userId.isEmpty()) {
//                Map<String, Object> reviewData = new HashMap<>();
//                reviewData.put("rating", userRating);
//                reviewData.put("feedback", feedback);
//
//                database.collection(Constants.KEY_COLLECTION_USERS).document(userId)
//                        .update(reviewData)
//                        .addOnSuccessListener(aVoid -> Toast.makeText(this, "Review submitted!", Toast.LENGTH_SHORT).show())
//                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to submit review: " + e.getMessage(), Toast.LENGTH_SHORT).show());
//            }
//        });
//
//        LayerDrawable stars = (LayerDrawable) ratingBar.getProgressDrawable();

    /// / Change the filled stars to white
//        stars.getDrawable(2).setColorFilter(Color.parseColor("#1C2E46"), PorterDuff.Mode.SRC_IN);
//        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
//
//        builder.show();
//    }


    // Method to update the review text based on rating
//    private void updateReviewText(float rating) {
//        if (rating >= 4.5) {
//            String s = "Outstanding! 🤩";
//            textView.setText(s);
//        } else if (rating >= 3.5) {
//            String s = "Excellent! 😇";
//            textView.setText(s);
//        } else if (rating >= 2.5) {
//            String s = "Nice effort! 😎";
//            textView.setText(s);
//        } else if (rating >= 1.5) {
//            textView.setText("Needs improvement! 🤔");
//        } else if (rating > 0) {
//            textView.setText("Could be better! 🙂");
//        } else {
//            textView.setText("No ratings yet! 😕");
//        }
//    }
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

                    // Load and improve profile image quality
                    String encodedImage = document.getString(Constants.KEY_IMAGE);
                    if (encodedImage != null && !encodedImage.isEmpty()) {
                        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);

                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888; // Improves quality
                        options.inDither = true; // Reduces color banding
                        options.inScaled = true; // Ensures smooth scaling

                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
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
            new AlertDialog.Builder(this).setTitle("Reveal Password").setMessage("For security reasons, avoid showing your password in public.\nDo you still want to proceed?").setCancelable(false).setPositiveButton("OK", (dialog, which) -> {

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
            }).setCancelable(false).setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss()).show();
        } else {
            showPassword.setText("*************");
            isPasswordVisible = false;
        }
    }

}
