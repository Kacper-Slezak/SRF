package com.laby.models;

/**
 * Klasa reprezentująca użytkownika systemu.
 */
public class User {
    private int id;
    private String username;
    private String passwordHash;
    private String sessionId; // Opcjonalne pole dla sesji użytkownika

    /**
     * Domyślny konstruktor bez parametrów.
     */
    public User() {
    }

    /**
     * Konstruktor z wszystkimi polami.
     * @param id Identyfikator użytkownika
     * @param username Nazwa użytkownika
     * @param passwordHash Zahashowane hasło użytkownika
     */
    public User(int id, String username, String passwordHash) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    /**
     * Konstruktor bez identyfikatora (przydatny do tworzenia nowych użytkowników przed zapisaniem do bazy).
     * @param username Nazwa użytkownika
     * @param passwordHash Zahashowane hasło użytkownika
     */
    public User(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }

    // Gettery i settery
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", sessionId='" + sessionId + '\'' +
                '}';
    }
}
