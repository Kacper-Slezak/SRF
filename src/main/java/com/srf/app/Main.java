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
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/registration.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), 400, 600);
        stage.setTitle("SRF");
        stage.setScene(scene);
        stage.show();
    }
    public void Login(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("fxml/login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 400, 600);
        stage.setTitle("SRF");
        stage.setScene(scene);
        stage.show();
        //TODO scene manager??
        //https://www.youtube.com/watch?v=RifjriAxbw8
    }
}