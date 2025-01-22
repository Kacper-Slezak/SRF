package com.srf.controllers;

import com.srf.dao.UserDAO;
import com.srf.models.User;
import com.srf.utils.AlertManager;
import com.srf.utils.UserSingleton;
import com.srf.utils.DatabaseConnection;
import com.srf.utils.SceneManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import com.srf.services.AuthenticationService;

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

    private AuthenticationService authenticationService;

    UserSingleton data = UserSingleton.getInstance();
    AlertManager alertManager = AlertManager.getInstance();
    SceneManager sceneManager = SceneManager.getInstance();

    public void initialize() {
        try {
            UserDAO userDAO = new UserDAO(DatabaseConnection.getConnection());
            this.authenticationService = new AuthenticationService(userDAO);
        } catch (SQLException e) {
            alertManager.showError(
                    "Database Error",
                    "Could not connect to database. Please try again later.");
        }
    }

    @FXML
    public void onLogInButton(ActionEvent actionEvent) {
        if (authenticationService == null) {
            alertManager.showError( "System Error",
                    "System initialization failed. Please restart the application.");
            return;
        }

        String username = usernameTextField.getText();
        String password = passwordField.getText();

        try {
            Optional<User> user = authenticationService.login(username, password);

            if (user.isPresent()) {
                alertManager.showInfo( "Success", "Login successful!");
                User currentUser = user.get();
                data.setUser(currentUser);
                sceneManager.switchToHomeScene(actionEvent);
            } else {
                alertManager.showError( "Login Error", "Invalid username or password.");
            }
        } catch (IllegalArgumentException e) {
            alertManager.showError( "Validation Error", e.getMessage());
        } catch (SQLException e) {
            alertManager.showError(
                    "Database Error",
                    "Could not connect to database. Please try again later.");
        }
    }
    @FXML
    public void onRegisterButton(ActionEvent actionEvent) {
        sceneManager.switchToRegistrationScene(actionEvent);
    }
}