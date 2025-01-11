package com.laby.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        //TODO naprawic ladowanie logowania
        //mam identycznie jak w samochodzie i tam tak dziala, co jest nie tak??
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("fxml/login.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), 400, 600);
        stage.setTitle("SRF");
        stage.setScene(scene);
        stage.show();
    }
}