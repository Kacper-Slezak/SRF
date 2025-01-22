package com.srf.services;

import com.srf.dao.RatingDAO;
import com.srf.models.Rating;
import com.srf.utils.AlertManager;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RatingService {
    private final RatingDAO ratingDAO;
    private final ExecutorService executorService;
    private final AlertManager alertManager;

    public RatingService(RatingDAO ratingDAO) {
        this.ratingDAO = ratingDAO;
        this.executorService = Executors.newSingleThreadExecutor();
        this.alertManager = AlertManager.getInstance();
    }

    public void saveRating(int userId, int movieId, double ratingValue, Runnable onSuccess) {
        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws SQLException {
                try {
                    Rating existingRating = ratingDAO.findRating(userId, movieId);

                    if (existingRating != null) {
                        existingRating.setRating(ratingValue);
                        ratingDAO.updateRating(existingRating);
                    } else {
                        Rating newRating = new Rating(userId, movieId, ratingValue);
                        ratingDAO.addRating(newRating);
                    }
                    return null;
                } catch (Exception e) {
                    throw new SQLException("Error saving rating: " + e.getMessage());
                }
            }
        };

        task.setOnSucceeded(event -> {
            Platform.runLater(() -> {
                alertManager.showInfo("Success", "Rating saved successfully");
                if (onSuccess != null) {
                    onSuccess.run();
                }
            });
        });

        task.setOnFailed(event -> {
            Platform.runLater(() ->
                    alertManager.showError(
                            "Save Error",
                            "Failed to save rating: " + task.getException().getMessage()
                    )
            );
        });

        executorService.submit(task);
    }

}