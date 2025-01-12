package com.srf.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader root = new FXMLLoader(getClass().getResource("/com/srf/login.fxml"));
        Scene homeScene = new Scene(root.load(), 400, 600);
        stage.setScene(homeScene);

        stage.setTitle("SRF");
        stage.show();
    }
}