package com.srf.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.controlsfx.control.Rating;

import java.lang.reflect.Array;
import java.util.ArrayList;

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

    @FXML
    public void refresh(boolean searchOrRecommend) {
        ListVbox.getChildren().clear();
        Label description = new Label();
        if (searchOrRecommend) {
            description.setText("Your personal recommendations");
        }
        else{
            description.setText("Search results");
        }
        ListVbox.getChildren().add(description);
        int amountOfMovies = 5;
        ArrayList<Rating> listRatings = new ArrayList<>();

        for (int i = 0; i < amountOfMovies; i++){
            HBox hBox = new HBox();
            hBox.setSpacing(10);

            Label title = new Label();
            Label genre = new Label();
            Rating rating = new org.controlsfx.control.Rating();
            listRatings.add(rating);
            //title.setFont();


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
    public void onRating(ActionEvent actionEvent) {
        //TODO powiazac z ustawieniem oceny
        //Listener na rating
    }
}