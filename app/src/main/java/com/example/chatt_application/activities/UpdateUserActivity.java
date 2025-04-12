package com.example.chatt_application.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import com.example.chatt_application.R;
import com.example.chatt_application.utilites.Constants;
import com.example.chatt_application.utilites.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.makeramen.roundedimageview.RoundedImageView;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class UpdateUserActivity extends AppCompatActivity {

    private EditText inputName, inputEmail, inputPassword, inputConfirmPassword;
    private RoundedImageView imageProfile;
    private FrameLayout layoutImage;
    private Button updateButton;
    private AppCompatImageView imageBack;
    private String encodedImage;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;

    ImageView togglePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user);

        // Initialize components

        initializeViews();

        togglePassword.setOnClickListener(view -> {
            if (inputPassword.getInputType() == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                // Hide Password

                inputPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                inputConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                togglePassword.setImageResource(R.drawable.baseline_visibility_off_24);
            } else {
                // Show Password
                inputPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                inputConfirmPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                togglePassword.setImageResource(R.drawable.baseline_visibility_24);
            }

            // Move cursor to end of text after changing input type
            inputPassword.setSelection(inputPassword.getText().length());
            inputConfirmPassword.setSelection(inputConfirmPassword.getText().length());
        });


        loadUserDetails();
        setListeners();
    }

    private void initializeViews() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        database = FirebaseFirestore.getInstance();
        togglePassword = findViewById(R.id.togglePassword);
        inputName = findViewById(R.id.inputName);
        inputEmail = findViewById(R.id.inputEmail);
        inputPassword = findViewById(R.id.inputPassword);
        inputConfirmPassword = findViewById(R.id.inputConfirmPassword);
        imageProfile = findViewById(R.id.imageProfile);
        layoutImage = findViewById(R.id.layoutImage);
        updateButton = findViewById(R.id.buttonUpdate);
        imageBack = findViewById(R.id.imageBack);
    }

    private void loadUserDetails() {
        // Get user ID from preferences
        String userId = preferenceManager.getString(Constants.KEY_USER_ID);

        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fetch current user data from Firestore
        database.collection(Constants.KEY_COLLECTION_USERS).document(userId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Pre-fill form with current values
                inputName.setText(documentSnapshot.getString(Constants.KEY_NAME));
                inputEmail.setText(documentSnapshot.getString(Constants.KEY_EMAIL));

                // Load profile image if exists
                String imageString = documentSnapshot.getString(Constants.KEY_IMAGE);
                if (imageString != null && !imageString.isEmpty()) {
                    byte[] bytes = Base64.decode(imageString, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    imageProfile.setImageBitmap(bitmap);
                    encodedImage = imageString; // Save current image
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(UpdateUserActivity.this, "Failed to load user data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void setListeners() {
        imageBack.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), ShowProfileActivity.class);
            startActivity(intent);
        });

        layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });

        updateButton.setOnClickListener(v -> {
            if (isValidUpdateDetails()) {
                updateUserProfile();
            }
        });
    }

    private void updateUserProfile() {
        String userId = preferenceManager.getString(Constants.KEY_USER_ID);
        if (userId == null || userId.isEmpty()) {
            Toast.makeText(this, "User ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate inputs
        if (!isValidUpdateDetails()) return;

        updateButton.setEnabled(false);
        updateButton.setText(R.string.updating);

        String name = inputName.getText().toString().trim();
        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();
        String confirmPassword = inputConfirmPassword.getText().toString().trim();

        // Ensure password and confirm match
        if (!password.isEmpty() && !password.equals(confirmPassword)) {
            showToast("Password and Confirm Password do not match");
            updateButton.setEnabled(true);
            updateButton.setText(R.string.update);
            return;
        }

        // Prepare update map
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_NAME, name);
        updates.put(Constants.KEY_EMAIL, email);

        if (!password.isEmpty()) {
            updates.put(Constants.KEY_PASSWORD, password); // 🔐 You can hash this before saving (see note below)
        }

        if (encodedImage != null) {
            updates.put(Constants.KEY_IMAGE, encodedImage);
        }

        database.collection(Constants.KEY_COLLECTION_USERS).document(userId).update(updates).addOnSuccessListener(unused -> {
            // Update SharedPreferences
            preferenceManager.putString(Constants.KEY_NAME, name);
            preferenceManager.putString(Constants.KEY_EMAIL, email);
            if (!password.isEmpty()) {
                preferenceManager.putString(Constants.KEY_PASSWORD, password);
            }
            if (encodedImage != null) {
                preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);
            }

            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();

            // Go back to main or profile activity
            Intent intent = new Intent(UpdateUserActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }).addOnFailureListener(e -> {
            updateButton.setEnabled(true);
            updateButton.setText(R.string.update);
            Toast.makeText(this, "Update Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }


    private boolean isValidUpdateDetails() {
        if (inputName.getText().toString().trim().isEmpty()) {
            showToast("Enter name");
            return false;
        } else if (inputEmail.getText().toString().trim().isEmpty()) {
            showToast("Enter email");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(inputEmail.getText().toString()).matches()) {
            showToast("Enter valid email");
            return false;
        } else if (!inputPassword.getText().toString().isEmpty() && inputPassword.getText().toString().length() < 6) {
            showToast("Password must be at least 6 characters");
            return false;
        } else if (!inputPassword.getText().toString().isEmpty() && !inputPassword.getText().toString().equals(inputConfirmPassword.getText().toString())) {
            showToast("Password and confirm password must match");
            return false;
        }

        return true;
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK) {
            if (result.getData() != null) {
                Uri imageUri = result.getData().getData();
                try {
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    imageProfile.setImageBitmap(bitmap);
                    encodedImage = encodeImage(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    showToast("Image not found");
                }
            }
        }
    });

    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}