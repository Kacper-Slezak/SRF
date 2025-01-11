package com.srf.services;

import com.srf.dao.RatingDAO;
import com.srf.models.Rating;
import javafx.concurrent.Task;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Serwis rekomendacji oparty na metodzie SVD (Singular Value Decomposition).
 * Obsługuje generowanie rekomendacji asynchronicznie oraz korzysta z pamięci podręcznej w celu optymalizacji wydajności.
 */
public class RecommendationService {
    private final RatingDAO ratingDAO;
    private final Map<Integer, List<MovieRecommendation>> recommendationsCache;
    private double[][] lastRatingMatrix;
    private long lastUpdateTime;
    private static final long CACHE_VALIDITY_PERIOD = 15 * 60 * 1000; // 15 minut

    public RecommendationService(RatingDAO ratingDAO) {
        this.ratingDAO = ratingDAO;
        this.recommendationsCache = new ConcurrentHashMap<>();
    }

    /**
     * Klasa pomocnicza reprezentująca rekomendację filmu.
     */
    public static class MovieRecommendation {
        private final int movieId;
        private final double predictedRating;

        public MovieRecommendation(int movieId, double predictedRating) {
            this.movieId = movieId;
            this.predictedRating = predictedRating;
        }

        public int getMovieId() { return movieId; }
        public double getPredictedRating() { return predictedRating; }
    }

    /**
     * Generuje rekomendacje dla użytkownika asynchronicznie.
     * @param userId ID użytkownika
     * @param k Liczba wymiarów w macierzy SVD
     * @return Task zwracający listę rekomendacji
     */
    public Task<List<MovieRecommendation>> generateRecommendationsAsync(int userId, int k) {
        return new Task<>() {
            @Override
            protected List<MovieRecommendation> call() throws Exception {
                try {
                    updateProgress(0, 100);

                    // Sprawdź cache
                    if (isCacheValid(userId)) {
                        updateProgress(100, 100);
                        return recommendationsCache.get(userId);
                    }

                    // Pobierz oceny
                    updateProgress(20, 100);
                    List<Rating> ratings;
                    ratings = ratingDAO.getAllRatings();

                    // Przygotuj macierz ocen
                    updateProgress(40, 100);
                    double[][] ratingMatrix = prepareRatingMatrix(ratings);

                    // Oblicz SVD
                    updateProgress(60, 100);
                    double[][] predictedRatings;
                    try {
                        predictedRatings = SVDRecommender.computeSVD(ratingMatrix, k);
                    } catch (Exception e) {
                        throw new RuntimeException("Błąd podczas obliczania SVD.", e);
                    }

                    // Generuj rekomendacje
                    updateProgress(80, 100);
                    List<MovieRecommendation> recommendations = generateRecommendations(userId, predictedRatings, ratingMatrix);

                    // Zapisz w cache
                    recommendationsCache.put(userId, recommendations);
                    lastUpdateTime = System.currentTimeMillis();

                    updateProgress(100, 100);
                    return recommendations;

                } catch (Exception e) {
                    // Obsłuż błąd globalny
                    updateProgress(0, 100);
                    throw new RuntimeException("Wystąpił błąd podczas generowania rekomendacji.", e);
                }
            }
        };
    }

    /**
     * Sprawdza ważność pamięci podręcznej dla podanego użytkownika.
     * @param userId ID użytkownika
     * @return true, jeśli cache jest ważny
     */
    private boolean isCacheValid(int userId) {
        if (!recommendationsCache.containsKey(userId)) return false;
        return System.currentTimeMillis() - lastUpdateTime < CACHE_VALIDITY_PERIOD;
    }

    /**
     * Przygotowuje macierz ocen na podstawie listy ocen użytkowników.
     * @param ratings Lista ocen
     * @return Dwuwymiarowa macierz ocen
     */
    private double[][] prepareRatingMatrix(List<Rating> ratings) {
        try {
            int maxUserId = ratings.stream().mapToInt(Rating::getUserId).max().orElse(0);
            int maxMovieId = ratings.stream().mapToInt(Rating::getMovieId).max().orElse(0);

            double[][] ratingMatrix = new double[maxUserId + 1][maxMovieId + 1];
            for (Rating rating : ratings) {
                ratingMatrix[rating.getUserId()][rating.getMovieId()] = rating.getRating();
            }

            lastRatingMatrix = ratingMatrix;
            return ratingMatrix;
        } catch (Exception e) {
            throw new RuntimeException("Błąd podczas przygotowywania macierzy ocen.", e);
        }
    }

    /**
     * Generuje rekomendacje na podstawie przewidywanych ocen i macierzy oryginalnych ocen.
     * @param userId ID użytkownika
     * @param predictedRatings Przewidywane oceny (macierz SVD)
     * @param originalRatings Oryginalne oceny użytkowników
     * @return Lista rekomendacji filmowych
     */
    private List<MovieRecommendation> generateRecommendations(int userId, double[][] predictedRatings, double[][] originalRatings) {
        try {
            List<MovieRecommendation> recommendations = new ArrayList<>();

            for (int movieId = 0; movieId < predictedRatings[userId].length; movieId++) {
                if (originalRatings[userId][movieId] == 0) {
                    double predictedRating = predictedRatings[userId][movieId];
                    if (predictedRating >= 3.5) {
                        recommendations.add(new MovieRecommendation(movieId, predictedRating));
                    }
                }
            }

            recommendations.sort((a, b) -> Double.compare(b.getPredictedRating(), a.getPredictedRating()));
            return recommendations;
        } catch (Exception e) {
            throw new RuntimeException("Błąd podczas generowania rekomendacji.", e);
        }
    }
}
