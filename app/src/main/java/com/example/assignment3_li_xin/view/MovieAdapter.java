package com.example.assignment3_li_xin.view;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.assignment3_li_xin.R;
import com.example.assignment3_li_xin.model.MovieModel;
import com.example.assignment3_li_xin.network.ApiClient;
import com.example.assignment3_li_xin.network.ApiService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {

    private static final String TAG = "MovieAdapter";

    // Listener interface for click events
    public interface OnMovieClickListener {
        void onMovieClick(MovieModel movie);
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(MovieModel movie, boolean isFavorite);
    }

    private final List<MovieModel> movieList = new ArrayList<>();
    private final OnMovieClickListener listener;
    private final OnFavoriteClickListener favoriteClickListener;

    public MovieAdapter(OnMovieClickListener listener, OnFavoriteClickListener favoriteClickListener) {
        this.listener = listener;
        this.favoriteClickListener = favoriteClickListener;
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movie, parent, false);
        return new MovieViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        MovieModel currentMovie = movieList.get(position);
        if (currentMovie != null) {
            holder.bind(currentMovie);

            // Fetch full movie details if director or rating is missing
            if ("Not Available".equals(currentMovie.getDirector()) || "Not Available".equals(currentMovie.getImdbRating())) {
                if (currentMovie.getImdbID() != null && !currentMovie.getImdbID().isEmpty()) {
                    fetchFullMovieDetails(currentMovie.getImdbID(), holder, position);
                } else {
                    Log.e(TAG, "IMDb ID is null or empty, cannot fetch details.");
                }
            }

            // Check favorite status in Firestore
            holder.fetchFavoriteStatus(currentMovie);
        }
    }

    @Override
    public int getItemCount() {
        return movieList.size();
    }

    @Override
    public long getItemId(int position) {
        return movieList.get(position).getImdbID().hashCode();
    }

    // Method to update the movie list
    public void updateMovieList(List<MovieModel> movies) {
        movieList.clear();
        if (movies != null) {
            movieList.addAll(movies);
        }
        notifyDataSetChanged();
    }

    // Fetch detailed movie information using IMDb ID
    private void fetchFullMovieDetails(String imdbID, MovieViewHolder holder, int position) {
        String apiKey = "5d005d1";
        ApiService apiService = ApiClient.getRetrofitInstance().create(ApiService.class);

        Call<MovieModel> call = apiService.getMovieDetails(apiKey, imdbID, "short");
        call.enqueue(new Callback<MovieModel>() {
            @Override
            public void onResponse(Call<MovieModel> call, Response<MovieModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    MovieModel detailedMovie = response.body();

                    // Update the movie in the list and notify the adapter
                    movieList.set(position, detailedMovie);
                    notifyItemChanged(position);

                    Log.d(TAG, "Details fetched for IMDb ID: " + imdbID);
                } else {
                    Log.e(TAG, "Failed to fetch movie details. Response code: " + response.code() + ", Message: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<MovieModel> call, Throwable t) {
                Log.e(TAG, "Error fetching movie details", t);
            }
        });
    }

    // ViewHolder class
    class MovieViewHolder extends RecyclerView.ViewHolder {

        private final TextView title;
        private final TextView year;
        private final TextView director;
        private final TextView rating;
        private final ImageView poster;
        private final ImageButton favoriteButton;

        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.movieTitle);
            year = itemView.findViewById(R.id.movieYear);
            director = itemView.findViewById(R.id.movieDirector);
            rating = itemView.findViewById(R.id.movieRating);
            poster = itemView.findViewById(R.id.moviePoster);
            favoriteButton = itemView.findViewById(R.id.favoriteButton);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION && position < movieList.size()) {
                        MovieModel movie = movieList.get(position);
                        listener.onMovieClick(movie);
                    }
                }
            });

            favoriteButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && position < movieList.size()) {
                    MovieModel movie = movieList.get(position);
                    boolean isFavorite = !movie.isFavorite();

                    // Update UI
                    movie.setFavorite(isFavorite);
                    if (isFavorite) {
                        favoriteButton.setImageResource(R.drawable.baseline_favorite_24);
                    } else {
                        favoriteButton.setImageResource(R.drawable.baseline_favorite_border_24);
                    }

                    // Update Firebase Firestore
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null) {
                        String userId = currentUser.getUid();
                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        if (isFavorite) {
                            // Add to favorites
                            db.collection("users")
                                    .document(userId)
                                    .collection("favorites")
                                    .document(movie.getImdbID())
                                    .set(movie)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Added to favorites: " + movie.getTitle());
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Failed to add movie to favorites: " + movie.getTitle(), e);
                                    });
                        } else {
                            // Remove from favorites
                            db.collection("users")
                                    .document(userId)
                                    .collection("favorites")
                                    .document(movie.getImdbID())
                                    .delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Removed from favorites: " + movie.getTitle());

                                        // Triggering favoriteClickListener to handle removal separately in MyFavoriteActivity
                                        if (favoriteClickListener != null) {
                                            favoriteClickListener.onFavoriteClick(movie, false);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Failed to remove movie from favorites: " + movie.getTitle(), e);
                                    });
                        }
                    }
                }
            });
        }

        public void bind(@NonNull MovieModel movie) {
            // Safely bind movie details
            title.setText(movie.getTitle() != null ? movie.getTitle() : "Title Not Available");
            year.setText(movie.getYear() != null ? movie.getYear() : "Year Not Available");

            String directorName = movie.getDirector() != null && !movie.getDirector().isEmpty() ? movie.getDirector() : "Not Available";
            director.setText("Director: " + directorName);

            String imdbRating = movie.getImdbRating() != null && !movie.getImdbRating().isEmpty() ? movie.getImdbRating() : "Not Available";
            rating.setText("Rating: " + imdbRating + "/10");

            Glide.with(itemView.getContext())
                    .load(movie.getPoster())
                    .placeholder(R.drawable.placeholder_image)
                    .into(poster);
        }

        // Fetch favorite status from Firestore
        public void fetchFavoriteStatus(MovieModel movie) {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser == null || movie.getImdbID() == null) return;

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String userId = currentUser.getUid();

            db.collection("users")
                    .document(userId)
                    .collection("favorites")
                    .document(movie.getImdbID())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            favoriteButton.setImageResource(R.drawable.baseline_favorite_24);
                            movie.setFavorite(true);
                        } else {
                            favoriteButton.setImageResource(R.drawable.baseline_favorite_border_24);
                            movie.setFavorite(false);
                        }
                    });
        }
    }
}





