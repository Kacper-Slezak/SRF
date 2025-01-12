module com.srf {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires jbcrypt;
    requires java.sql;
    requires ejml.simple;

    // Since your Main class is in com.srf.app
    opens com.srf.app to javafx.fxml;
    exports com.srf.app;

    // These should match your actual package structure
    opens com.srf.controllers to javafx.fxml;
    exports com.srf.controllers;

    opens com.srf.dao to javafx.fxml;
    exports com.srf.dao;

    opens com.srf.services to javafx.fxml;
    exports com.srf.services;

    opens com.srf.utils to javafx.fxml;
    exports com.srf.utils;

    opens com.srf.models to javafx.fxml;
    exports com.srf.models;
}