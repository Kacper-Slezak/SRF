package com.srf.controllers;

import com.srf.dao.MovieDAO;
import com.srf.dao.RatingDAO;
import com.srf.dao.imdbDAO;
import com.srf.models.Movie;
import com.srf.models.User;
import com.srf.models.Rating;
import com.srf.services.RatingService;
import com.srf.services.RecommendationService;
import com.srf.services.SearchService;
import com.srf.services.imdbService;
import com.srf.utils.DataSingleton;
import com.srf.utils.DatabaseConnection;
import com.srf.utils.AlertManager;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.application.Platform;

import javafx.event.ActionEvent;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeController {
    @FXML
    public Button SearchButton;
    @FXML
    public Button RefreshButton;
    @FXML
    public TextField SearchTextField;
    @FXML
    public VBox ListVbox;
    @FXML
    public Button PlusButton;
    @FXML
    public Label NameLabel;
    @FXML
    public VBox RatingVbox;


    private RecommendationService recommendationService;
    private SearchService searchService;
    private RatingService ratingService;

    private RatingDAO ratingDAO;

    private List<Movie> recommendationsList = new ArrayList<>();
    private List<Movie> searchList = new ArrayList<>();

    private boolean previousWasRecommend = false;
    private boolean isNewRatingAdded = false;
    private int currentStartIndex = 0;
    private static final int pageSize = 6;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final AlertManager alertManager = AlertManager.getInstance();

    private User currentUser;
    DataSingleton data = DataSingleton.getInstance();
    Label description = new Label();
    Label description2 = new Label();

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
            currentUser = data.getUser();
            NameLabel.setText(currentUser.getUsername());
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
            description.setText("Search Results:");
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
        if (recommendationsList.isEmpty() || isNewRatingAdded) {
            recommendationService.invalidateCache(currentUser.getId());
            Task<List<Movie>> recommendedMoviesTask = recommendationService.generateRecommendationsAsync(currentUser.getId(), 20);

            recommendedMoviesTask.setOnSucceeded(event -> {
                    isNewRatingAdded = false;
                recommendationsList = recommendedMoviesTask.getValue();
                currentStartIndex = 0; // Reset indeksu
                previousWasRecommend = true; // Oznacz, że wyświetlamy rekomendacje
                description.setText("Your personal recommendations:");
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
            currentStartIndex = 0; // Reset indeksu, jeśli lista już istnieje
            previousWasRecommend = true; // Oznacz, że wyświetlamy rekomendacje
            description.setText("Your personal recommendations:");
            refresh(recommendationsList); // Wyświetl pierwszą paczkę
        }
    }
    @FXML
    public void refresh(List<Movie> movies) {
        try {
            ListVbox.getChildren().clear();
            RatingVbox.getChildren().clear();

            if (movies == null || movies.isEmpty()) {
                description.setText("No more data to display.");
                return;
            }
            if (currentStartIndex >= movies.size()) {
                description.setText("No more data to display.");
                return;
            }

            description2.setText("Your ratings:");
            ListVbox.getChildren().add(description);
            RatingVbox.getChildren().add(description2);
            int endIndex = Math.min(currentStartIndex + pageSize, movies.size());

            for (int i = currentStartIndex; i < endIndex; i++) {
                HBox hBox = new HBox(10);
                VBox vBox = new VBox();
                Label title = new Label();
                Label genre = new Label();
                org.controlsfx.control.Rating ratingControl = new org.controlsfx.control.Rating();
                Button IMDb = new Button("IMDb");

                Movie movie = movies.get(i);
                title.setText(movie.getTitle());
                title.setFont(Font.font("system", FontWeight.BOLD, FontPosture.REGULAR, 15));
                genre.setText(movie.getGenre());
                //ratingControl.setPrefHeight(38);
                ratingControl.setPadding(new Insets(0, 0, 6, 0));

                IMDb.setOnAction(event -> onIMDbButtonClick(movie.getId()));
                try {
                    Rating existingRating = ratingDAO.findRating(currentUser.getId(), movie.getId());
                    if (existingRating != null) {
                        ratingControl.setRating(existingRating.getRating());
                    }
                    else{
                        ratingControl.setRating(0);
                    }
                } catch (SQLException e) {
                    alertManager.showError("Rating fetch error", e.getMessage());
                }

                // Add rating change listener
                final Movie finalMovie = movie;
                ratingControl.ratingProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal != null && !newVal.equals(oldVal)) {
                        isNewRatingAdded = true;
                        ratingService.saveRating(
                                currentUser.getId(),
                                finalMovie.getId(),
                                newVal.doubleValue(),
                                () -> {
                                }
                        );
                    }
                });

                vBox.getChildren().addAll(title, genre);
                ListVbox.getChildren().add(vBox);
                hBox.getChildren().addAll(ratingControl, IMDb);
                hBox.setAlignment(Pos.CENTER_RIGHT);
                RatingVbox.getChildren().add(hBox);
            }

            currentStartIndex = endIndex;
        } catch (Exception e) {
            alertManager.showError(
                    "Refresh Error",
                    "Failed to refresh results: " + e.getMessage()
            );
        }
    }

    @FXML
    private void onIMDbButtonClick(int movieId) {
        String url = imdbService.fetchImdbUrl(movieId);
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
        //TODO Adding new movie
    }
    @FXML
    public void onRefreshButton(ActionEvent event) {
        if (previousWasRecommend) {
            refresh(recommendationsList);
        } else {
            refresh(searchList);
        }


    }

    public void onClose() {
        executorService.shutdown();
        ratingService.shutdown();
    }
}