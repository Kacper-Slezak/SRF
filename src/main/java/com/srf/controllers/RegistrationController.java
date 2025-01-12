package com.srf.controllers;

import com.srf.models.User;
import com.srf.services.AuthenticationService;
import com.srf.dao.UserDAO;
import com.srf.utils.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

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

    private Stage stage;
    private Scene scene;

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
    public void onRegisterButton(ActionEvent actionEvent) throws IOException {
        if (authenticationService == null) {
            showAlert(Alert.AlertType.ERROR, "System Error",
                    "System initialization failed. Please restart the application.");
            return;
        }

        String username = usernameTextField.getText();
        String password = passwordField.getText();
        String repeatPassword = repeatPasswordField.getText();

        try {
            // Wywołanie logiki rejestracji w serwisie
            User newUser = authenticationService.register(username, password, repeatPassword);
            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "Registration successful! Please log in with your new account.");
            switchToLoginScene(actionEvent);

        } catch (IllegalArgumentException e) {
            showAlert(Alert.AlertType.ERROR, "Registration Error", e.getMessage());
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Database Error",
                    "Could not connect to database. Please try again later.");
        }
    }


    public void switchToLoginScene(ActionEvent event) throws IOException {
        FXMLLoader root = new FXMLLoader(getClass().getResource("/com/srf/login.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root.load());
        stage.setScene(scene);
        stage.show();
    }
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }


}