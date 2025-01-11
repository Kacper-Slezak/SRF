package com.laby.dao;

import com.laby.models.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserDAO {
    private final Connection connection;

    // Konstruktor
    public UserDAO(Connection connection) {
        this.connection = connection;
    }

    /**
     * Pobiera wszystkich użytkowników z bazy danych.
     * @return Lista użytkowników
     * @throws SQLException Jeśli wystąpi błąd połączenia z bazą danych
     */
    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";

        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                // Tworzenie obiektu User na podstawie danych z bazy
                users.add(new User(
                        resultSet.getInt("id"),
                        resultSet.getString("username"),
                        resultSet.getString("password_hash")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Błąd podczas pobierania użytkowników: " + e.getMessage());
            throw e; // Rzucanie wyjątku wyżej
        }
        return users;
    }

    /**
     * Znajduje użytkownika na podstawie nazwy użytkownika.
     * @param username Nazwa użytkownika
     * @return Obiekt User w Optional, jeśli użytkownik istnieje
     * @throws SQLException Jeśli wystąpi błąd połączenia z bazą danych
     */
    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return Optional.of(new User(
                            resultSet.getInt("id"),
                            resultSet.getString("username"),
                            resultSet.getString("password_hash")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Błąd podczas znajdowania użytkownika: " + e.getMessage());
            throw e;
        }
        return Optional.empty();
    }

    /**
     * Dodaje nowego użytkownika do bazy danych.
     * @param user Obiekt użytkownika
     * @throws SQLException Jeśli wystąpi błąd podczas dodawania użytkownika
     */
    public void addUser(User user) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPasswordHash());
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Błąd podczas dodawania użytkownika: " + e.getMessage());
            throw e; // Rzucanie wyjątku wyżej
        }
    }

    /**
     * Zapisuje użytkownika (dodaje lub aktualizuje, jeśli istnieje).
     * @param user Obiekt użytkownika
     * @throws SQLException Jeśli wystąpi błąd połączenia z bazą danych
     */
    public void save(User user) throws SQLException {
        if (findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Użytkownik z podaną nazwą już istnieje.");
        }
        addUser(user);
    }
}
