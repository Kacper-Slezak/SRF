package com.srf.services;

import com.srf.dao.MovieDAO;
import com.srf.models.Movie;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SearchService {
    private final MovieDAO movieDAO;

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
        List<Movie> matchingMovies = new ArrayList<>();
        try {
            // Pobierz wszystkie filmy z DAO
            List<Movie> allMovies = movieDAO.getAllMovies();

            // Rozdziel zapytanie na słowa kluczowe
            String[] keywords = query.toLowerCase().split(" ");

            // Filtruj filmy według tytułu lub gatunku
            for (Movie movie : allMovies) {
                String title = movie.getTitle().toLowerCase();
                String genre = movie.getGenre().toLowerCase();

                for (String keyword : keywords) {
                    if (title.contains(keyword) || genre.contains(keyword)) {
                        matchingMovies.add(movie);
                        break; // Unikaj dodawania tego samego filmu wielokrotnie
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Błąd podczas wyszukiwania filmów: " + e.getMessage());
        }
        return matchingMovies;
    }
}
