package com.srf.controllers;

import com.srf.dao.MovieDAO;
import com.srf.dao.RatingDAO;
import com.srf.models.Movie;
import com.srf.models.User;
import com.srf.models.Rating;
import com.srf.services.RatingService;
import com.srf.services.RecommendationService;
import com.srf.services.SearchService;
import com.srf.utils.DataSingleton;
import com.srf.utils.DatabaseConnection;
import com.srf.utils.AlertManager;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.application.Platform;

import javafx.event.ActionEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//TODO odciążyć homecontroller scroll down      
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


    private RecommendationService recommendationService;
    private SearchService searchService;
    private RatingService ratingService;

    private RatingDAO ratingDAO;

    private List<Movie> recommendationsList = new ArrayList<>();
    private List<Movie> searchList = new ArrayList<>();

    private boolean previousWasRecommend = false;
    private int currentStartIndex = 0;
    private static final int pageSize = 6;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final AlertManager alertManager = AlertManager.getInstance();

    private User currentUser;
    DataSingleton data = DataSingleton.getInstance();
    Label description = new Label();

    @FXML
    public void initialize() {
        try {
            ratingDAO = new RatingDAO(DatabaseConnection.getConnection());
            MovieDAO movieDAO = new MovieDAO(DatabaseConnection.getConnection());
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
        ListVbox.getChildren().clear();
        description.setText("Wyniki wyszukiwania:");

        String searchQuery = SearchTextField.getText();
        Task<List<Movie>> searchTask = new Task<>() {
            @Override
            protected List<Movie> call() {
                return searchService.searchMovies(searchQuery);
            }
        };

        searchTask.setOnSucceeded(event -> {
            searchList = searchTask.getValue();
            currentStartIndex = 0; // Reset indeksu
            previousWasRecommend = false; // Oznacz, że wyświetlamy wyniki wyszukiwania
            ListVbox.getChildren().add(description);
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
        ListVbox.getChildren().clear();

        if (recommendationsList.isEmpty()) {
            Task<List<Movie>> recommendedMoviesTask = recommendationService.generateRecommendationsAsync(currentUser.getId(), 20);

            recommendedMoviesTask.setOnSucceeded(event -> {
                recommendationsList = recommendedMoviesTask.getValue();
                currentStartIndex = 0; // Reset indeksu
                previousWasRecommend = true; // Oznacz, że wyświetlamy rekomendacje
                description.setText("Your personal recommendations:");
                ListVbox.getChildren().add(description);
                refresh(recommendationsList); // Wyświetl pierwszą paczkę
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
            if (movies == null || movies.isEmpty()) {
                description.setText("No more data to display.");
                return;
            }

            int endIndex = Math.min(currentStartIndex + pageSize, movies.size());

            if (currentStartIndex >= movies.size()) {
                description.setText("No more data to display.");
                return;
            }

            ListVbox.getChildren().clear();
            ListVbox.getChildren().add(description);

            for (int i = currentStartIndex; i < endIndex; i++) {
                HBox hBox = new HBox(10);

                Label title = new Label();
                Label genre = new Label();
                org.controlsfx.control.Rating ratingControl = new org.controlsfx.control.Rating();

                Movie movie = movies.get(i);
                title.setText("Movie title: " + movie.getTitle());
                genre.setText("Genre: " + movie.getGenre());

                try {
                    Rating existingRating = ratingDAO.findRating(currentUser.getId(), movie.getId());
                    if (existingRating != null) {
                        ratingControl.setRating(existingRating.getRating());
                    }
                } catch (SQLException e) {
                    alertManager.showError("Rating fetch error", e.getMessage());
                }

                // Add rating change listener
                final Movie finalMovie = movie;
                ratingControl.ratingProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal != null && !newVal.equals(oldVal)) {
                        ratingService.saveRating(
                                currentUser.getId(),
                                finalMovie.getId(),
                                newVal.doubleValue(),
                                () -> {
                                }
                        );
                    }
                });

                hBox.getChildren().addAll(title, genre, ratingControl);
                ListVbox.getChildren().add(hBox);
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
    public void onSearchButton(ActionEvent actionEvent) {
        search();
    }
    @FXML
    public void onGenerateRecommendationsButton(ActionEvent event) {
        recommend();
    }
    @FXML
    public void onPlusButton(ActionEvent event) {

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