package com.srf.utils;

import com.srf.controllers.HomeController;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneManager {
    private Scene scene;

    public SceneManager(Scene scene) {
        this.scene = scene;
    }
    public void switchToRegisterScene(ActionEvent event) throws IOException {
        String sceneName = "/com/srf/registration.fxml";
        switchScene(event, sceneName);
    }
    public void switchToHomeScene(ActionEvent event, int sessionId) throws IOException {
        String sceneName = "/com/srf/home.fxml";
        try {
            FXMLLoader root = new FXMLLoader(getClass().getResource(sceneName));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root.load());
            stage.setScene(scene);
            stage.setTitle("SRF");
            HomeController controller = root.<HomeController>getController();
            controller.initSessionID(this, sessionId);
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Scene Error", "Could not load the scene: " + sceneName);
        }
    }

    public void switchToLoginScene(ActionEvent event) throws IOException {
        String sceneName = "/com/srf/login.fxml";
        switchScene(event, sceneName);
    }

    public void switchScene(ActionEvent event, String sceneName) throws IOException {
        try {
            FXMLLoader root = new FXMLLoader(getClass().getResource(sceneName));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            scene = new Scene(root.load());
            stage.setScene(scene);
            stage.show();
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
