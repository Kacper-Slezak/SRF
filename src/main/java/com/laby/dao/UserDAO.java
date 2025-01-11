package com.laby.dao;

import com.laby.models.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    private final Connection connection;

    // Konstruktor
    public UserDAO(Connection connection) {
        this.connection = connection;
    }

    // Metoda do pobierania wszystkich użytkowników
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

    // Metoda do dodawania nowego użytkownika
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
}
