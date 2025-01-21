package com.srf.utils;

import com.srf.models.Movie;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.io.IOException;

public class SceneManager {
    private static SceneManager instance;
    AlertManager alertManager = AlertManager.getInstance();

    public void showPrimaryScene() {
        String sceneName = "/com/srf/login.fxml";
        try {
            Stage stage = new Stage();
            Parent root = FXMLLoader.load(getClass().getResource(sceneName));

            stage.setScene(new Scene(root, 1280, 720));
            stage.setTitle("SRF");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            alertManager.showAlert(Alert.AlertType.ERROR, "Scene Error", "Could not load the scene: " + sceneName);
        }
    }

    public void switchToHomeScene(ActionEvent event) throws IOException {
        String sceneName = "/com/srf/home.fxml";
        switchScene(event, sceneName);
    }
    public void switchToLoginScene(ActionEvent event) throws IOException {
        String sceneName = "/com/srf/login.fxml";
        switchScene(event, sceneName);
    }
    public void switchToRegistrationScene(ActionEvent event) throws IOException {
        String sceneName = "/com/srf/registration.fxml";
        switchScene(event, sceneName);
    }
    public void switchToMovieCreatorScene(ActionEvent event) throws IOException {
        String sceneName = "/com/srf/movieCreator.fxml";
        switchScene(event, sceneName);
    }

    public void switchScene(ActionEvent event, String sceneName) throws IOException {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(sceneName));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.getScene().setRoot(root);
            stage.show();
        } catch (IOException e) {
            alertManager.showAlert(Alert.AlertType.ERROR, "Scene Error", "Could not load the scene: " + sceneName);
        }
    }

    public static SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    public void addMovie(VBox MainVbox) {
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("/com/srf/movie.fxml"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        MainVbox.getChildren().add(root);
    }
}