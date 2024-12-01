package com.example.assignment3_li_xin.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.assignment3_li_xin.R;
import com.example.assignment3_li_xin.databinding.ActivityMyFavoriteBinding;
import com.example.assignment3_li_xin.model.MovieModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class MyFavoriteActivity extends AppCompatActivity {

    private ActivityMyFavoriteBinding binding;
    private MovieAdapter movieAdapter;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Use View Binding to inflate the layout
        binding = ActivityMyFavoriteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Setup Toolbar
        setupToolbar();

        // Setup RecyclerView
        setupRecyclerView();

        // Fetch favorite movies from Firebase
        fetchFavoriteMovies();

        // Setup BottomNavigationView
        setupBottomNavigationView();
    }

    private void setupBottomNavigationView() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_favorites); // Highlight the current page

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_search) {
                // Navigate to MovieSearchActivity with appropriate flags
                Intent searchIntent = new Intent(MyFavoriteActivity.this, MovieSearchActivity.class);
                searchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(searchIntent);
                finish();
                return true;
            } else if (id == R.id.nav_favorites) {
                // Already on this activity, no action required
                return true;
            }
            return false;
        });
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Hide the default title and set up the custom title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false); // Hide the default title
        }

        // Set up custom title for the toolbar
        TextView toolbarTitle = toolbar.findViewById(R.id.toolbarTitle);
        toolbarTitle.setText(getString(R.string.my_favorite_movies));

        // Handle the logout button click
        toolbar.setNavigationOnClickListener(v -> {
            // Log out the user
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(MyFavoriteActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();

            // Navigate to Login with appropriate flags
            Intent intent = new Intent(MyFavoriteActivity.this, Login.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }



    private void setupRecyclerView() {
        binding.recyclerViewFavorites.setLayoutManager(new LinearLayoutManager(this));
        movieAdapter = new MovieAdapter(movie -> {
            // Navigate to FavoriteDetailsActivity when a favorite movie is selected
            Intent intent = new Intent(MyFavoriteActivity.this, FavoriteDetailsActivity.class);
            intent.putExtra("MOVIE", movie); // Pass the full movie object to the new activity
            startActivity(intent);
        }, this::removeFavoriteMovie);

        binding.recyclerViewFavorites.setAdapter(movieAdapter);
    }


    private void fetchFavoriteMovies() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        String userId = currentUser.getUid();
        db.collection("users")
                .document(userId)
                .collection("favorites")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<MovieModel> favoriteMovies = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        MovieModel movie = document.toObject(MovieModel.class);
                        favoriteMovies.add(movie);
                    }
                    movieAdapter.updateMovieList(favoriteMovies);
                })
                .addOnFailureListener(e -> {
                    // Handle any errors here if needed
                    Toast.makeText(MyFavoriteActivity.this, "Failed to load favorites", Toast.LENGTH_SHORT).show();
                });
    }

    private void removeFavoriteMovie(MovieModel movie, boolean isFavorite) {
        if (!isFavorite) {
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser == null) {
                Toast.makeText(MyFavoriteActivity.this, "No user logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = currentUser.getUid();

            db.collection("users")
                    .document(userId)
                    .collection("favorites")
                    .document(movie.getImdbID())
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(MyFavoriteActivity.this, "Removed from Favorites", Toast.LENGTH_SHORT).show();
                        // Refresh the favorite list after removal
                        fetchFavoriteMovies();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(MyFavoriteActivity.this, "Failed to remove from Favorites", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the favorite movies list every time this activity resumes
        fetchFavoriteMovies();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}




