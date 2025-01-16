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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.Alert;
import javafx.application.Platform;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
//TODO odciążyć homecontroller czat mi taka opocje zaproponował: odciazyc.txt
public class HomeController {
    @FXML
    public Button SearchButton;
    @FXML
    public Button RefreshButton;
    @FXML
    public TextField SearchTextField;
    @FXML
    public VBox ListVbox;

    private RecommendationService recommendationService;
    private SearchService searchService;
    private RatingDAO ratingDAO;
    private MovieDAO movieDAO;
    private List<Movie> cachedRecommendations = new ArrayList<>();
    private ArrayList<Number> userRatings = new ArrayList<>();
    private int currentStartIndex = 0;
    private static final int PAGE_SIZE = 5;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final AlertManager alertManager = AlertManager.getInstance();

    private User currentUser;
    DataSingleton data = DataSingleton.getInstance();

    @FXML
    public void initialize() {
        try {
            ratingDAO = new RatingDAO(DatabaseConnection.getConnection());
            movieDAO = new MovieDAO(DatabaseConnection.getConnection());
            recommendationService = new RecommendationService(ratingDAO, movieDAO);
            searchService = new SearchService(movieDAO);
            currentUser = data.getUser();
        } catch (SQLException e) {
            Platform.runLater(() ->
                    alertManager.showAlert(
                            Alert.AlertType.ERROR,
                            "Błąd inicjalizacji",
                            "Nie udało się nawiązać połączenia z bazą danych: " + e.getMessage()
                    )
            );
        }
    }

    private void zapiszOcene(int movieId, double ratingValue) {
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
                    System.err.println("Rating save error: " + e.getMessage());
                    e.printStackTrace();
                    throw new SQLException("Błąd podczas zapisywania oceny: " + e.getMessage());
                }
            }
        };

        task.setOnSucceeded(event -> {
            System.out.println("Rating saved successfully");
            Platform.runLater(() -> {
                alertManager.showAlert(
                        Alert.AlertType.INFORMATION,
                        "Sukces",
                        "Ocena została zapisana pomyślnie"
                );
                cachedRecommendations.clear();
                refresh(true);
            });
        });

        task.setOnFailed(event -> {
            System.err.println("Rating save failed: " + task.getException().getMessage());
            Platform.runLater(() ->
                    alertManager.showAlert(
                            Alert.AlertType.ERROR,
                            "Błąd zapisu",
                            "Nie udało się zapisać oceny: " + task.getException().getMessage()
                    )
            );
        });

        executorService.submit(task);
    }

    @FXML
    public void refresh(boolean searchOrRecommend) {
        ListVbox.getChildren().clear();
        Label description = new Label();

        if (searchOrRecommend) {
            description.setText("Twoje osobiste rekomendacje");
            if (cachedRecommendations.isEmpty()) {
                Task<List<Movie>> recommendedMoviesTask =
                        recommendationService.generateRecommendationsAsync(currentUser.getId(), 20);


                recommendedMoviesTask.setOnSucceeded(event -> {
                    cachedRecommendations = recommendedMoviesTask.getValue();
                    currentStartIndex = 0;
                    displayNextBatch(true);
                });

                recommendedMoviesTask.setOnFailed(event -> {
                    Platform.runLater(() ->
                            alertManager.showAlert(
                                    Alert.AlertType.ERROR,
                                    "Błąd rekomendacji",
                                    "Nie udało się pobrać rekomendacji: " + recommendedMoviesTask.getException().getMessage()
                            )
                    );
                });

                new Thread(recommendedMoviesTask).start();
            }   else {
                displayNextBatch(true);
            }
        } else {
            description.setText("Wyniki wyszukiwania");
            String searchQuery = SearchTextField.getText();
            Task<List<Movie>> searchTask = new Task<>() {
                @Override
                protected List<Movie> call() {
                    return searchService.searchMovies(searchQuery);
                }
            };

            searchTask.setOnSucceeded(event -> displaySearchResults(searchTask.getValue()));

            new Thread(searchTask).start();
        }
        ListVbox.getChildren().add(description);
    }

    private void displayNextBatch(boolean isRecommendation) {
        try {
            int endIndex = Math.min(currentStartIndex + PAGE_SIZE,
                    isRecommendation ? cachedRecommendations.size() : PAGE_SIZE);

            if (isRecommendation && currentStartIndex >= cachedRecommendations.size()) {
                Label noMore = new Label("Nie ma więcej rekomendacji do wyświetlenia");
                ListVbox.getChildren().add(noMore);
                return;
            }

            for (int i = currentStartIndex; i < endIndex; i++) {
                HBox hBox = new HBox(10);

                Label title = new Label();
                Label genre = new Label();
                org.controlsfx.control.Rating ratingControl = new org.controlsfx.control.Rating(); // JavaFX Rating control

                if (isRecommendation) {
                    Movie movie = cachedRecommendations.get(i);
                    title.setText("Tytuł filmu: " + movie.getTitle());
                    genre.setText("Gatunek: " + movie.getGenre());

                    try {
                        Rating existingRating = ratingDAO.findRating(currentUser.getId(), movie.getId());
                        if (existingRating != null) {
                            ratingControl.setRating(existingRating.getRating());
                        }
                    } catch (SQLException e) {
                        System.err.println("Błąd podczas pobierania oceny: " + e.getMessage());
                    }

                    final int movieId = movie.getId();
                    ratingControl.ratingProperty().addListener((observable, oldValue, newValue) -> {
                        if (newValue != null && !oldValue.equals(newValue)) {
                            zapiszOcene(movieId, newValue.doubleValue());
                        }
                    });
                } else {
                    title.setText("Wynik wyszukiwania");
                    genre.setText("Gatunek");
                }

                hBox.getChildren().addAll(title, genre, ratingControl);
                ListVbox.getChildren().add(hBox);
            }

            if (isRecommendation) {
                currentStartIndex = endIndex;
            }
        } catch (Exception e) {
            Platform.runLater(() ->
                    alertManager.showAlert(
                            Alert.AlertType.ERROR,
                            "Błąd wyświetlania",
                            "Wystąpił błąd podczas wyświetlania filmów: " + e.getMessage()
                    )
            );
        }
    }

    private void displaySearchResults(List<Movie> movies) {
        Platform.runLater(() -> {
            if (movies.isEmpty()) {
                ListVbox.getChildren().add(new Label("Brak wyników wyszukiwania."));
                return;
            }

            for (Movie movie : movies) {
                HBox hBox = new HBox(10);

                Label title = new Label("Tytuł: " + movie.getTitle());
                Label genre = new Label("Gatunek: " + movie.getGenre());
                org.controlsfx.control.Rating ratingControl = new org.controlsfx.control.Rating(); // Dodajemy kontrolkę ocen

                try {
                    // Sprawdzamy, czy użytkownik już ocenił ten film
                    Rating existingRating = ratingDAO.findRating(currentUser.getId(), movie.getId());
                    if (existingRating != null) {
                        ratingControl.setRating(existingRating.getRating()); // Ustawiamy ocenę, jeśli istnieje
                    }
                } catch (SQLException e) {
                    System.err.println("Błąd podczas pobierania oceny: " + e.getMessage());
                }

                final int movieId = movie.getId();  // Pobieramy ID filmu

                // Listener reagujący na zmianę oceny
                ratingControl.ratingProperty().addListener((observable, oldValue, newValue) -> {
                    if (newValue != null && !oldValue.equals(newValue)) {
                        zapiszOcene(movieId, newValue.doubleValue()); // Zapisujemy ocenę
                    }
                });

                hBox.getChildren().addAll(title, genre, ratingControl);  // Dodajemy kontrolkę do GUI
                ListVbox.getChildren().add(hBox);
            }
        });
    }


    @FXML
    public void onSearchButton(ActionEvent actionEvent) {
        refresh(false);
    }

    @FXML
    public void onRefreshButton(ActionEvent actionEvent) {
        refresh(true);
    }

    public void onClose() {
        executorService.shutdown();
    }

    public void onGenerateRecommendationsButton(ActionEvent event) {
    }
}