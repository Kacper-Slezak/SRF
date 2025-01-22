package com.srf.utils;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.Objects;

public class SceneManager {
    private static SceneManager instance;
    AlertManager alertManager = AlertManager.getInstance();
    Scene currentScene;

    public void showPrimaryScene() {
        String sceneName = "/com/srf/login.fxml";
        try {
            Stage stage = new Stage();
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(sceneName)));

            stage.setScene(new Scene(root, 1280, 720));
            currentScene = stage.getScene();

            stage.setTitle("SRF");
            stage.setMinWidth(220);
            stage.setMinHeight(400);
            stage.show();
        } catch (IOException e) {
            alertManager.showError( "Scene Error", "Could not load the scene: " + sceneName);
        }
    }
    public void switchToHomeScene(ActionEvent event) {
        String sceneName = "/com/srf/home.fxml";
        switchScene(event, sceneName, 670, 670);
    }
    public void switchToLoginScene(ActionEvent event) {
        String sceneName = "/com/srf/login.fxml";
        switchScene(event, sceneName, 400, 220);
    }
    public void switchToRegistrationScene(ActionEvent event) {
        String sceneName = "/com/srf/registration.fxml";
        switchScene(event, sceneName, 500, 220);
    }
    public void switchToMovieCreatorScene(ActionEvent event) {
        String sceneName = "/com/srf/movieCreator.fxml";
        switchScene(event, sceneName, 500, 340);
    }
    public void switchScene(ActionEvent event, String sceneName, int Height, int Width) {
        try {
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource(sceneName)));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.getScene().setRoot(root);
            currentScene = stage.getScene();
            stage.setMinWidth(Width);
            stage.setMinHeight(Height);
            stage.show();
        } catch (IOException e) {
            alertManager.showError( "Scene Error", "Could not load the scene: " + sceneName);
        }
    }
    public void addMovie(VBox MainVbox) {
        Parent root;
        try {
            root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/srf/movie.fxml")));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        MainVbox.getChildren().add(root);
    }
    public static SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }
}