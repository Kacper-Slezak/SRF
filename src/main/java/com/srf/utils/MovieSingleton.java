package com.srf.utils;

import com.srf.models.Movie;

public class MovieSingleton {
    private static final MovieSingleton instance;

    private Movie movie;
    private boolean AddedRating = false;
    private int movieIndex;

    private MovieSingleton() {}

    static {
        try{
            instance = new MovieSingleton();
        } catch (Exception e){
            throw new RuntimeException("Singleton exception");
        }
    }

    public static MovieSingleton getInstance(){
        return instance;
    }

    public Movie getMovie() {
        return movie;
    }
    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public void setAddedRating(boolean added) {
        this.AddedRating = added;
    }
    public boolean getAddedRating() {
        return AddedRating;
    }

    public void setMovieIndex(int movieIndex) {this.movieIndex = movieIndex;}
    public int getMovieIndex() {return movieIndex;}
}
