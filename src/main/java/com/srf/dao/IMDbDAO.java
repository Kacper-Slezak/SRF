package com.srf.dao;

import com.srf.models.IMDb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class IMDbDAO {
    private Connection connection;
    public IMDbDAO(Connection connection) {
        this.connection = connection;
    }

    /**
     * Pobiera IMDb ID na podstawie identyfikatora filmu.
     *
     * @param movieId ID filmu.
     * @return IMDb ID lub null, jeśli nie znaleziono.
     * @throws SQLException w przypadku problemu z bazą danych.
     */
    public String getImdbIdByMovieId(int movieId) throws SQLException {
        String query = "SELECT  imdbId FROM links WHERE movieId = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, movieId);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString("imdbId");
            } else {
                return null;
            }
        }
    }

    /**
     * Dodaje nowy rekord IMDb do bazy danych.
     *
     * @param link Obiekt IMDb reprezentujący nowy rekord.
     * @return true, jeśli operacja się powiodła; false w przeciwnym razie.
     * @throws SQLException w przypadku problemu z bazą danych.
     */
    public boolean addImdbLink(IMDb link) throws SQLException {
        String query = "INSERT INTO links (movieId, imdbId) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, IMDb.getMovieId());
            statement.setInt(2, IMDb.getImdbId());
            return statement.executeUpdate() > 0;
        }
    }

    /**
     * Aktualizuje IMDb ID dla danego filmu.
     *
     * @param movieId ID filmu.
     * @param newImdbId Nowy IMDb ID.
     * @return true, jeśli operacja się powiodła; false w przeciwnym razie.
     * @throws SQLException w przypadku problemu z bazą danych.
     */
    public boolean updateImdbId(int movieId, String newImdbId) throws SQLException {
        String query = "UPDATE links SET imdbId = ? WHERE movieId = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, newImdbId);
            statement.setInt(2, movieId);
            return statement.executeUpdate() > 0;
        }
    }

    /**
     * Usuwa rekord IMDb na podstawie identyfikatora filmu.
     *
     * @param movieId ID filmu.
     * @return true, jeśli operacja się powiodła; false w przeciwnym razie.
     * @throws SQLException w przypadku problemu z bazą danych.
     */
    public boolean deleteImdbLink(int movieId) throws SQLException {
        String query = "DELETE FROM links WHERE movieId = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, movieId);
            return statement.executeUpdate() > 0;
        }
    }
}

