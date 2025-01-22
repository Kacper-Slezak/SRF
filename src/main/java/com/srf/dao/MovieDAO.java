package com.srf.dao;

import com.srf.models.Movie;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MovieDAO {
    private Connection connection;

    public MovieDAO(Connection connection) {
        this.connection = connection;
    }

    public List<Movie> getAllMovies() throws SQLException {
        List<Movie> movies = new ArrayList<>();
        String sql = "SELECT * FROM movies";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                movies.add(new Movie(
                        resultSet.getInt("id"),
                        resultSet.getString("title"),
                        resultSet.getString("genre")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return movies;
    }

    public int addMovie(Movie movie) throws SQLException {
        String sql = "INSERT INTO movies (title, genre) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, movie.getTitle());
            statement.setString(2, movie.getGenre());
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1); // Pobranie ID nowego filmu
                } else {
                    throw new SQLException("Nie udało się uzyskać ID nowego filmu.");
                }
            }
        }
    }

    public List<Movie> searchMoviesByQuery(String sqlQuery, String[] keywords) throws SQLException {
        List<Movie> movies = new ArrayList<>();

        try (PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery)) {
            int paramIndex = 1;
            for (String keyword : keywords) {
                String keywordPattern = "%" + keyword + "%";
                preparedStatement.setString(paramIndex++, keywordPattern); // Dla tytułu
                preparedStatement.setString(paramIndex++, keywordPattern); // Dla gatunku
            }

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Movie movie = new Movie(0,"title","genre");
                    movie.setId(resultSet.getInt("id"));
                    movie.setTitle(resultSet.getString("title"));
                    movie.setGenre(resultSet.getString("genre"));
                    movies.add(movie);
                }
            }
        }

        return movies;
    }

}


