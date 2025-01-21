package com.srf.controllers;

import com.srf.dao.MovieDAO;
import com.srf.dao.RatingDAO;
import com.srf.models.Movie;
import com.srf.services.RatingService;
import com.srf.utils.AlertManager;
import com.srf.utils.MovieSingleton;
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

import java.io.IOException;
import java.sql.SQLException;

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
        ratingDAO = new RatingDAO(DatabaseConnection.getConnection());
        ratingService = new RatingService(ratingDAO);
    }

    public void onCreateButton(ActionEvent event) {
        String title = titleTextField.getText();
        String genres = getGenres();
        String IMDblink = IMDbLinkTextField.getText();

        Movie movie = new Movie(0, title, genres); // ID ustawiamy na 0, bo zostanie wygenerowane w bazie
        int movieId;

        try {
            // Używamy połączenia z bazy danych z DatabaseConnection
            MovieDAO movieDAO = new MovieDAO(DatabaseConnection.getConnection());
            movieId = movieDAO.addMovie(movie); // Dodanie filmu i uzyskanie ID
        } catch (SQLException e) {
            alertManager.showError("Błąd", "Nie udało się dodać filmu: " + e.getMessage());
            return;
        }

        // Pobieranie oceny z kontrolki i zapisywanie jej
        Double rating = movieRating.getRating();
        ratingService.saveRating(UserSingleton.getInstance().getUser().getId(), movieId, rating, null);

        try {
            sceneManager.switchToHomeScene(event);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
