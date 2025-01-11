package com.laby.models;

public class Rating {
    private int userId; // ID użytkownika, który wystawił ocenę
    private int movieId; // ID filmu, który został oceniony
    private double score; // Ocena (np. w skali 1-10)

    // Konstruktor
    public Rating(int userId, int movieId, double score) {
        this.userId = userId;
        this.movieId = movieId;
        this.score = score;
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

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    // Metoda toString()
    @Override
    public String toString() {
        return "Rating{" +
                "userId=" + userId +
                ", movieId=" + movieId +
                ", score=" + score +
                '}';
    }
}
