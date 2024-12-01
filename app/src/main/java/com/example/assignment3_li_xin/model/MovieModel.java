package com.example.assignment3_li_xin.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class MovieModel implements Parcelable {

    @SerializedName("Title")
    private final String title;

    @SerializedName("Year")
    private final String year;

    @SerializedName("imdbID")
    private final String imdbID;

    @SerializedName("Type")
    private final String type;

    @SerializedName("Poster")
    private final String poster;

    @SerializedName("Plot")
    private String plot;

    @SerializedName("Director")
    private final String director;

    @SerializedName("imdbRating")
    private final String imdbRating;

    private boolean isFavorite;

    // Default constructor
    public MovieModel() {
        this.title = "Not Available";
        this.year = "Not Available";
        this.imdbID = "Not Available";
        this.type = "Not Available";
        this.poster = "Not Available";
        this.plot = "Not Available";
        this.director = "Not Available";
        this.imdbRating = "Not Available";
        this.isFavorite = false;
    }

    // Constructor with parameters
    public MovieModel(String title, String year, String imdbID, String type, String poster, String plot, String director, String imdbRating, boolean isFavorite) {
        this.title = title;
        this.year = year;
        this.imdbID = imdbID;
        this.type = type;
        this.poster = poster;
        this.plot = plot;
        this.director = director;
        this.imdbRating = imdbRating;
        this.isFavorite = isFavorite;
    }

    // Parcelable implementation
    protected MovieModel(Parcel in) {
        title = in.readString();
        year = in.readString();
        imdbID = in.readString();
        type = in.readString();
        poster = in.readString();
        plot = in.readString();
        director = in.readString();
        imdbRating = in.readString();
        isFavorite = in.readByte() != 0;
    }

    public static final Creator<MovieModel> CREATOR = new Creator<MovieModel>() {
        @Override
        public MovieModel createFromParcel(Parcel in) {
            return new MovieModel(in);
        }

        @Override
        public MovieModel[] newArray(int size) {
            return new MovieModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(year);
        dest.writeString(imdbID);
        dest.writeString(type);
        dest.writeString(poster);
        dest.writeString(plot);
        dest.writeString(director);
        dest.writeString(imdbRating);
        dest.writeByte((byte) (isFavorite ? 1 : 0));
    }

    public void setPlot(String plot) {
        this.plot = plot;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getYear() {
        return year;
    }

    public String getImdbID() {
        return imdbID;
    }

    public String getType() {
        return type;
    }

    public String getPoster() {
        return poster;
    }

    public String getPlot() {
        return plot;
    }

    public String getDirector() {
        return director;
    }

    public String getImdbRating() {
        return imdbRating;
    }

    // Getter and Setter for isFavorite
    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    @Override
    public String toString() {
        return "MovieModel{" +
                "title='" + title + '\'' +
                ", year='" + year + '\'' +
                ", imdbID='" + imdbID + '\'' +
                ", type='" + type + '\'' +
                ", poster='" + poster + '\'' +
                ", plot='" + plot + '\'' +
                ", director='" + director + '\'' +
                ", imdbRating='" + imdbRating + '\'' +
                ", isFavorite=" + isFavorite +
                '}';
    }
}








