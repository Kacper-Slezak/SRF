package com.laby.app;

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
        Scene scene = new Scene(fxmlLoader.load(), 400, 600);
        stage.setTitle("SRF");
        stage.setScene(scene);
        stage.show();
    }
}
