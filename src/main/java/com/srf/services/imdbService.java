package com.srf.services;
import com.srf.dao.imdbDAO;

import java.sql.SQLException;


public class imdbService {
    private static imdbDAO imdbDAO = null;

    public imdbService(imdbDAO imdbDAO) {
        imdbService.imdbDAO = imdbDAO;
    }

    public static String fetchImdbUrl(int movieId) {
        String imdbBaseUrl = "https://www.imdb.com/title/tt";
        try {
            String imdbId = imdbDAO.getImdbIdByMovieId(movieId);
            if (imdbId != null) {
                // Upewnij się, że IMDb ID ma dokładnie 7 znaków, dodając wiodące zera w razie potrzeby
                imdbId = String.format("%07d", Integer.parseInt(imdbId));
                return imdbBaseUrl + imdbId;
            }
        } catch (SQLException e) {
            System.err.println("Failed to fetch IMDb ID: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Invalid IMDb ID format: " + e.getMessage());
        }
        return null;
    }

}

