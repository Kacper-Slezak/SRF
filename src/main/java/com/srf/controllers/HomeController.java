package com.srf.controllers;

import com.srf.dao.MovieDAO;
import com.srf.dao.RatingDAO;
import com.srf.models.Movie;
import com.srf.models.User;
import com.srf.services.RecommendationService;
import com.srf.services.SearchService;
import com.srf.utils.*;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.application.Platform;

import javafx.event.ActionEvent;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class HomeController {
    @FXML
    public Button SearchButton;
    @FXML
    public TextField SearchTextField;
    @FXML
    public VBox MainVbox;
    @FXML
    public Label NameLabel;
    @FXML
    public Button GenerateRecommendationsButton;
    @FXML
    public Button LogOutButton;
    @FXML
    public ScrollPane MainScrollPane;
    @FXML
    public Button AddMovieButton;

    private RecommendationService recommendationService;
    private SearchService searchService;
    private User currentUser;
    private List<Movie> recommendationsList = new ArrayList<>();
    private List<Movie> searchList = new ArrayList<>();

    private final Label moviesDescription = new Label();
    private final Label ratingsDescription = new Label();
    private final AlertManager alertManager = AlertManager.getInstance();
    private final SceneManager sceneManager = SceneManager.getInstance();
    private final UserSingleton userSingleton = UserSingleton.getInstance();
    private final MovieSingleton movieSingleton = MovieSingleton.getInstance();

    public void initialize() {
        try {
            RatingDAO ratingDAO = new RatingDAO(DatabaseConnection.getConnection());
            MovieDAO movieDAO = new MovieDAO(DatabaseConnection.getConnection());

            recommendationService = new RecommendationService(ratingDAO, movieDAO);
            searchService = new SearchService(movieDAO);

            currentUser = userSingleton.getUser();

            NameLabel.setText("Hello " + currentUser.getUsername()+"!");
            MainScrollPane.setFitToWidth(true);
            MainScrollPane.setStyle("-fx-focus-color: transparent; -fx-faint-focus-color: transparent;");

        } catch (SQLException e) {
            Platform.runLater(() ->
                    alertManager.showError(
                            "Initialization Error",
                            "Failed to connect to database: " + e.getMessage()
                    )
            );
        }
    }
    public void refresh(List<Movie> movies) {
        try {
            MainVbox.getChildren().clear();

            if (movies == null || movies.isEmpty()) {
                alertManager.showInfo("Movie information", "No movies to display");
                return;
            }

            setDescription();

            int endIndex = movies.size();
            for (int i = 0; i < endIndex; i++) {
                Movie movie = movies.get(i);
                movieSingleton.setMovieIndex(i+1);
                movieSingleton.setMovie(movie);
                sceneManager.addMovie(MainVbox);
            }
        } catch (Exception e) {
            alertManager.showError(
                    "Refresh Error",
                    "Failed to refresh results: " + e.getMessage()
            );
        }
    }

    private void setDescription() {
        HBox descriptionBox = new HBox();
        Region filler = new Region();
        HBox.setHgrow(filler, Priority.ALWAYS);
        descriptionBox.getChildren().addAll(moviesDescription, filler, ratingsDescription);
        ratingsDescription.setText("Your ratings:");
        ratingsDescription.setPadding(new Insets(10, 130, 5, 0));
        moviesDescription.setPadding(new Insets(10, 0, 5, 15));
        MainVbox.getChildren().add(descriptionBox);
    }
    public void setLoadingCursor(boolean isLoading) {
        Scene currentScene = MainVbox.getScene();
        if (isLoading) {
            currentScene.setCursor(Cursor.WAIT);
        } else {
            currentScene.setCursor(Cursor.DEFAULT);
        }
    }

    @FXML
    public void onSearchButton() {
        String searchQuery = SearchTextField.getText();
        Task<List<Movie>> searchTask = new Task<>() {
            @Override
            protected List<Movie> call() {
                return searchService.searchMovies(searchQuery);
            }
        };

        searchTask.setOnSucceeded(event -> {
            moviesDescription.setText("Search Results:");
            searchList = searchTask.getValue();
            refresh(searchList);
        });

        searchTask.setOnFailed(event -> Platform.runLater(() ->
                alertManager.showError(
                        "Search Error",
                        "Couldn't load search results: " + searchTask.getException().getMessage()
                )
        ));

        new Thread(searchTask).start();
    }
    @FXML
    public void onGenerateRecommendationsButton() {
        if (recommendationsList.isEmpty() || movieSingleton.getAddedRating()) {
            setLoadingCursor(true);

            recommendationService.invalidateCache(currentUser.getId());
            Task<List<Movie>> recommendedMoviesTask = recommendationService.generateRecommendationsAsync(currentUser.getId(), 50);

            recommendedMoviesTask.setOnSucceeded(event -> {
                setLoadingCursor(false);
                movieSingleton.setAddedRating(false);
                recommendationsList = recommendedMoviesTask.getValue();
                moviesDescription.setText("Your personal recommendations:");
                refresh(recommendationsList);
            });

            recommendedMoviesTask.setOnFailed(event -> {
                setLoadingCursor(false);
                Platform.runLater(() ->
                        alertManager.showError(
                                "Recommendations Error",
                                "Couldn't load recommendations: " + recommendedMoviesTask.getException().getMessage()
                        )
                );
            });

            new Thread(recommendedMoviesTask).start();
        } else {
            alertManager.showError("Generation error", "Rate a movie to generate new recommendations");
        }
    }
    @FXML
    public void onAddMovieButton(ActionEvent event) {
        sceneManager.switchToMovieCreatorScene(event);
    }
    @FXML
    public void onLogOutButton(ActionEvent event) {
        recommendationService.invalidateCache(currentUser.getId());
        alertManager.showInfo("Session Information", "Successfully logged out");
        sceneManager.switchToLoginScene(event);
    }
}