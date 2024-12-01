package com.example.assignment3_li_xin.view;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.assignment3_li_xin.R;
import com.example.assignment3_li_xin.databinding.ActivityMovieDetailsBinding;
import com.example.assignment3_li_xin.model.MovieModel;
import com.example.assignment3_li_xin.viewmodel.MovieViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class MovieDetailsActivity extends AppCompatActivity {

    private ActivityMovieDetailsBinding binding;
    private MovieViewModel movieViewModel;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private boolean isFavorite = false;
    private boolean isFavoriteChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Use View Binding to inflate the layout
        binding = ActivityMovieDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Set up the toolbar with a navigation icon
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Set up custom title for the toolbar
        TextView toolbarTitle = toolbar.findViewById(R.id.toolbarTitle);
        toolbarTitle.setText(getString(R.string.movie_details));

        // Get IMDb ID from intent
        String imdbID = getIntent().getStringExtra("MOVIE_IMDB_ID");

        if (imdbID != null) {
            movieViewModel = new ViewModelProvider(this).get(MovieViewModel.class);
            String apiKey = getString(R.string.omdb_api_key);

            // Observe the movie details
            movieViewModel.getMovieDetails(apiKey, imdbID).observe(this, movie -> {
                if (movie != null) {
                    binding.movieTitle.setText(movie.getTitle());
                    binding.movieYear.setText(movie.getYear());
                    binding.moviePlot.setText(movie.getPlot());
                    Glide.with(MovieDetailsActivity.this)
                            .load(movie.getPoster())
                            .placeholder(R.drawable.placeholder_image)
                            .into(binding.moviePoster);
                    checkFavoriteStatus(movie);  // Check if the movie is already a favorite

                    // Set up favorite button click listener
                    binding.favoriteButton.setOnClickListener(v -> {
                        if (isFavorite) {
                            removeFromFavorites(movie);
                        } else {
                            addToFavorites(movie);
                        }
                    });
                } else {
                    binding.moviePlot.setText(R.string.plot_not_available);
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Navigate back when the navigation icon is clicked
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkFavoriteStatus(MovieModel movie) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null || movie.getImdbID() == null) return;

        String userId = currentUser.getUid();
        db.collection("users")
                .document(userId)
                .collection("favorites")
                .document(movie.getImdbID())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        isFavorite = true;
                        binding.favoriteButton.setImageResource(R.drawable.baseline_favorite_24);
                    } else {
                        isFavorite = false;
                        binding.favoriteButton.setImageResource(R.drawable.baseline_favorite_border_24);
                    }
                });
    }

    private void addToFavorites(MovieModel movie) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(MovieDetailsActivity.this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        db.collection("users")
                .document(userId)
                .collection("favorites")
                .document(movie.getImdbID())
                .set(movie)
                .addOnSuccessListener(aVoid -> {
                    isFavorite = true;
                    isFavoriteChanged = true;
                    binding.favoriteButton.setImageResource(R.drawable.baseline_favorite_24);
                    Toast.makeText(MovieDetailsActivity.this, "Added to Favorites", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(MovieDetailsActivity.this, "Failed to add to Favorites", Toast.LENGTH_SHORT).show());
    }

    private void removeFromFavorites(MovieModel movie) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(MovieDetailsActivity.this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();
        db.collection("users")
                .document(userId)
                .collection("favorites")
                .document(movie.getImdbID())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    isFavorite = false;
                    isFavoriteChanged = true;
                    binding.favoriteButton.setImageResource(R.drawable.baseline_favorite_border_24);
                    Toast.makeText(MovieDetailsActivity.this, "Removed from Favorites", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(MovieDetailsActivity.this, "Failed to remove from Favorites", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onBackPressed() {
        // Notify parent activity if the favorite status changed
        if (isFavoriteChanged) {
            setResult(RESULT_OK);
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}










