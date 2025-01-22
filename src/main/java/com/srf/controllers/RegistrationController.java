package com.srf.controllers;

import com.srf.services.AuthenticationService;
import com.srf.dao.UserDAO;
import com.srf.utils.AlertManager;
import com.srf.utils.DatabaseConnection;
import com.srf.utils.SceneManager;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

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
    @FXML
    public Button logInButton;

    private AuthenticationService authenticationService;
    AlertManager alertManager = AlertManager.getInstance();
    SceneManager sceneManager = SceneManager.getInstance();

    public void initialize() {
        try {
            UserDAO userDAO = new UserDAO(DatabaseConnection.getConnection());
            this.authenticationService = new AuthenticationService(userDAO);
        } catch (SQLException e) {
            alertManager.showError( "Database Error",
                    "Could not connect to database. Please try again later.");
        }
    }

    @FXML
    public void onRegisterButton(ActionEvent actionEvent) {
        if (authenticationService == null) {
            alertManager.showError( "System Error",
                    "System initialization failed. Please restart the application.");
            return;
        }

        String username = usernameTextField.getText();
        String password = passwordField.getText();
        String repeatPassword = repeatPasswordField.getText();

        try {
            authenticationService.register(username, password, repeatPassword);
            alertManager.showInfo( "Success",
                    "Registration successful! Please log in with your new account.");
            sceneManager.switchToLoginScene(actionEvent);

        } catch (IllegalArgumentException e) {
            alertManager.showError( "Registration Error", e.getMessage());
        } catch (SQLException e) {
            alertManager.showError( "Database Error",
                    "Could not connect to database. Please try again later.");
        }
    }
    @FXML
    public void onLoginButton(ActionEvent event) {
        sceneManager.switchToLoginScene(event);
    }
}