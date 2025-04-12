package com.example.chatt_application.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.chatt_application.R;
import com.example.chatt_application.databinding.ActivitySignUpBinding;
import com.example.chatt_application.utilites.Constants;
import com.example.chatt_application.utilites.PreferenceManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private String encodedImage;
    private ActivitySignUpBinding binding;
    private ProgressBar progressBar;
    private PreferenceManager preferenceManager;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());

        ImageView togglePassword = findViewById(R.id.togglePassword);
        EditText inputPassword = findViewById(R.id.inputPassword);
        EditText inputConfirmPassword = findViewById(R.id.inputConfirmPassword);

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

        setListeners();

        TextView textContentCreateNewAcc = findViewById(R.id.textContentSignUp);

        // Initialize ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);  // User can't dismiss manually

        textContentCreateNewAcc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show loading
                progressDialog.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog.dismiss(); // Hide loading
                        Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                        startActivity(intent);
                    }
                }, 1000);
            }
        });


    }

    @Override
    public void onBackPressed() {
        // Exit the app when back button is pressed
        super.onBackPressed();
        finish();
    }


    private void setListeners() {
        binding.buttonSignup.setOnClickListener(v -> {
            if (isValidSignUpDetails()) {
                signUp();
            }
        });

        binding.layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }


    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

    }


    private void signUp() {
        loading(true);
//        FirebaseFirestore database = FirebaseFirestore.getInstance();
//        HashMap<String, Object> user = new HashMap<>();
//        user.put(Constants.KEY_NAME, binding.inputName.getText().toString());
//        user.put(Constants.KEY_EMAIL, binding.inputEmail.getText().toString());
//        user.put(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString());
//
//        user.put(Constants.KEY_IMAGE, encodedImage);
//
//        database.collection(Constants.KEY_COLLECTION_USERS)
//                .add(user)
//                .addOnSuccessListener(documentReference -> {
//                    loading(false);
//                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
//                    preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
//                    preferenceManager.putString(Constants.KEY_NAME, binding.inputName.getText().toString());
//
//                    preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);
//                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                    startActivity(intent);
//
//                })
//                .addOnFailureListener(exception -> {
//                    loading(false);
//                    showToast(exception.getMessage());
//                });
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(binding.inputEmail.getText().toString(), binding.inputPassword.getText().toString()).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                loading(false);
                showToast(task.getException().getMessage());
                return;
            }

            // If successful, continue with storing user data in Firestore
            FirebaseFirestore database = FirebaseFirestore.getInstance();
            HashMap<String, Object> user = new HashMap<>();
            user.put(Constants.KEY_NAME, binding.inputName.getText().toString());
            user.put(Constants.KEY_EMAIL, binding.inputEmail.getText().toString());
            user.put(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString());
            user.put(Constants.KEY_IMAGE, encodedImage);

            database.collection(Constants.KEY_COLLECTION_USERS).add(user).addOnSuccessListener(documentReference -> {
                loading(false);
                preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
                preferenceManager.putString(Constants.KEY_NAME, binding.inputName.getText().toString());
                preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }).addOnFailureListener(exception -> {
                loading(false);
                showToast(exception.getMessage());
            });
        });

    }

    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();

        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);

        byte[] bytes = byteArrayOutputStream.toByteArray();

        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }


    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK) {
            if (result.getData() != null) {
                Uri imageUri = result.getData().getData();
                try {
                    assert imageUri != null;
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    binding.imageProfile.setImageBitmap(bitmap);
                    binding.textAddImage.setVisibility(View.GONE);
                    encodedImage = encodeImage(bitmap);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    });


    private boolean isValidSignUpDetails() {
        if (encodedImage == null) {
            showToast("Select a profile image!");
            return false;
        }

        String name = binding.inputName.getText().toString().trim();
        String email = binding.inputEmail.getText().toString().trim();
        String password = binding.inputPassword.getText().toString();
        String confirmPassword = binding.inputConfirmPassword.getText().toString();

        if (name.isEmpty()) {
            showToast("Please enter your name!");
            return false;
        }

        if (email.isEmpty()) {
            showToast("Please enter your email!");
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showToast("Please enter a valid email address!");
            return false;
        }

        if (password.isEmpty()) {
            showToast("Please enter a password!");
            return false;
        }

        if (confirmPassword.isEmpty()) {
            showToast("Please confirm your password!");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            showToast("Passwords do not match!");
            return false;
        }

        return true; // If all checks pass, return true
    }


    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.buttonSignup.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.buttonSignup.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }

    }

}
