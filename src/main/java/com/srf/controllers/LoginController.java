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
    private Scene scene;
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
       if (authenticationService == null) {
           showAlert(Alert.AlertType.ERROR, "System Error",
                   "System initialization failed. Please restart the application.");
           return;
       }

       String username = usernameTextField.getText();
       String password = passwordField.getText();

       if (username.isEmpty() || password.isEmpty()) {
           showAlert(Alert.AlertType.ERROR, "Login Error",
                   "Please fill in all fields.");
           return;
       }
       try {
           Optional<User> user = authenticationService.login(username, password);
           if (user.isPresent()) {
               showAlert(Alert.AlertType.INFORMATION, "Success", "Login successful!");
               switchScene(actionEvent, "home");
           }
           else{
               showAlert(Alert.AlertType.ERROR, "Login Error", "no user found");
           }

       } catch (IllegalArgumentException e) {
           showAlert(Alert.AlertType.ERROR, "Login Error", e.getMessage());
       } catch (SQLException e) {
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
