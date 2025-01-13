package com.srf.controllers;

import com.srf.services.RecommendationService;
import com.srf.utils.SceneManager;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.controlsfx.control.Rating;

import java.util.ArrayList;
import java.util.List;

public class HomeController {
    @FXML
    public Button SearchButton;
    @FXML
    public org.controlsfx.control.Rating Rating;
    @FXML
    public TextField SearchTextField;
    @FXML
    public Label TitleLabel;
    @FXML
    public Label GenreLabel;
    @FXML
    public Button RefreshButton;
    @FXML
    public VBox ListVbox;

    private RecommendationService recommendationService;
    private int currentSessionID;
    private ArrayList<Number> ratingsArrayList = new ArrayList<>();

    public void initSessionID(final SceneManager sceneManager, int sessionID) {
        currentSessionID = sessionID;
    }

    @FXML
    public void refresh(boolean searchOrRecommend) {
        int amountOfMovies = 5;

        //TODO wpisać oceny poprzedniego refresha do bazy danych

        ratingsArrayList.clear();

        ListVbox.getChildren().clear();
        Label description = new Label();

        if (searchOrRecommend) {
            description.setText("Your personal recommendations");
            Task<List<RecommendationService.MovieRecommendation>> recommendedMovies = recommendationService.generateRecommendationsAsync(currentSessionID, amountOfMovies);
        }
        else{
            description.setText("Search results");
            //TODO uzyc fukcji szukania
        }

        ListVbox.getChildren().add(description);

        //TODO ustawić wartości początkowe ratingów
        // w przypadku wyszukiwania bo chyba system rekomendacji nie wypluje ocenionego juz filmu

        for (int i = 0; i < amountOfMovies; i++){
            HBox hBox = new HBox();
            hBox.setSpacing(10);

            Label title = new Label();
            Label genre = new Label();
            Rating rating = new org.controlsfx.control.Rating();

            rating.ratingProperty().addListener((observable, oldValue, newValue) -> {
                ratingsArrayList.add(newValue);
            });

            if (searchOrRecommend){
                title.setText("FILM OF THE CENTURY");
                genre.setText("Adventure");
                hBox.getChildren().addAll(title, genre, rating);
                //TODO powiazac z rezultatem rekomendacji
            }
            else {
                title.setText("PIECE OF GARBAGE");
                genre.setText("Romance");
                hBox.getChildren().addAll(title, genre, rating);
                //TODO powiazac z rezultatem wyszukiwania
            }
            ListVbox.getChildren().add(hBox);
        }
    }
    @FXML
    public void onSearchButton(ActionEvent actionEvent) {
        refresh(false);
    }
    @FXML
    public void onRefreshButton(ActionEvent actionEvent) {
        refresh(true);
    }
}