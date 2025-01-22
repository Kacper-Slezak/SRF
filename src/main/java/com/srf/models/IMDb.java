package com.srf.models;

/**
 * Reprezentuje tag przypisany do filmu przez użytkownika.
 * Tagi są używane do kategoryzacji i lepszego dopasowania rekomendacji.
 */

public class IMDb {
    private static int movieId;
    private static int imdbId;
    private int tmdbId;

    public IMDb(int movieId, int imdbId) {
        this.movieId = movieId;
        this.imdbId = imdbId;
        this.tmdbId = tmdbId;
    }

    // Gettery i settery
    public static int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        IMDb.movieId = movieId;
    }

    public static int getImdbId() {return imdbId;}
    public void setImdbId(int imdbId) {this.imdbId = imdbId;}
    public int getTmdbId() {return tmdbId;}
    public void setTmdbId(int tmdbId) {this.tmdbId = tmdbId;}

    public String toString() {
        return movieId + "\t" + imdbId + "\t" + tmdbId;
    }
}

