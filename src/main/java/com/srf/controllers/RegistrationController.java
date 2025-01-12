package com.srf.controllers;

import com.srf.models.User;
import com.srf.services.AuthenticationService;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.SQLException;

public class RegistrationController {
    public TextField usernameTextField;
    public PasswordField passwordField;
    public PasswordField repeatPasswordField;
    public Button registerButton;

    private AuthenticationService authenticationService;

    // Konstuktor
    public void setAuthenticationService(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    public void onRegisterButton(ActionEvent actionEvent) {
        String username = usernameTextField.getText();
        String password = passwordField.getText();
        String repeatPassword = repeatPasswordField.getText();

        // Validacja tak podstawowa danych
        if (username.isEmpty() || password.isEmpty() || repeatPassword.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Registration Error", "Please fill in all fields.");
            return;
        }

        if (!password.equals(repeatPassword)) {
            showAlert(Alert.AlertType.ERROR, "Registration Error", "Passwords do not match.");
            return;
        }

        try {
            User newUser = authenticationService.register(username, password);
            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "Registration successful! Please log in with your new account.");

            // Ta scena, zamkya okno
            ((Stage) registerButton.getScene().getWindow()).close();

            /* I tu trzeba otworzyć logowanie?
           cos takeigo mi chat wypluł:
           SceneManager.switchToLogin();
             */

        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Registration Error", e.getMessage());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Could not connect to database. Please try again later.");
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