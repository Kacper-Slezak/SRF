package com.srf.utils;

import javafx.scene.control.Alert;

public class AlertManager {
    private static AlertManager instance;

    public AlertManager() {
        AlertManager alertManager = this;
    }

    public void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    public static AlertManager getInstance() {
        if (instance == null) {
            instance = new AlertManager();
        }
        return instance;
    }
}
