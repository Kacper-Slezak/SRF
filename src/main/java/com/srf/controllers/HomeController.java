package com.srf.controllers;

import com.srf.dao.MovieDAO;
import com.srf.models.Movie;
import com.srf.models.User;
import com.srf.services.RecommendationService;
import com.srf.services.SearchService;
import com.srf.utils.DataSingleton;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.controlsfx.control.Rating;

import java.util.List;

public class HomeController {
    @FXML
    public TextField SearchTextField;
    @FXML
    public Button SearchButton;
    @FXML
    public Button RefreshButton;
    @FXML
    public VBox ListVbox;
    @FXML
    public Label NameLabel;

    private RecommendationService recommendationService;
    private SearchService searchService;

    private User currentUser;
    private int amountOfMovies = 7;

    DataSingleton data = DataSingleton.getInstance();

    @FXML
    public void initialize() {
        currentUser = data.getUser();
        NameLabel.setText(currentUser.getUsername());
    }

    @FXML
    public void onRefreshButton(ActionEvent actionEvent) {
        DBUpdate();

        ListVbox.getChildren().clear();

        Task<List<RecommendationService.MovieRecommendation>> recommendedMovies = recommendationService.generateRecommendationsAsync(currentUser.getId(), amountOfMovies);

        Label description = new Label();
        description.setText("Your personal recommendations");
        ListVbox.getChildren().add(description);

        for (int i = 0; i < amountOfMovies; i++){
            HBox hBox = new HBox();
            hBox.setSpacing(10);

            Label title = new Label();
            Label genre = new Label();
            Rating rating = new Rating();

            //TODO powiazac z rezultatem rekomendacji

            hBox.getChildren().addAll(title, genre, rating);
            ListVbox.getChildren().add(hBox);

            newListener(rating);
        }
    }

    @FXML
    public void onSearchButton(ActionEvent actionEvent) {
        DBUpdate();

        ListVbox.getChildren().clear();
        Label description = new Label();

        description.setText("Search results");

        List<Movie> searchedMoviesList = searchService.searchMoviesByTitle(SearchTextField.getText());

        ListVbox.getChildren().add(description);

        MovieDAO searchedMovieDAO;
        for (int i = 0; i < amountOfMovies; i++){
            HBox hBox = new HBox();
            hBox.setSpacing(10);

            Label title = new Label();
            Label genre = new Label();
            Rating rating = new Rating();

            //TODO powiazac z rezultatem wyszukiwania

            hBox.getChildren().addAll(title, genre, rating);

            ListVbox.getChildren().add(hBox);

            newListener(rating);
        }
    }

    private void DBUpdate() {
        //TODO dbupdate
    }

    private void newListener(Rating rating) {
        rating.ratingProperty().addListener((observable, oldValue, newValue) -> {
            //TODO logika listenera NIE MAM POJECIA JAK
        });
    }
}