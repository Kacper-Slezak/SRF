package com.srf.services;
import com.srf.dao.imdbDAO;

import java.sql.SQLException;


public class imdbService {
    private static imdbDAO imdbDAO = null;

    public imdbService(imdbDAO imdbDAO) {
        imdbService.imdbDAO = imdbDAO;
    }

    public static String fetchImdbUrl(int movieId) {
        String imdbBaseUrl = "https://www.imdb.com/title/tt0";
        try {
            String imdbId = imdbDAO.getImdbIdByMovieId(movieId);
            if (imdbId != null && imdbId.length() == 7) {
                return imdbBaseUrl + imdbId;
            } else if (imdbId != null && imdbId.length() == 6) {
                return imdbBaseUrl + '0' + imdbId;
            }
        } catch (SQLException e) {
            System.err.println("Failed to fetch IMDb ID: " + e.getMessage());
        }
        return null;
    }
}

