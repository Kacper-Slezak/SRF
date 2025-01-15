package com.srf.controllers;

import com.srf.dao.UserDAO;
import com.srf.models.User;
import com.srf.utils.AlertManager;
import com.srf.utils.DataSingleton;
import com.srf.utils.DatabaseConnection;
import com.srf.utils.SceneManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import com.srf.services.AuthenticationService;

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

    private AuthenticationService authenticationService;
    private User currentUser;

    DataSingleton data = DataSingleton.getInstance();
    AlertManager alertManager = AlertManager.getInstance();
    SceneManager sceneManager = SceneManager.getInstance();

    @FXML
    public void initialize() {
        try {
            UserDAO userDAO = new UserDAO(DatabaseConnection.getConnection());
            this.authenticationService = new AuthenticationService(userDAO);
        } catch (SQLException e) {
            alertManager.showAlert(Alert.AlertType.ERROR,
                    "Database Error",
                    "Could not connect to database. Please try again later.");
        }
    }

    @FXML
    public void onLogInButton(ActionEvent actionEvent) throws IOException {
        if (authenticationService == null) {
            alertManager.showAlert(Alert.AlertType.ERROR, "System Error",
                    "System initialization failed. Please restart the application.");
            return;
        }

        String username = usernameTextField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            alertManager.showAlert(Alert.AlertType.ERROR, "Login Error",
                    "Please fill in all fields.");
            return;
        }
        try {
            Optional<User> user = authenticationService.login(username, password);
            if (user.isPresent()) {
                alertManager.showAlert(Alert.AlertType.INFORMATION, "Success", "Login successful!");
                currentUser = user.get();
                data.setUser(currentUser);
                sceneManager.switchToHomeScene(actionEvent);
            }
            else{
                alertManager.showAlert(Alert.AlertType.ERROR, "Login Error", "no user found");
            }

        } catch (IllegalArgumentException e) {
            alertManager.showAlert(Alert.AlertType.ERROR, "Login Error", e.getMessage());
        } catch (SQLException e) {
            alertManager.showAlert(Alert.AlertType.ERROR,
                    "Database Error",
                    "Could not connect to database. Please try again later.");
        }
    }

    @FXML
    public void onRegisterButton(ActionEvent actionEvent) throws IOException {
        sceneManager.switchToRegistrationScene(actionEvent);
    }
}
