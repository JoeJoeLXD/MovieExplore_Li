package com.example.assignment3_li_xin.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.assignment3_li_xin.R;
import com.example.assignment3_li_xin.databinding.ActivityMovieSearchBinding;
import com.example.assignment3_li_xin.model.MovieModel;
import com.example.assignment3_li_xin.viewmodel.MovieViewModel;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MovieSearchActivity extends AppCompatActivity {

    private ActivityMovieSearchBinding binding;
    private MovieAdapter movieAdapter;
    private MovieViewModel movieViewModel;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private static final String TAG = "MovieSearchActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Use View Binding to inflate the layout
        binding = ActivityMovieSearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase
        initializeFirebase();

        // Check if user is logged in
        if (!isUserLoggedIn()) {
            redirectToLogin();
            return;
        }

        // Setup Toolbar
        setupToolbar();

        // Setup RecyclerView
        setupRecyclerView();

        // Initialize ViewModel
        movieViewModel = new ViewModelProvider(this).get(MovieViewModel.class);

        // Observe changes in the movie list
        observeMovies();

        // Set up the Search button click listener
        TextInputLayout searchFieldLayout = binding.searchFieldLayout;
        searchFieldLayout.setEndIconOnClickListener(v -> onSearchClicked());

        // Handle keyboard search action
        binding.searchField.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                onSearchClicked();
                // Hide the keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(binding.searchField.getWindowToken(), 0);
                }
                return true;
            }
            return false;
        });

        // Handle BottomNavigationView
        setupBottomNavigation();
    }

    // ADD THE onActivityResult METHOD BELOW onCreate
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == RESULT_OK) {
            // Refresh the movie list or favorites after returning from MovieDetailsActivity
            String apiKey = getString(R.string.omdb_api_key);
            String query = binding.searchField.getText().toString().trim();
            if (!query.isEmpty()) {
                movieViewModel.searchMovies(apiKey, query);
            }
        }
    }

    private void setupBottomNavigation() {
        binding.bottomNavigationView.setSelectedItemId(R.id.nav_search); // Highlight the current item

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_search) {
                Toast.makeText(this, "Search Selected", Toast.LENGTH_SHORT).show();
                return true;
            } else if (id == R.id.nav_favorites) {
                Intent intent = new Intent(this, MyFavoriteActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                overridePendingTransition(0, 0);
                return true;
            }
            return false;
        });
    }

    private void redirectToLogin() {
        Intent intent = new Intent(MovieSearchActivity.this, Login.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private boolean isUserLoggedIn() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        return currentUser != null;
    }

    private void setupToolbar() {
        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        // Hide the default title (already set in XML)
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Handle the logout button click
        toolbar.setNavigationOnClickListener(v -> {
            // Log out the user
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(MovieSearchActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();

            // Navigate to the Login page
            Intent intent = new Intent(MovieSearchActivity.this, Login.class);
            startActivity(intent);

            // Finish current activity to prevent returning here when pressing back
            finish();
        });
    }


    private void setupRecyclerView() {
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        movieAdapter = new MovieAdapter(movie -> {
            // Handle click to navigate to MovieDetailsActivity
            if (movie != null) {
                Intent intent = new Intent(MovieSearchActivity.this, MovieDetailsActivity.class);
                intent.putExtra("MOVIE_IMDB_ID", movie.getImdbID()); // Pass IMDb ID to fetch details in MovieDetailsActivity
                startActivityForResult(intent, 100); // Request code 100
            } else {
                Toast.makeText(MovieSearchActivity.this, "Unable to load movie details", Toast.LENGTH_SHORT).show();
            }
        }, this::toggleFavorite);

        binding.recyclerView.setAdapter(movieAdapter);
    }

    private void onSearchClicked() {
        String query = binding.searchField.getText().toString().trim();
        if (!query.isEmpty()) {
            // Clear RecyclerView and hide "No Results Found" message before the search begins
            movieAdapter.updateMovieList(new ArrayList<>());
            binding.noResultsFoundText.setVisibility(View.GONE);

            String apiKey = getString(R.string.omdb_api_key);
            movieViewModel.searchMovies(apiKey, query);
        } else {
            Toast.makeText(MovieSearchActivity.this, "Please enter a movie title", Toast.LENGTH_SHORT).show();
        }
    }

    private void observeMovies() {
        movieViewModel.getMovies().observe(this, movieModels -> {
            String query = binding.searchField.getText().toString().trim();

            if (movieModels != null && !movieModels.isEmpty()) {
                // Update RecyclerView with results and hide "No Results Found"
                movieAdapter.updateMovieList(movieModels);
                binding.noResultsFoundText.setVisibility(View.GONE);
            } else {
                // Only show "No Results Found" if there was a valid search query and no results
                if (!query.isEmpty()) {
                    binding.noResultsFoundText.setVisibility(View.VISIBLE);
                    Toast.makeText(MovieSearchActivity.this, "No results found for '" + query + "'", Toast.LENGTH_SHORT).show();
                }
                movieAdapter.updateMovieList(new ArrayList<>()); // Clear the list if no movies found
            }
        });
    }


    private void toggleFavorite(MovieModel movie, boolean isFavorite) {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(MovieSearchActivity.this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = currentUser.getUid();

        if (movie.getImdbID() == null || movie.getImdbID().isEmpty()) {
            Toast.makeText(this, "Movie ID is not available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Prepare a map to add only the necessary fields to Firestore
        Map<String, Object> movieData = new HashMap<>();
        movieData.put("title", movie.getTitle());
        movieData.put("year", movie.getYear());
        movieData.put("imdbID", movie.getImdbID());
        movieData.put("poster", movie.getPoster());
        movieData.put("director", movie.getDirector());
        movieData.put("imdbRating", movie.getImdbRating());

        if (isFavorite) {
            // Add movie to user's favorite list in Firestore
            db.collection("users")
                    .document(userId)
                    .collection("favorites")
                    .document(movie.getImdbID())
                    .set(movieData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(MovieSearchActivity.this, "Added to Favorites", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(MovieSearchActivity.this, "Failed to add to Favorites", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Remove movie from user's favorite list in Firestore
            db.collection("users")
                    .document(userId)
                    .collection("favorites")
                    .document(movie.getImdbID())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(MovieSearchActivity.this, "Removed from Favorites", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(MovieSearchActivity.this, "Failed to remove from Favorites", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}



















