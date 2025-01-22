package com.srf.controllers;

import com.srf.dao.IMDbDAO;
import com.srf.dao.MovieDAO;
import com.srf.dao.RatingDAO;
import com.srf.models.Movie;
import com.srf.services.RatingService;
import com.srf.utils.AlertManager;
import com.srf.utils.DatabaseConnection;
import com.srf.utils.UserSingleton;
import com.srf.utils.SceneManager;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.Rating;

import java.sql.SQLException;

public class MovieCreatorController {
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

    AlertManager alertManager = AlertManager.getInstance();
    SceneManager sceneManager = SceneManager.getInstance();
    RatingService ratingService;

    public void initialize() throws SQLException {
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
        RatingDAO ratingDAO = new RatingDAO(DatabaseConnection.getConnection());
        ratingService = new RatingService(ratingDAO);
    }
    private String getGenres() {
        ObservableList list = genresCheckComboBox.getCheckModel().getCheckedItems();
        StringBuilder genres = new StringBuilder((String) list.get(0));
        for (int i = 1; i < list.size(); i++) {
            genres.append("|").append(list.get(i));
        }
        return genres.toString();
    }

    @FXML
    public void onCreateButton(ActionEvent event) {
        String title = titleTextField.getText();
        String genres = getGenres();
        //String IMDblink = IMDbLinkTextField.getText();
        //TODO I haven't linked the link to the link database

        Movie movie = new Movie(0, title, genres);
        int movieId;

        try {
            MovieDAO movieDAO = new MovieDAO(DatabaseConnection.getConnection());
            //IMDbDAO imdbDAO = new IMDbDAO(DatabaseConnection.getConnection());
            movieId = movieDAO.addMovie(movie);
            //IMDbDAO.addImdbLink
        } catch (SQLException e) {
            alertManager.showError(
                    "Database Error",
                    "Couldn't save the movie: " + e.getMessage());
            return;
        }

        double rating = movieRating.getRating();
        ratingService.saveRating(UserSingleton.getInstance().getUser().getId(), movieId, rating, null);

        sceneManager.switchToHomeScene(event);

    }
    @FXML
    public void onCancelButton(ActionEvent event) {
        sceneManager.switchToHomeScene(event);
    }
}