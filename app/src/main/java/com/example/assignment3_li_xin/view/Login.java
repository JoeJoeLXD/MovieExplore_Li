package com.example.assignment3_li_xin.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.assignment3_li_xin.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private ActivityLoginBinding binding;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up ViewBinding
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Log out user every time the app is opened to ensure login is required
        mAuth.signOut();
        Log.d(TAG, "onCreate: FirebaseAuth instance initialized.");

        // Login Button Click Listener
        binding.loginButton.setOnClickListener(v -> {
            String email = binding.emailEditText.getText().toString().trim();
            String password = binding.passwordEditText.getText().toString().trim();

            if (!validateInputs(email, password)) {
                return;
            }

            loginUser(email, password);
        });

        // Register TextView Click Listener (to open Register activity)
        binding.registerTextView.setOnClickListener(v -> {
            Intent intent = new Intent(Login.this, Register.class);
            startActivity(intent);
            finish();
        });
    }

    /**
     * Validate email and password inputs.
     *
     * @param email    The email input
     * @param password The password input
     * @return True if valid, false otherwise
     */
    private boolean validateInputs(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            binding.emailEditText.setError("Email is required.");
            binding.emailEditText.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailEditText.setError("Invalid email format.");
            binding.emailEditText.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            binding.passwordEditText.setError("Password is required.");
            binding.passwordEditText.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * Authenticate the user with email and password.
     *
     * @param email    The email input
     * @param password The password input
     */
    private void loginUser(String email, String password) {
        Log.d(TAG, "loginUser: Attempting to log in with email: " + email);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            Log.d(TAG, "loginUser: Login successful, User ID: " + user.getUid());
                            Toast.makeText(Login.this, "Login successful", Toast.LENGTH_SHORT).show();

                            // Navigate to MovieSearchActivity
                            navigateToMovieSearch();
                        } else {
                            Log.e(TAG, "loginUser: Login was successful, but user object is null.");
                            showError("An unexpected error occurred. Please try again.");
                        }
                    } else {
                        handleLoginError(task.getException());
                    }
                });
    }

    /**
     * Navigate to MovieSearchActivity and clear the back stack.
     */
    private void navigateToMovieSearch() {
        Intent intent = new Intent(Login.this, MovieSearchActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // Clear the stack and start a new task
        startActivity(intent);
        finish();
    }

    /**
     * Handle errors during login.
     *
     * @param exception The exception encountered during login
     */
    private void handleLoginError(@NonNull Exception exception) {
        String errorMessage;

        if (exception instanceof FirebaseAuthInvalidUserException) {
            errorMessage = "No account found with this email. Please register.";
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            errorMessage = "Invalid password. Please try again.";
        } else {
            errorMessage = exception.getMessage() != null ? exception.getMessage() : "Unknown error occurred.";
        }

        Log.e(TAG, "handleLoginError: Authentication failed: " + errorMessage);
        showError(errorMessage);
    }

    /**
     * Display an error message to the user.
     *
     * @param message The error message
     */
    private void showError(String message) {
        Toast.makeText(Login.this, message, Toast.LENGTH_LONG).show();
    }
}


