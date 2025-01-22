package com.srf.services;

import com.srf.dao.MovieDAO;
import com.srf.models.Movie;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SearchService {
    private final MovieDAO movieDAO;
    private static final int maxSearchResult = 250;

    public SearchService(MovieDAO movieDAO) {
        this.movieDAO = movieDAO;
    }

    /**
     * Wyszukiwanie filmów według tytułu lub gatunku.
     *
     * @param query Fraza wyszukiwania (może zawierać wiele słów).
     * @return Lista pasujących filmów.
     */
    public List<Movie> searchMovies(String query) {
        List<Movie> matchingMovies = null;
        try {
            // Rozdziel zapytanie na słowa kluczowe i sformatuj jako warunki SQL
            String[] keywords = query.toLowerCase().split(" ");
            StringBuilder sqlQueryBuilder = new StringBuilder("SELECT * FROM movies WHERE ");

            for (int i = 0; i < keywords.length; i++) {
                if (i > 0) {
                    sqlQueryBuilder.append(" OR ");
                }
                sqlQueryBuilder.append("(LOWER(title) LIKE ? OR LOWER(genre) LIKE ?)");
            }
            sqlQueryBuilder.append(" LIMIT " + maxSearchResult);

            String sqlQuery = sqlQueryBuilder.toString();

            // Wywołaj DAO, przekazując zapytanie SQL oraz parametry
            matchingMovies = movieDAO.searchMoviesByQuery(sqlQuery, keywords);
        } catch (SQLException e) {
            System.err.println("Błąd podczas wyszukiwania filmów: " + e.getMessage());
        }
        return matchingMovies;
    }
}
