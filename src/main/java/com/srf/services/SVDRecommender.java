package com.srf.services;

import org.ejml.simple.SimpleMatrix;
import org.ejml.simple.SimpleSVD;

/**
 * Klasa implementująca system rekomendacji wykorzystujący algorytm SVD (Singular Value Decomposition).
 * SVD pozwala na redukcję wymiarowości macierzy ocen użytkowników i odkrycie ukrytych wzorców w danych.
 */
public class SVDRecommender {

    // Stałe określające zakres możliwych ocen
    private static final double MIN_RATING = 1.0;
    private static final double MAX_RATING = 5.0;

    /**
     * Główna metoda wykonująca dekompozycję SVD i obliczająca rekomendacje.
     * @param ratings Macierz ocen [użytkownicy x filmy]
     * @param k Liczba wymiarów do których redukujemy macierz
     * @return Macierz przewidywanych ocen
     */
    public static double[][] computeSVD(double[][] ratings, int k) {
        validateInput(ratings);

        final int numUsers = ratings.length;
        final int numMovies = ratings[0].length;

        System.out.println(String.format("Rozpoczynam obliczenia SVD dla macierzy %dx%d", numUsers, numMovies));

        try {
            // Obliczenie średnich ocen dla każdego użytkownika
            double[] rowMeans = calculateRowMeans(ratings, numUsers, numMovies);

            // Normalizacja macierzy przez odjęcie średnich
            SimpleMatrix normalizedMatrix = createNormalizedMatrix(ratings, rowMeans, numUsers, numMovies);

            // Wykonanie dekompozycji SVD
            System.out.println("Rozpoczynam dekompozycję SVD...");
            SimpleSVD<SimpleMatrix> svd = normalizedMatrix.svd();
            normalizedMatrix = null;

            // Redukcja wymiarów - wybieramy minimum z k i wymiarów macierzy
            k = Math.min(k, Math.min(numUsers, numMovies));
            System.out.println("Redukuję wymiary do k=" + k);

            // Rekonstrukcja macierzy z zredukowaną liczbą wymiarów
            SimpleMatrix reconstructed = reconstructMatrix(svd, numUsers, numMovies, k);
            svd = null;

            // Denormalizacja i ograniczenie wartości do zakresu ocen
            return denormalizeAndClampResults(reconstructed, rowMeans, numUsers, numMovies);

        } catch (Exception e) {
            System.out.println("Błąd podczas obliczeń SVD: " + e.getMessage());
            throw new SVDComputationException("Obliczenia SVD nie powiodły się", e);
        }
    }

    /**
     * Sprawdza poprawność danych wejściowych.
     */
    private static void validateInput(double[][] ratings) {
        if (ratings == null || ratings.length == 0 || ratings[0].length == 0) {
            System.out.println("Próba przetworzenia pustej macierzy ocen");
            throw new IllegalArgumentException("Macierz ocen nie może być pusta");
        }
    }

    /**
     * Oblicza średnie oceny dla każdego użytkownika, ignorując brakujące oceny (0).
     */
    private static double[] calculateRowMeans(double[][] ratings, int numUsers, int numMovies) {
        System.out.println("Obliczam średnie ocen dla użytkowników...");
        double[] rowMeans = new double[numUsers];

        for (int i = 0; i < numUsers; i++) {
            double sum = 0;
            int count = 0;
            for (int j = 0; j < numMovies; j++) {
                if (ratings[i][j] > 0) {  // Pomijamy brakujące oceny
                    sum += ratings[i][j];
                    count++;
                }
            }
            rowMeans[i] = count > 0 ? sum / count : 0;
        }
        return rowMeans;
    }

    /**
     * Tworzy znormalizowaną macierz przez odjęcie średnich ocen użytkowników.
     */
    private static SimpleMatrix createNormalizedMatrix(double[][] ratings, double[] rowMeans,
                                                       int numUsers, int numMovies) {
        System.out.println("Tworzę znormalizowaną macierz...");
        double[][] normalizedData = new double[numUsers][numMovies];

        for (int i = 0; i < numUsers; i++) {
            for (int j = 0; j < numMovies; j++) {
                if (ratings[i][j] > 0) {
                    normalizedData[i][j] = ratings[i][j] - rowMeans[i];
                }
            }
        }

        return new SimpleMatrix(normalizedData);
    }

    /**
     * Rekonstruuje macierz z wykorzystaniem zredukowanych wymiarów.
     */
    private static SimpleMatrix reconstructMatrix(SimpleSVD<SimpleMatrix> svd,
                                                  int numUsers, int numMovies, int k) {
        System.out.println("Rekonstruuję macierz ze zredukowaną liczbą wymiarów...");
        SimpleMatrix U = svd.getU().extractMatrix(0, numUsers, 0, k);
        SimpleMatrix S = svd.getW().extractMatrix(0, k, 0, k);
        SimpleMatrix V = svd.getV().extractMatrix(0, numMovies, 0, k);

        return U.mult(S).mult(V.transpose());
    }

    /**
     * Przywraca pierwotną skalę ocen i ogranicza wartości do dozwolonego zakresu.
     */
    private static double[][] denormalizeAndClampResults(SimpleMatrix reconstructed,
                                                         double[] rowMeans,
                                                         int numUsers, int numMovies) {
        System.out.println("Denormalizuję i ograniczam wartości końcowych ocen...");
        double[][] result = new double[numUsers][numMovies];

        for (int i = 0; i < numUsers; i++) {
            for (int j = 0; j < numMovies; j++) {
                result[i][j] = Math.min(MAX_RATING,
                        Math.max(MIN_RATING,
                                reconstructed.get(i, j) + rowMeans[i]));
            }
        }

        return result;
    }

    /**
     * Własna klasa wyjątków dla błędów podczas obliczeń SVD.
     */
    public static class SVDComputationException extends RuntimeException {
        public SVDComputationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
