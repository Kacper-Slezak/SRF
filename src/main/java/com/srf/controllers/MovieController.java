package com.srf.controllers;

import com.srf.dao.RatingDAO;
import com.srf.models.Movie;
import com.srf.models.User;
import com.srf.services.RatingService;
import com.srf.services.imdbService;
import com.srf.utils.AlertManager;
import com.srf.utils.DatabaseConnection;
import com.srf.utils.UserSingleton;
import com.srf.utils.MovieSingleton;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.controlsfx.control.Rating;

import java.sql.SQLException;

public class MovieController {
    public Label TitleLabel;
    public Label GenresLabel;
    public Rating MovieRating;
    public Button IMDbButton;

    private RatingDAO ratingDAO;
    private RatingService ratingService;
    private final AlertManager alertManager = AlertManager.getInstance();
    private final MovieSingleton movieSingleton = MovieSingleton.getInstance();
    private final UserSingleton userSingleton = UserSingleton.getInstance();
    public Movie movie;
    public User user;
    public int movieID;
    public int userID;

    public void initialize() {
        try {
            ratingDAO = new RatingDAO(DatabaseConnection.getConnection());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        ratingService = new RatingService(ratingDAO);

        movie = movieSingleton.getMovie();
        user = userSingleton.getUser();

        movieID = movieSingleton.getMovie().getId();
        userID = user.getId();

        TitleLabel.setText(movie.getTitle());
        GenresLabel.setText(movie.getGenre());

        setExistingRating();
        setRatingListener();

    }

    private void setRatingListener() {
        MovieRating.ratingProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.equals(oldVal)) {
                movieSingleton.setAddedRating(true);
                ratingService.saveRating(
                        userID,
                        movieID,
                        newVal.doubleValue(),
                        () -> {
                        }
                );
            }
        });
    }

    private void setExistingRating() {
        try {
            com.srf.models.Rating existingRating = ratingDAO.findRating(userID, movieID);
            if (existingRating != null) {
                MovieRating.setRating(existingRating.getRating());
            }
            else {
                MovieRating.setRating(0);
            }
        } catch (SQLException e) {
            alertManager.showError("Rating fetch error", e.getMessage());
        }
    }

    public void onIMDbButton(ActionEvent event) {
        String url = imdbService.fetchImdbUrl(movieID);
        if (url != null) {
            try {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
            } catch (Exception e) {
                alertManager.showError("Error", "Unable to open the link: " + e.getMessage());
            }
        } else {
            alertManager.showError("Error", "No IMDb link available for this movie.");
        }

    }
}