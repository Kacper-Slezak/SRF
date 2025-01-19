package com.srf.services;

import com.srf.dao.RatingDAO;
import com.srf.dao.MovieDAO;
import com.srf.models.Rating;
import com.srf.models.Movie;
import javafx.concurrent.Task;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class RecommendationService {
    private final RatingDAO ratingDAO;
    private final MovieDAO movieDAO;
    private final Map<Integer, List<MovieRecommendation>> recommendationsCache;
    private long lastUpdateTime;
    private static final long CACHE_VALIDITY_PERIOD = 15 * 60 * 1000;
    private static final int TOP_RECOMMENDATIONS = 100;
    private static final int MIN_RATINGS_FOR_MOVIE = 3;
    private static final double MIN_RATING_THRESHOLD = 3.0; // Obniżony próg dla testów

    public RecommendationService(RatingDAO ratingDAO, MovieDAO movieDAO) {
        this.ratingDAO = ratingDAO;
        this.movieDAO = movieDAO;
        this.recommendationsCache = new ConcurrentHashMap<>();
    }

    public Task<List<Movie>> generateRecommendationsAsync(int userId, int k) {
        return new Task<>() {
            @Override
            protected List<Movie> call() throws Exception {
                try {
                    System.out.println("=== Starting recommendation generation for user " + userId + " ===");
                    updateProgress(0,   100);

                    // Sprawdź cache
                    if (isCacheValid(userId)) {
                        System.out.println("Using cached recommendations for user " + userId);
                        updateProgress(100, 100);

                        // Konwersja z cache do listy Movie
                        List<Movie> cachedMovies = convertRecommendationsToMovies(recommendationsCache.get(userId));
                        System.out.println("Returning " + cachedMovies.size() + " cached movies");
                        return cachedMovies;
                    }

                    updateProgress(20, 100);
                    System.out.println("Loading all ratings...");
                    List<Rating> allRatings = ratingDAO.getAllRatings();
                    System.out.println("Total ratings loaded: " + allRatings.size());

                    // Filtrowanie ocen
                    Map<Integer, Long> movieRatingCounts = allRatings.stream()
                            .collect(Collectors.groupingBy(Rating::getMovieId, Collectors.counting()));

                    Set<Integer> validMovieIds = movieRatingCounts.entrySet().stream()
                            .filter(e -> e.getValue() >= MIN_RATINGS_FOR_MOVIE)
                            .map(Map.Entry::getKey)
                            .collect(Collectors.toSet());

                    System.out.println("Original movie count: " + movieRatingCounts.size());
                    System.out.println("Filtered movie count: " + validMovieIds.size());

                    List<Rating> filteredRatings = allRatings.stream()
                            .filter(r -> validMovieIds.contains(r.getMovieId()))
                            .collect(Collectors.toList());
                    System.out.println("Filtered ratings count: " + filteredRatings.size());

                    updateProgress(40, 100);

                    // Przygotowanie macierzy ocen
                    System.out.println("Preparing rating matrix...");
                    double[][] ratingMatrix = prepareRatingMatrix(filteredRatings, validMovieIds);
                    System.out.println("Rating matrix prepared with dimensions: " + ratingMatrix.length + " x " +
                            (ratingMatrix.length > 0 ? ratingMatrix[0].length : 0));

                    updateProgress(60, 100);

                    // Obliczanie SVD
                    System.out.println("Computing SVD...");
                    int effectiveK = Math.min(k, 20);
                    double[][] predictedRatings = SVDRecommender.computeSVD(ratingMatrix, effectiveK);
                    System.out.println("SVD computation completed");

                    updateProgress(80, 100);

                    // Generowanie rekomendacji
                    System.out.println("Generating recommendations...");
                    List<MovieRecommendation> recommendations = generateRecommendations(
                            userId,
                            predictedRatings,
                            ratingMatrix,
                            new ArrayList<>(validMovieIds)
                    );

                    System.out.println("Generated " + recommendations.size() + " recommendations");
                    if (!recommendations.isEmpty()) {
                        System.out.println("Top recommendation: MovieID=" + recommendations.get(0).getMovieId() +
                                ", Rating=" + recommendations.get(0).getPredictedRating());
                    }

                    // Konwersja do listy obiektów Movie
                    System.out.println("Converting recommendations to movies...");
                    List<Movie> movies = convertRecommendationsToMovies(recommendations);
                    System.out.println("Converted " + movies.size() + " movies");

                    recommendationsCache.put(userId, recommendations);
                    lastUpdateTime = System.currentTimeMillis();

                    updateProgress(100, 100);
                    System.out.println("=== Recommendation generation completed for user " + userId + " ===");
                    return movies;

                } catch (Exception e) {
                    System.err.println("Error generating recommendations: " + e.getMessage());
                    e.printStackTrace();
                    updateProgress(0, 100);
                    throw e;
                }
            }
        };
    }


    private double[][] prepareRatingMatrix(List<Rating> ratings, Set<Integer> validMovieIds) {
        Map<Integer, Integer> movieIdToIndex = new HashMap<>();
        int index = 0;
        for (Integer movieId : validMovieIds) {
            movieIdToIndex.put(movieId, index++);
        }

        int maxUserId = ratings.stream().mapToInt(Rating::getUserId).max().orElse(0);
        int totalMovies = validMovieIds.size();

        System.out.println("Creating rating matrix: " + (maxUserId + 1) + " x " + totalMovies);

        double[][] ratingMatrix = new double[maxUserId + 1][totalMovies];
        int nonZeroRatings = 0;

        for (Rating rating : ratings) {
            Integer movieIndex = movieIdToIndex.get(rating.getMovieId());
            if (movieIndex != null) {
                ratingMatrix[rating.getUserId()][movieIndex] = rating.getRating();
                nonZeroRatings++;
            }
        }

        System.out.println("Matrix sparsity: " +
                String.format("%.2f%%", (1 - (double)nonZeroRatings / ((maxUserId + 1) * totalMovies)) * 100));

        return ratingMatrix;
    }

    private List<MovieRecommendation> generateRecommendations(
            int userId,
            double[][] predictedRatings,
            double[][] originalRatings,
            List<Integer> validMovieIds) {

        List<MovieRecommendation> recommendations = new ArrayList<>();
        int potentialRecommendations = 0;

        for (int movieIndex = 0; movieIndex < predictedRatings[userId].length; movieIndex++) {
            if (originalRatings[userId][movieIndex] == 0) {
                potentialRecommendations++;
                double predictedRating = predictedRatings[userId][movieIndex];
                if (predictedRating >= MIN_RATING_THRESHOLD) {
                    int realMovieId = validMovieIds.get(movieIndex);
                    recommendations.add(new MovieRecommendation(realMovieId, predictedRating));
                }
            }
        }

        System.out.println("Potential recommendations checked: " + potentialRecommendations);
        System.out.println("Recommendations above threshold: " + recommendations.size());

        recommendations.sort((a, b) -> Double.compare(b.getPredictedRating(), a.getPredictedRating()));

        List<MovieRecommendation> finalRecommendations = recommendations.size() > TOP_RECOMMENDATIONS ?
                recommendations.subList(0, TOP_RECOMMENDATIONS) :
                recommendations;

        System.out.println("Final recommendations count: " + finalRecommendations.size());
        return finalRecommendations;
    }

    private boolean isCacheValid(int userId) {
        if (!recommendationsCache.containsKey(userId)) {
            System.out.println("No cache entry for user " + userId);
            return false;
        }
        boolean isValid = System.currentTimeMillis() - lastUpdateTime < CACHE_VALIDITY_PERIOD;
        System.out.println("Cache " + (isValid ? "is" : "is not") + " valid for user " + userId);
        return isValid;
    }

    public static class MovieRecommendation {
        private final int movieId;
        private final double predictedRating;

        public MovieRecommendation(int movieId, double predictedRating) {
            this.movieId = movieId;
            this.predictedRating = predictedRating;
        }

        public int getMovieId() { return movieId; }
        public double getPredictedRating() { return predictedRating; }

        @Override
        public String toString() {
            return String.format("MovieRecommendation{movieId=%d, rating=%.2f}", movieId, predictedRating);
        }
    }
    private List<Movie> convertRecommendationsToMovies(List<MovieRecommendation> recommendations) throws SQLException {
        List<Movie> movies = new ArrayList<>();

        for (MovieRecommendation recommendation : recommendations) {
            // Pobierz film na podstawie ID
            List<Movie> matchingMovies = movieDAO.getAllMovies()
                    .stream()
                    .filter(movie -> movie.getId() == recommendation.getMovieId())
                    .collect(Collectors.toList());

            if (!matchingMovies.isEmpty()) {
                movies.add(matchingMovies.get(0));
            }
        }
        return movies;
    }
    public void invalidateCache(int userId) {
        recommendationsCache.remove(userId); // Usuń cache dla danego użytkownika
        System.out.println("Cache invalidated for user " + userId);
    }


}
