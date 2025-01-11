module com.lab.srf {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires jbcrypt;
    requires java.sql;
    requires ejml.simple;

    opens com.srf to javafx.fxml;
    exports com.srf.controllers;
    exports com.srf.app;
    opens com.srf.app to javafx.fxml;
    exports com.srf.dao;
    opens com.srf.dao to javafx.fxml;
    exports com.srf.services;
    opens com.srf.services to javafx.fxml;
    exports com.srf.utils;
    opens com.srf.utils to javafx.fxml;
    exports com.srf.models;
    opens com.srf.models to javafx.fxml;
}