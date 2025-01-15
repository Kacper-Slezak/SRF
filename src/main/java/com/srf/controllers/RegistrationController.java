package com.srf.controllers;

import com.srf.models.User;
import com.srf.services.AuthenticationService;
import com.srf.dao.UserDAO;
import com.srf.utils.AlertManager;
import com.srf.utils.DatabaseConnection;
import com.srf.utils.SceneManager;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.sql.SQLException;

public class RegistrationController {
    @FXML
    public TextField usernameTextField;

    @FXML
    public PasswordField passwordField;

    @FXML
    public PasswordField repeatPasswordField;

    @FXML
    public Button registerButton;

    private AuthenticationService authenticationService;
    AlertManager alertManager = AlertManager.getInstance();
    SceneManager sceneManager = SceneManager.getInstance();

    @FXML
    public void initialize() {
        try {
            UserDAO userDAO = new UserDAO(DatabaseConnection.getConnection());
            this.authenticationService = new AuthenticationService(userDAO);
        } catch (SQLException e) {
            alertManager.showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Could not connect to database. Please try again later.");
        }
    }

    @FXML
    public void onRegisterButton(ActionEvent actionEvent) throws IOException {
        if (authenticationService == null) {
            alertManager.showAlert(Alert.AlertType.ERROR, "System Error",
                    "System initialization failed. Please restart the application.");
            return;
        }

        String username = usernameTextField.getText();
        String password = passwordField.getText();
        String repeatPassword = repeatPasswordField.getText();

        // Podstawowa walidacja danych
        if (username.isEmpty() || password.isEmpty() || repeatPassword.isEmpty()) {
            alertManager.showAlert(Alert.AlertType.ERROR, "Registration Error",
                    "Please fill in all fields.");
            return;
        }

        if (!password.equals(repeatPassword)) {
            alertManager.showAlert(Alert.AlertType.ERROR, "Registration Error",
                    "Passwords do not match.");
            return;
        }

        try {
            User newUser = authenticationService.register(username, password);
            alertManager.showAlert(Alert.AlertType.INFORMATION, "Success",
                    "Registration successful! Please log in with your new account.");
            sceneManager.switchToLoginScene(actionEvent);


        } catch (IllegalArgumentException e) {
            alertManager.showAlert(Alert.AlertType.ERROR, "Registration Error", e.getMessage());
        } catch (SQLException e) {
            alertManager.showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Could not connect to database. Please try again later.");
        } /*catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "System Error",
                    "Could not load the login screen. Please restart the application.");
        }*/
    }
}