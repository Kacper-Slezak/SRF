package com.laby.services;

import com.laby.dao.RatingDAO;
import com.laby.models.Rating;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RecommendationService {
    private final RatingDAO ratingDAO;

    public RecommendationService(RatingDAO ratingDAO) {
        this.ratingDAO = ratingDAO;
    }

    public List<String> generateRecommendationsForUser(int userId, int k) throws SQLException {
        // Pobranie wszystkich ocen z bazy danych
        List<Rating> ratings = ratingDAO.getAllRatings();

        // Znalezienie maksymalnych ID użytkowników i filmów
        int maxUserId = ratings.stream().mapToInt(Rating::getUserId).max().orElse(0);
        int maxMovieId = ratings.stream().mapToInt(Rating::getMovieId).max().orElse(0);

        // Tworzenie macierzy użytkownicy x filmy
        double[][] ratingMatrix = new double[maxUserId + 1][maxMovieId + 1];
        for (Rating rating : ratings) {
            ratingMatrix[rating.getUserId()][rating.getMovieId()] = rating.getRating();
        }

        // Obliczanie SVD
        double[][] predictedRatings = SVDRecommender.computeSVD(ratingMatrix, k);

        // Generowanie listy rekomendacji dla użytkownika
        List<String> recommendations = new ArrayList<>();
        for (int movieId = 0; movieId < predictedRatings[userId].length; movieId++) {
            if (ratingMatrix[userId][movieId] == 0) { // Filmy bez oceny
                recommendations.add("Film ID: " + movieId + " | Przewidywana ocena: " + predictedRatings[userId][movieId]);
            }
        }

        return recommendations;
    }
}
