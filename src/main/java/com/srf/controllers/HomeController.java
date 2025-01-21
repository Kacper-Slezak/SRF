package com.srf.controllers;

import com.srf.dao.MovieDAO;
import com.srf.dao.RatingDAO;
import com.srf.dao.imdbDAO;
import com.srf.models.Movie;
import com.srf.models.User;
import com.srf.services.RatingService;
import com.srf.services.RecommendationService;
import com.srf.services.SearchService;
import com.srf.services.imdbService;
import com.srf.utils.*;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
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

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeController {
    @FXML
    public Button SearchButton;
    @FXML
    public TextField SearchTextField;
    @FXML
    public VBox MainVbox;
    @FXML
    public Button PlusButton;
    @FXML
    public Label NameLabel;
    @FXML
    public Button GenerateRecommendationsButton;
    @FXML
    public Button LogOutButton;
    @FXML
    public ScrollPane MainScrollPane;

    private RecommendationService recommendationService;
    private SearchService searchService;
    private RatingService ratingService;
    private RatingDAO ratingDAO;
    private List<Movie> recommendationsList = new ArrayList<>();
    private List<Movie> searchList = new ArrayList<>();
    private User currentUser;
    private Label moviesDescription = new Label();
    private Label ratingsDescription = new Label();

    private boolean previousWasRecommend = false;
    private int currentStartIndex = 0;
    private static final int pageSize = 7;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final AlertManager alertManager = AlertManager.getInstance();
    private final SceneManager sceneManager = SceneManager.getInstance();
    private final UserSingleton userSingleton = UserSingleton.getInstance();
    private final MovieSingleton movieSingleton = MovieSingleton.getInstance();

    @FXML
    public void initialize() {
        try {
            ratingDAO = new RatingDAO(DatabaseConnection.getConnection());
            MovieDAO movieDAO = new MovieDAO(DatabaseConnection.getConnection());
            imdbDAO imdbDAO = new imdbDAO   (DatabaseConnection.getConnection());
            imdbService imdbService = new imdbService(imdbDAO);
            recommendationService = new RecommendationService(ratingDAO, movieDAO);
            searchService = new SearchService(movieDAO);
            ratingService = new RatingService(ratingDAO);
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
    @FXML
    public void search() {
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
            currentStartIndex = 0; // Reset indeksu
            previousWasRecommend = false; // Oznacz, że wyświetlamy wyniki wyszukiwania
            refresh(searchList); // Wyświetl pierwszą paczkę
        });

        searchTask.setOnFailed(event -> {
            Platform.runLater(() ->
                    alertManager.showError(
                            "Błąd wyszukiwania",
                            "Nie udało się pobrać wyników wyszukiwania: " + searchTask.getException().getMessage()
                    )
            );
        });

        new Thread(searchTask).start();
    }
    @FXML
    private void recommend() {
        if (recommendationsList.isEmpty() || movieSingleton.getAddedRating()) {
            recommendationService.invalidateCache(currentUser.getId());
            Task<List<Movie>> recommendedMoviesTask = recommendationService.generateRecommendationsAsync(currentUser.getId(), 20);

            recommendedMoviesTask.setOnSucceeded(event -> {
                movieSingleton.setAddedRating(false);
                recommendationsList = recommendedMoviesTask.getValue();
                currentStartIndex = 0; // Reset indeksu
                previousWasRecommend = true; // Oznacz, że wyświetlamy rekomendacje
                moviesDescription.setText("Your personal recommendations:");
                refresh(recommendationsList);
            });

            recommendedMoviesTask.setOnFailed(event -> {
                Platform.runLater(() ->
                        alertManager.showError(
                                "Błąd rekomendacji",
                                "Nie udało się pobrać rekomendacji: " + recommendedMoviesTask.getException().getMessage()
                        )
                );
            });

            new Thread(recommendedMoviesTask).start();
        } else {
            alertManager.showInfo("Generation error", "Rate a movie to generate new recommendations");
        }
    }
    @FXML
    public void refresh(List<Movie> movies) {
        try {
            MainVbox.getChildren().clear();

            if (movies == null || movies.isEmpty()) {
                alertManager.showInfo("Movie information", "No more movies to display");
                return;
            }

            HBox descriptionBox = new HBox();
            Region filler = new Region();
            descriptionBox.setHgrow(filler, Priority.ALWAYS);
            descriptionBox.getChildren().addAll(moviesDescription, filler, ratingsDescription);
            ratingsDescription.setText("Your ratings:");
            ratingsDescription.setPadding(new Insets(10, 130, 5, 0));
            moviesDescription.setPadding(new Insets(10, 0, 5, 15));
            MainVbox.getChildren().add(descriptionBox);

            int endIndex = movies.size();
            for (int i = currentStartIndex; i < endIndex; i++) {
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

    @FXML
    public void onSearchButton(ActionEvent actionEvent) {
        search();
    }
    @FXML
    public void onGenerateRecommendationsButton(ActionEvent event) {
        recommend();
    }
    @FXML
    public void onPlusButton(ActionEvent event) {
        try {
            //TODO check if user rated enough
            sceneManager.switchToMovieCreatorScene(event);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @FXML
    public void onLogOutButton(ActionEvent event) {
        try {
            alertManager.showInfo("Session Information", "Successfully logged out");
            sceneManager.switchToLoginScene(event);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}