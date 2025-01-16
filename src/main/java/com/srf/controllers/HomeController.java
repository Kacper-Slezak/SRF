package com.srf.controllers;

import com.srf.dao.MovieDAO;
import com.srf.dao.RatingDAO;
import com.srf.models.Movie;
import com.srf.models.User;
import com.srf.models.Rating;
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

    private RatingDAO ratingDAO;
    private MovieDAO movieDAO;

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
            movieDAO = new MovieDAO(DatabaseConnection.getConnection());
            recommendationService = new RecommendationService(ratingDAO, movieDAO);
            searchService = new SearchService(movieDAO);
            currentUser = data.getUser();
            NameLabel.setText(currentUser.getUsername());
        } catch (SQLException e) {
            Platform.runLater(() ->
                    alertManager.showError(
                            "Błąd inicjalizacji",
                            "Nie udało się nawiązać połączenia z bazą danych: " + e.getMessage()
                    )
            );
        }
    }
    @FXML
    public void search() {
        //CO TU SIE ODJANIEPAWLA bo daje nulla
        String searchQuery = SearchTextField.getText();
        Task<List<Movie>> searchTask = new Task<>() {
            @Override
            protected List<Movie> call() {
                return searchService.searchMovies(searchQuery);
            }
        };

        //null searchlist
        searchList = searchTask.getValue();

        searchTask.setOnSucceeded(event -> {
            currentStartIndex = 0;
            previousWasRecommend = false;
            description.setText("Wyniki wyszukiwania:");
            refresh(searchList);
        });


        new Thread(searchTask).start();

        ListVbox.getChildren().add(description);
    }
    @FXML
    private void recommend() {
        if (recommendationsList.isEmpty()) {
            Task<List<Movie>> recommendedMoviesTask = recommendationService.generateRecommendationsAsync(currentUser.getId(), 20);

            recommendedMoviesTask.setOnSucceeded(event -> {
                recommendationsList = recommendedMoviesTask.getValue();
                currentStartIndex = 0;
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
        }   else {
            previousWasRecommend = true;
            refresh(recommendationsList);
        }
        ListVbox.getChildren().add(description);
    }
    @FXML
    public void refresh(List<Movie> movies) {
        try{
            Platform.runLater(() -> {
                    try {
                        ListVbox.getChildren().clear();

                        int endIndex = Math.min(currentStartIndex + pageSize, previousWasRecommend ? movies.size() : pageSize);

                        if (previousWasRecommend && currentStartIndex >= movies.size()) {
                            description.setText("No more recommendations.");
                            return;
                        }

                        for (int i = currentStartIndex; i < endIndex; i++) {
                            HBox hBox = new HBox(10);

                            Label title = new Label();
                            Label genre = new Label();
                            org.controlsfx.control.Rating ratingControl = new org.controlsfx.control.Rating(); // JavaFX Rating control

                            Movie movie = movies.get(i);
                            title.setText("Tytuł filmu: " + movie.getTitle());
                            genre.setText("Gatunek: " + movie.getGenre());

                            try {
                                Rating existingRating = ratingDAO.findRating(currentUser.getId(), movie.getId());
                                if (existingRating != null) {
                                    ratingControl.setRating(existingRating.getRating());
                                }
                            } catch (SQLException e) {
                                alertManager.showError(
                                        "Błąd podczas pobierania oceny",
                                        e.getMessage()
                                );}

                            final int movieId = movie.getId();
                            ratingControl.ratingProperty().addListener((observable, oldValue, newValue) -> {
                                if (newValue != null && !oldValue.equals(newValue)) {
                                    saveRating(movieId, newValue.doubleValue());
                                }
                            });

                            hBox.getChildren().addAll(title, genre, ratingControl);
                            ListVbox.getChildren().add(hBox);
                        }

                        currentStartIndex = endIndex;
                    } catch (Exception e) {
                        Platform.runLater(() ->
                                alertManager.showError(
                                        "Błąd wyświetlania filmów",
                                        e.getMessage()
                                )
                        );
                    }
            });
        } catch (NullPointerException e) {
            alertManager.showError(
                    "Błąd listy filmów",
                    "Upewnij się, że wygenerowałeś listę filmów lub wyszukałeś zanim odświeżyłeś"
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
        } else{
            refresh(searchList);
        }

    }
    //TODO rating service do zrobienia, imo jedyne pozyteczne odciazenie homea
    private void saveRating(int movieId, double ratingValue) {
        System.out.println("Saving rating: movieId=" + movieId + ", rating=" + ratingValue);
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws SQLException {
                try {
                    // Najpierw sprawdzamy czy ocena już istnieje
                    Rating existingRating = ratingDAO.findRating(currentUser.getId(), movieId);

                    if (existingRating != null) {
                        // Aktualizacja istniejącej oceny
                        existingRating.setRating(ratingValue);
                        ratingDAO.updateRating(existingRating);
                        System.out.println("Updated existing rating");
                    } else {
                        // Dodanie nowej oceny
                        Rating nowaOcena = new Rating(
                                currentUser.getId(),
                                movieId,
                                ratingValue
                        );
                        ratingDAO.addRating(nowaOcena);
                        System.out.println("Added new rating");
                    }
                    return null;
                } catch (Exception e) {
                    alertManager.showError(
                            "Rating save error",
                            e.getMessage()
                    );
                    throw new SQLException("Błąd podczas zapisywania oceny: " + e.getMessage());
                }
            }
        };

        task.setOnSucceeded(event -> {
            System.out.println("Rating saved successfully");
            Platform.runLater(() -> {
                alertManager.showInfo(
                        "Sukces",
                        "Ocena została zapisana pomyślnie"
                );
                recommendationsList.clear();
            });
        });

        task.setOnFailed(event -> {
            System.err.println("Rating save failed: " + task.getException().getMessage());
            Platform.runLater(() ->
                    alertManager.showError(
                            "Błąd zapisu",
                            "Nie udało się zapisać oceny: " + task.getException().getMessage()
                    )
            );
        });

        executorService.submit(task);
    }

    public void onClose() {
        executorService.shutdown();
    }
}