package com.srf.models;

/**
 * Reprezentuje tag przypisany do filmu przez użytkownika.
 * Tagi są używane do kategoryzacji i lepszego dopasowania rekomendacji.
 */

public class Tag {
    private int userId;
    private int movieId;
    private String tag;

    public Tag(int userId, int movieId, String tag) {
        this.userId = userId;
        this.movieId = movieId;
        this.tag = tag;
    }

    // Gettery i settery
    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag.toLowerCase().trim();
    }
    public String toString() {
        return "Tag{" +
                "movieId=" + movieId +
                ", tag='" + tag + '\'' +
                '}';
    }
}

