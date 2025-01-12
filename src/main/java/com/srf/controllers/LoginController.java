package com.srf.controllers;

import com.srf.dao.UserDAO;
import com.srf.models.User;
import com.srf.utils.DatabaseConnection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import com.srf.services.AuthenticationService;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class LoginController {
    @FXML
    public TextField usernameTextField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public Button logInButton;
    @FXML
    public Button registerButton;

    private Stage stage;
    private Scene       scene;
    private AuthenticationService authenticationService;

    @FXML
    public void initialize() {
        try {
            UserDAO userDAO = new UserDAO(DatabaseConnection.getConnection());
            this.authenticationService = new AuthenticationService(userDAO);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Could not connect to database. Please try again later.");
        }
    }

    @FXML
    public void onLogInButton(ActionEvent actionEvent) throws IOException {
        // Sprawdzenie, czy serwis został poprawnie zainicjalizowany
        if (authenticationService == null) {
            showAlert(Alert.AlertType.ERROR, "System Error",
                    "System initialization failed. Please restart the application.");
            return;
        }

        // Pobranie danych z pól tekstowych
        String username = usernameTextField.getText();
        String password = passwordField.getText();

        try {
            // Próba logowania za pomocą serwisu
            Optional<User> user = authenticationService.login(username, password);

            if (user.isPresent()) {
                // Sukces: przekierowanie do ekranu głównego
                showAlert(Alert.AlertType.INFORMATION, "Success", "Login successful!");
                switchScene(actionEvent, "home");
            } else {
                // Nieudane logowanie: brak użytkownika lub nieprawidłowe hasło
                showAlert(Alert.AlertType.ERROR, "Login Error", "Invalid username or password.");
            }
        } catch (IllegalArgumentException e) {
            // Obsługa błędów walidacji z serwisu
            showAlert(Alert.AlertType.ERROR, "Validation Error", e.getMessage());
        } catch (SQLException e) {
            // Obsługa problemów z bazą danych
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Could not connect to database. Please try again later.");
        }
    }


    @FXML
    public void onRegisterButton(ActionEvent actionEvent) throws IOException {
        switchScene(actionEvent, "registration");
    }

    public void switchScene(ActionEvent event, String sceneName) throws IOException {
        sceneName = "/com/srf/" + sceneName + ".fxml";
        try {
            FXMLLoader root = new FXMLLoader(getClass().getResource(sceneName));
            stage = (Stage)((Node)event.getSource()).getScene().getWindow();
            scene = new Scene(root.load());
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Scene Error", "Could not load the scene: " + sceneName);
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
