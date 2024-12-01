package com.example.assignment3_li_xin.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.assignment3_li_xin.model.MovieModel;
import com.example.assignment3_li_xin.model.MovieResponse;
import com.example.assignment3_li_xin.network.ApiClient;
import com.example.assignment3_li_xin.network.ApiService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.util.Log;

public class MovieViewModel extends ViewModel {

    private final MutableLiveData<List<MovieModel>> movies = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<MovieModel> movieDetails = new MutableLiveData<>();
    private final ApiService apiService;

    // Constructor
    public MovieViewModel() {
        apiService = ApiClient.getRetrofitInstance().create(ApiService.class);
    }

    // Getter for movie list LiveData
    public LiveData<List<MovieModel>> getMovies() {
        return movies;
    }

    // Getter for movie details LiveData
    public LiveData<MovieModel> getMovieDetails(String apiKey, String imdbID) {
        fetchMovieDetails(apiKey, imdbID);
        return movieDetails;
    }

    // Method to search movies
    public void searchMovies(String apiKey, String query) {
        Log.d("MovieViewModel", "Search initiated with query: " + query);

        // Validate API key and query
        if (apiKey == null || apiKey.isEmpty() || query == null || query.isEmpty()) {
            Log.e("MovieViewModel", "Invalid API key or search query. Search aborted.");
            return;
        }

        Call<MovieResponse> call = apiService.searchMovies(apiKey, query, "movie");
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<MovieModel> movieList = response.body().getMovies();

                    // Apply additional filtering to ensure only movies are displayed
                    List<MovieModel> filteredList = new ArrayList<>();
                    if (movieList != null && !movieList.isEmpty()) {
                        for (MovieModel movie : movieList) {
                            if ("movie".equalsIgnoreCase(movie.getType())) {
                                filteredList.add(movie);
                            }
                        }
                    }

                    if (!filteredList.isEmpty()) {
                        movies.setValue(filteredList);
                        Log.d("MovieViewModel", "Movies fetched successfully. Total: " + filteredList.size());
                    } else {
                        Log.e("MovieViewModel", "No movies found in the response");
                        movies.setValue(new ArrayList<>());  // Set an empty list if no movies are found
                    }
                } else {
                    Log.e("MovieViewModel", "Failed to fetch movies: " + response.message());
                    movies.setValue(new ArrayList<>());  // Set an empty list if the response is not successful
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                Log.e("MovieViewModel", "Error fetching movies: ", t);
                movies.setValue(new ArrayList<>());  // Set an empty list on failure
            }
        });
    }


    // Method to fetch movie details
    private void fetchMovieDetails(String apiKey, String imdbID) {
        Log.d("MovieViewModel", "Fetching details for IMDb ID: " + imdbID);

        // Validate API key and IMDb ID
        if (apiKey == null || apiKey.isEmpty() || imdbID == null || imdbID.isEmpty()) {
            Log.e("MovieViewModel", "Invalid API key or IMDb ID. Fetch aborted.");
            return;
        }

        Call<MovieModel> call = apiService.getMovieDetails(apiKey, imdbID, "short");
        call.enqueue(new Callback<MovieModel>() {
            @Override
            public void onResponse(Call<MovieModel> call, Response<MovieModel> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("MovieDetails", "Fetched details: " + response.body().toString());
                    movieDetails.setValue(response.body());
                } else {
                    Log.e("MovieDetails", "Failed to fetch details. Response: " + response.message());
                    movieDetails.setValue(new MovieModel());  // Set a default MovieModel on failure
                }
            }

            @Override
            public void onFailure(Call<MovieModel> call, Throwable t) {
                Log.e("MovieDetails", "API call failed: ", t);
                movieDetails.setValue(new MovieModel());
            }
        });
    }
}









