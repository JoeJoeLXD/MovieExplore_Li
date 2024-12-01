package com.example.assignment3_li_xin.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.assignment3_li_xin.databinding.ActivityRegisterBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class Register extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private FirebaseAuth mAuth;
    private static final String TAG = "RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up ViewBinding
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        Log.d(TAG, "onCreate: FirebaseAuth instance initialized.");

        // Register Button Click Listener
        binding.registerButton.setOnClickListener(v -> {
            String email = binding.emailEditText.getText().toString().trim();
            String password = binding.passwordEditText.getText().toString().trim();
            String confirmPassword = binding.confirmPasswordEditText.getText().toString().trim();

            // Input validation
            if (!validateInputs(email, password, confirmPassword)) {
                return;
            }

            // Show ProgressBar
            binding.progressBar.setVisibility(View.VISIBLE);
            registerUser(email, password);
        });

        // Login TextView Click Listener
        binding.loginTextView.setOnClickListener(v -> {
            Log.d(TAG, "onClick: Navigating to Login activity");
            Intent intent = new Intent(Register.this, Login.class);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Validates user inputs for email, password, and confirmPassword fields.
     */
    private boolean validateInputs(String email, String password, String confirmPassword) {
        if (TextUtils.isEmpty(email)) {
            binding.emailEditText.setError("Email is required.");
            binding.emailEditText.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailEditText.setError("Invalid email format.");
            binding.emailEditText.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            binding.passwordEditText.setError("Password is required.");
            binding.passwordEditText.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            binding.passwordEditText.setError("Password must be at least 6 characters.");
            binding.passwordEditText.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            binding.confirmPasswordEditText.setError("Passwords do not match.");
            binding.confirmPasswordEditText.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Registers a new user with Firebase Authentication.
     */
    private void registerUser(String email, String password) {
        Log.d(TAG, "registerUser: Attempting to register user with email: " + email);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    // Hide ProgressBar after registration is complete
                    binding.progressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        Log.d(TAG, "registerUser: Registration successful");
                        Toast.makeText(Register.this, "Registration successful. Please log in.", Toast.LENGTH_SHORT).show();

                        // Redirect to Login activity
                        Intent intent = new Intent(Register.this, Login.class);
                        startActivity(intent);
                        finish();
                    } else {
                        handleRegistrationError(task.getException());
                    }
                });
    }

    /**
     * Handles errors during the registration process and provides user-friendly error messages.
     */
    private void handleRegistrationError(@NonNull Exception exception) {
        String errorMessage;
        if (exception instanceof FirebaseAuthUserCollisionException) {
            errorMessage = "This email is already registered. Please log in.";
        } else {
            errorMessage = exception.getMessage() != null ? exception.getMessage() : "Unknown error occurred.";
        }
        Log.e(TAG, "registerUser: Registration failed: " + errorMessage);
        Toast.makeText(Register.this, "Registration failed: " + errorMessage, Toast.LENGTH_LONG).show();
    }
}



