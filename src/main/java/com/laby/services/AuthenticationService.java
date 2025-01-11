package com.laby.services;

import com.laby.dao.UserDAO;
import com.laby.models.User;
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

    public User register(String username, String password) throws SQLException {
        // Walidacja danych
        validateRegistrationData(username, password);

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

    public void logout(String sessionId) {
        activeSessions.remove(sessionId);
    }

    public Optional<User> getCurrentUser(String sessionId) {
        return Optional.ofNullable(activeSessions.get(sessionId));
    }

    private void validateRegistrationData(String username, String password) {
        if (username == null || username.trim().length() < 3) {
            throw new IllegalArgumentException("Username must be at least 3 characters long");
        }

        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }
    }

    private String generateSessionId() {
        return UUID.randomUUID().toString();
    }
}
