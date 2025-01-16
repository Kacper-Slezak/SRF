package com.srf.dao;

import com.srf.models.Rating;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RatingDAO {
    private Connection connection;

    public RatingDAO(Connection connection) {
        this.connection = connection;
    }

    public List<Rating> getAllRatings() throws SQLException {
        List<Rating> ratings = new ArrayList<>();
        String sql = "SELECT * FROM ratings";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                ratings.add(new Rating(
                        resultSet.getInt("user_id"),
                        resultSet.getInt("movie_id"),
                        resultSet.getDouble("rating")
                ));
            }
        }
        return ratings;
    }

    public void addRating(Rating rating) throws SQLException {
        String sql = "INSERT INTO ratings (user_id, movie_id, rating) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, rating.getUserId());
            statement.setInt(2, rating.getMovieId());
            statement.setDouble(3, rating.getRating());
            statement.executeUpdate();
        }
    }

    public Rating findRating(int userId, int movieId) throws SQLException {
        String sql = "SELECT * FROM ratings WHERE user_id = ? AND movie_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, movieId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new Rating(
                            resultSet.getInt("user_id"),
                            resultSet.getInt("movie_id"),
                            resultSet.getDouble("rating")
                    );
                } else {
                    return null;
                }
            }
        }
    }

    public void updateRating(Rating rating) throws SQLException {
        String sql = "UPDATE ratings SET rating = ? WHERE user_id = ? AND movie_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, rating.getRating());
            statement.setInt(2, rating.getUserId());
            statement.setInt(3, rating.getMovieId());
            statement.executeUpdate();
        }
    }

}
