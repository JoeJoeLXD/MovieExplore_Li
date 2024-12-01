package com.example.assignment3_li_xin.network;

import com.example.assignment3_li_xin.model.MovieModel;
import com.example.assignment3_li_xin.model.MovieResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {

    // Method for searching movies, ensuring type is always set to "movie"
    @GET("/")
    Call<MovieResponse> searchMovies(
            @Query("apikey") String apiKey,
            @Query("s") String searchTerm,
            @Query("type") String type
    );

    // Method for getting movie details
    @GET("/")
    Call<MovieModel> getMovieDetails(
            @Query("apikey") String apiKey,
            @Query("i") String imdbID,
            @Query("plot") String plot
    );
}






