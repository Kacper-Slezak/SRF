package com.srf.services;

import com.srf.dao.MovieDAO;
import com.srf.models.Movie;

import java.util.List;
import java.util.stream.Collectors;

public class SearchService {
    private final MovieDAO movieDAO;

    public SearchService(MovieDAO movieDAO) {
        this.movieDAO = movieDAO;
    }

    /**
     * Wyszukiwanie filmów według tytułu.
     *
     * @param query Fraza wyszukiwania.
     * @return Lista pasujących filmów.
     */
    public List<Movie> searchMoviesByTitle(String query) {
        try {
            // Pobranie wszystkich filmów z DAO
            List<Movie> allMovies = movieDAO.getAllMovies();

            // Filtracja filmów na podstawie tytułu (ignorowanie wielkości liter)
            return allMovies.stream()
                    .filter(movie -> movie.getTitle().toLowerCase().contains(query.toLowerCase()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            e.printStackTrace();
            return List.of(); // Zwróć pustą listę w przypadku błędu
        }
    }
}
