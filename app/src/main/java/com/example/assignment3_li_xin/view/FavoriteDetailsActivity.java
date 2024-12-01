package com.example.assignment3_li_xin.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.assignment3_li_xin.R;
import com.example.assignment3_li_xin.model.MovieModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class FavoriteDetailsActivity extends AppCompatActivity {

    private ImageView moviePoster;
    private TextView movieTitle;
    private TextView movieYear;
    private EditText moviePlot;
    private ImageButton favoriteButton;
    private Button saveButton;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private MovieModel movie;
    private boolean isProcessing = false; // Prevent duplicate actions

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_details);

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Initialize UI components
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Add the navigation click listener here
        toolbar.setNavigationOnClickListener(v -> {
            // Navigate to "My Favorite" page
            Intent intent = new Intent(FavoriteDetailsActivity.this, MyFavoriteActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        moviePoster = findViewById(R.id.moviePoster);
        movieTitle = findViewById(R.id.movieTitle);
        movieYear = findViewById(R.id.movieYear);
        moviePlot = findViewById(R.id.moviePlot);
        favoriteButton = findViewById(R.id.favoriteButton);
        saveButton = findViewById(R.id.saveButton);

        // Retrieve movie details from the intent
        if (getIntent() != null && getIntent().hasExtra("MOVIE")) {
            movie = getIntent().getParcelableExtra("MOVIE");

            if (movie != null) {
                displayMovieDetails(movie);
            }
        }

        // Set initial favorite status
        updateFavoriteIcon(movie.isFavorite());

        // Favorite Button click listener
        favoriteButton.setOnClickListener(v -> {
            if (!isProcessing) {
                toggleFavorite();
            }
        });

        // Save Button click listener
        saveButton.setOnClickListener(v -> {
            if (!isProcessing) {
                updateMoviePlot();
            }
        });
    }

    private void displayMovieDetails(MovieModel movie) {
        movieTitle.setText(movie.getTitle());
        movieYear.setText(movie.getYear());
        moviePlot.setText(movie.getPlot());

        // Load the movie poster using Glide
        Glide.with(this)
                .load(movie.getPoster())
                .placeholder(R.drawable.placeholder_image)
                .into(moviePoster);
    }

    private void updateFavoriteIcon(boolean isFavorite) {
        if (isFavorite) {
            favoriteButton.setImageResource(R.drawable.baseline_favorite_24);
        } else {
            favoriteButton.setImageResource(R.drawable.baseline_favorite_border_24);
        }
    }

    private void toggleFavorite() {
        isProcessing = true; // Prevent duplicate actions
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            isProcessing = false;
            return;
        }

        String userId = currentUser.getUid();
        DocumentReference favoriteMovieRef = db.collection("users")
                .document(userId)
                .collection("favorites")
                .document(movie.getImdbID());

        boolean newFavoriteStatus = !movie.isFavorite();
        movie.setFavorite(newFavoriteStatus);

        if (newFavoriteStatus) {
            favoriteMovieRef.set(movie)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(FavoriteDetailsActivity.this, "Added to Favorites", Toast.LENGTH_SHORT).show();
                        updateFavoriteIcon(true);
                        isProcessing = false;
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(FavoriteDetailsActivity.this, "Failed to add to Favorites", Toast.LENGTH_SHORT).show();
                        isProcessing = false;
                    });
        } else {
            favoriteMovieRef.delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(FavoriteDetailsActivity.this, "Removed from Favorites", Toast.LENGTH_SHORT).show();
                        updateFavoriteIcon(false);
                        isProcessing = false;
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(FavoriteDetailsActivity.this, "Failed to remove from Favorites", Toast.LENGTH_SHORT).show();
                        isProcessing = false;
                    });
        }
    }

    private void updateMoviePlot() {
        String updatedPlot = moviePlot.getText().toString().trim();

        if (updatedPlot.isEmpty()) {
            Toast.makeText(this, "Plot cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        isProcessing = true; // Prevent duplicate actions
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DocumentReference movieRef = db.collection("users")
                    .document(userId)
                    .collection("favorites")
                    .document(movie.getImdbID());

            movie.setPlot(updatedPlot);
            movieRef.update("plot", updatedPlot)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(FavoriteDetailsActivity.this, "Movie plot updated successfully", Toast.LENGTH_SHORT).show();
                        isProcessing = false;
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(FavoriteDetailsActivity.this, "Failed to update movie plot", Toast.LENGTH_SHORT).show();
                        isProcessing = false;
                    });
        }
    }

    @Override
    public void onBackPressed() {
        // Handle physical back button behavior
        Intent intent = new Intent(this, MovieSearchActivity.class); // Navigate to Search
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();

        // Call the superclass method
        super.onBackPressed();
    }

}



