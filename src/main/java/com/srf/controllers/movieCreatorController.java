package com.srf.controllers;

import com.srf.dao.RatingDAO;
import com.srf.services.RatingService;
import com.srf.utils.AlertManager;
import com.srf.utils.UserSingleton;
import com.srf.utils.SceneManager;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.Rating;

import java.io.IOException;

public class movieCreatorController {
    @FXML
    public TextField titleTextField;
    @FXML
    public CheckComboBox genresCheckComboBox;
    @FXML
    public TextField IMDbLinkTextField;
    @FXML
    public Button createButton;
    @FXML
    public Button cancelButton;
    @FXML
    public Rating movieRating;

    private RatingDAO ratingDAO;

    UserSingleton data = UserSingleton.getInstance();
    AlertManager alertManager = AlertManager.getInstance();
    SceneManager sceneManager = SceneManager.getInstance();
    RatingService ratingService;

    public void initialize() {
        String [] allGenres = {
                "Action",
                "Adventure",
                "Animation",
                "Comedy",
                "Drama",
                "Fantasy",
                "Horror",
                "Musical",
                "Romance",
                "Sci-Fi",
                "Mystery",
                "Thriller",
                "Western",
                "War",
                "IMAX"
        };
        genresCheckComboBox.getItems().addAll(allGenres);
        ratingService = new RatingService(ratingDAO);
    }

    public void onCreateButton(ActionEvent event) {
        int ID = getID();
        String title = titleTextField.getText();
        String genres = getGenres();
        String IMDblink = IMDbLinkTextField.getText();
        writeMovie(ID, title, genres, IMDblink);
        Double rating = movieRating.getRating();
        ratingService.saveRating(data.getUser().getId(), ID, rating, null);
        try {
            sceneManager.switchToHomeScene(event);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeMovie(int id, String title, String genres, String imDblink) {
        //TODO wpisanie do DB
    }

    private int getID() {
        //TODO znalezienie nieuzywanego ID
        return 0;
    }

    private String getGenres() {
        ObservableList list = genresCheckComboBox.getCheckModel().getCheckedItems();
        String genres = (String) list.get(0);
        for (int i = 1; i < list.size(); i++) {
            genres = genres + "|" + list.get(i);
        }
        return genres;
    }

    public void onCancelButton(ActionEvent event) throws IOException {
        try {
            sceneManager.switchToHomeScene(event);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
