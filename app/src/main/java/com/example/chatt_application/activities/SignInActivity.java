package com.example.chatt_application.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.chatt_application.R;
import com.example.chatt_application.databinding.ActivitySignInBinding;
import com.example.chatt_application.utilites.Constants;
import com.example.chatt_application.utilites.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class SignInActivity extends AppCompatActivity {

    private ProgressDialog progressDialog;
    private ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());
        setContentView(R.layout.activity_sign_in);

        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }

        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();

        // Password visibility toggle logic
        final EditText passwordInput = findViewById(R.id.inputPassword);
        final ImageView passwordToggle = findViewById(R.id.passwordToggle);

        passwordToggle.setOnClickListener(v -> {
            if (passwordInput.getTransformationMethod().equals(PasswordTransformationMethod.getInstance())) {
                // Show Password
                passwordInput.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                passwordToggle.setImageResource(R.drawable.baseline_visibility_24); // Change to "eye open" icon
            } else {
                // Hide Password
                passwordInput.setTransformationMethod(PasswordTransformationMethod.getInstance());
                passwordToggle.setImageResource(R.drawable.baseline_visibility_off_24); // Change to "eye closed" icon
            }
            passwordInput.setSelection(passwordInput.length()); // Move cursor to end
        });

        // Initialize TextView
        TextView textContentCreateNewAcc = findViewById(R.id.textContentCreateNewAcc);
        // Initialize ProgressDialog
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);  // User can't dismiss manually

        textContentCreateNewAcc.setOnClickListener(v -> {

            progressDialog.show();

            new Handler().postDelayed(() -> {
                progressDialog.dismiss(); // Hide loading
                Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(intent);
            }, 1500);    // Delay for 1.5 seconds before starting SignUpActivity
        });
    }

    @Override
    public void onBackPressed() {
        // Exit the app when back button is pressed
        super.onBackPressed();
        finishAffinity(); // Closes all activities and exits the app
    }


    private void SignIn() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();

        String email = binding.inputEmail.getText().toString().trim();
        String password = binding.inputPassword.getText().toString().trim();

        // Check if email is in a valid format
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            loading(false);
            showToast("Email is incorrect!"); // Show message if email format is invalid
            return;
        }

        // Check if email exists in Fire store
        database.collection(Constants.KEY_COLLECTION_USERS).whereEqualTo(Constants.KEY_EMAIL, email).get().addOnCompleteListener(emailCheckTask -> {
            if (emailCheckTask.isSuccessful() && emailCheckTask.getResult() != null && !emailCheckTask.getResult().getDocuments().isEmpty()) {
                // Email exists, now check password
                database.collection(Constants.KEY_COLLECTION_USERS).whereEqualTo(Constants.KEY_EMAIL, email).whereEqualTo(Constants.KEY_PASSWORD, password).get().addOnCompleteListener(passwordCheckTask -> {
                    if (passwordCheckTask.isSuccessful() && passwordCheckTask.getResult() != null && !passwordCheckTask.getResult().getDocuments().isEmpty()) {
                        DocumentSnapshot documentSnapshot = passwordCheckTask.getResult().getDocuments().get(0);

                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                        preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                        preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                        preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));

                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    } else {
                        loading(false);
                        showToast("Incorrect password!");
                    }
                });
            } else {
                loading(false);
                showToast("Email not registered!");
            }
        });
    }


//    private void SignIn()
//    {
//        loading(true);
//        FirebaseFirestore database = FirebaseFirestore.getInstance();
//        database.collection(Constants.KEY_COLLECTION_USERS)
//                .whereEqualTo(Constants.KEY_EMAIL , binding.inputEmail.getText().toString())
//                .whereEqualTo(Constants.KEY_PASSWORD , binding.inputPassword.getText().toString())
//                .get()
//                .addOnCompleteListener(task ->
//                {
//                    if(task.isSuccessful() && task.getResult()!=null && !task.getResult().getDocuments().isEmpty())
//                    {
//                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
//                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
//                        preferenceManager.putString(Constants.KEY_USER_ID , documentSnapshot.getId());
//                        preferenceManager.putString(Constants.KEY_NAME , documentSnapshot.getString(Constants.KEY_NAME));
//                        preferenceManager.putString(Constants.KEY_IMAGE , documentSnapshot.getString(Constants.KEY_IMAGE));
//                        Intent intent = new Intent(getApplicationContext() , MainActivity.class);
//                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                        startActivity(intent);
//                    }
//                    else{
//                            loading(false);
//                            showToast("Unable to Sign In");
//                    }
//                });
//    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.buttonSignin.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.buttonSignin.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);
        }


    }

    private void setListeners() {
        binding.buttonSignin.setOnClickListener(v -> {
            if (isValidSignInDetails()) {
                SignIn();
            }

        });
    }


    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

    }

    private Boolean isValidSignInDetails() {
        if (binding.inputEmail.getText().toString().trim().isEmpty()) {
            showToast("Enter email!");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            showToast("Enter valid email!");
        } else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Enter password");
            return false;
        }

        return true;

    }

}
