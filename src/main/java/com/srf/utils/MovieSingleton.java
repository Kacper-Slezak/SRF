package com.srf.utils;

import com.srf.models.Movie;
import com.srf.models.User;

public class MovieSingleton {
    private static MovieSingleton instance;

    private Movie movie;

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
}
