package com.srf.app;

import com.srf.utils.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;
import java.io.IOException;

public class Main extends Application {

    protected SceneManager sceneManager = new SceneManager();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        sceneManager.showPrimaryScene();
    }
}