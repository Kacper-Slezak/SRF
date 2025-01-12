package com.srf.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import com.srf.services.AuthenticationService;
import com.srf.app.Main;

public class LoginController {
    public TextField usernameTextField;
    public PasswordField passwordField;
    public Button logInButton;
    public Button registerButton;

    public void onLogInButton(ActionEvent actionEvent) {
        //TODO polaczyc z logowaniem
        //AuthenticationService.login(usernameTextField, passwordField);
        //nie dziala z jakiegos powodu
    }

    @FXML
    public void onRegisterButton(ActionEvent actionEvent) {
        //TODO polaczyc ze zmiana sceny
    }
}
