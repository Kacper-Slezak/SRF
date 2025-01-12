package com.srf.controllers;

import com.srf.models.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import com.srf.services.AuthenticationService;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

public class LoginController {
    public TextField usernameTextField;
    public PasswordField passwordField;
    public Button logInButton;
    public Button registerButton;

    private Stage stage;
    private Scene scene;

    public void switchToHomeScene(ActionEvent event) throws IOException {
        FXMLLoader root = new FXMLLoader(getClass().getResource("com/srf/home.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root.load());
        stage.setScene(scene);
        stage.show();
    }

    public void switchToRegisterScene(ActionEvent event) throws IOException {
        FXMLLoader root = new FXMLLoader(getClass().getResource("com/srf/registration.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root.load());
        stage.setScene(scene);
        stage.show();
    }

    public void onLogInButton(ActionEvent actionEvent) throws IOException {
        AuthenticationService auth = new AuthenticationService();
        User currentUser = auth.login(usernameTextField, passwordField);
        if (currentUser != Optional.empty()) {
            switchToHomeScene(actionEvent);
        }

    }

    @FXML
    public void onRegisterButton(ActionEvent actionEvent) throws IOException {
        switchToRegisterScene(actionEvent);
    }
}
