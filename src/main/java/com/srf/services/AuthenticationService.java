package com.srf.services;

import com.srf.dao.UserDAO;
import com.srf.models.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class AuthenticationService {
    private final UserDAO userDAO;
    private final Map<String, User> activeSessions = new HashMap<>();

    public AuthenticationService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public User register(String username, String password, String repeatPassword) throws SQLException {
        // Walidacja danych
        validateRegistrationData(username, password, repeatPassword);

        // Sprawdź czy użytkownik już istnieje
        if (userDAO.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Zahashuj hasło
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        // Stwórz i zapisz użytkownika
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPasswordHash(hashedPassword);

        userDAO.save(newUser);
        return newUser;
    }

    public Optional<User> login(String username, String password) throws SQLException {
        // Walidacja danych wejściowych
        validateLoginData(username, password);

        Optional<User> userOpt = userDAO.findByUsername(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (BCrypt.checkpw(password, user.getPasswordHash())) {
                String sessionId = generateSessionId();
                activeSessions.put(sessionId, user);
                user.setSessionId(sessionId);
                return Optional.of(user);
            }
        }

        return Optional.empty();
    }

    private void validateLoginData(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty.");
        }

        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }
    }

    public void logout(String sessionId) {
        activeSessions.remove(sessionId);
    }

    public Optional<User> getCurrentUser(String sessionId) {
        return Optional.ofNullable(activeSessions.get(sessionId));
    }

    private void validateRegistrationData(String username, String password, String repeatPassword) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty.");
        }

        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty.");
        }

        if (repeatPassword == null || repeatPassword.isEmpty()) {
            throw new IllegalArgumentException("Repeat password cannot be empty.");
        }

        if (!password.equals(repeatPassword)) {
            throw new IllegalArgumentException("Passwords do not match.");
        }
    }

    private String generateSessionId() {
        return UUID.randomUUID().toString();
    }
}


