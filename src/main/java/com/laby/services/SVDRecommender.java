package com.laby.services;

import org.ejml.simple.SimpleMatrix;

public class SVDRecommender {

    /**
     * Przeprowadza dekompozycję SVD i przewiduje brakujące oceny.
     *
     * @param ratings Macierz ocen (użytkownicy x filmy). Zera oznaczają brak oceny.
     * @param k       Liczba wymiarów do zachowania w macierzy S.
     * @return Zrekonstruowana macierz ocen.
     */
    public static double[][] computeSVD(double[][] ratings, int k) {
        // Utworzenie macierzy
        SimpleMatrix ratingMatrix = new SimpleMatrix(ratings);

        // Przeprowadzenie SVD
        SimpleMatrix U = ratingMatrix.svd().getU();
        SimpleMatrix S = ratingMatrix.svd().getW();
        SimpleMatrix V = ratingMatrix.svd().getV();

        // Przycięcie macierzy S do k wymiarów
        SimpleMatrix S_reduced = new SimpleMatrix(k, k);
        for (int i = 0; i < k; i++) {
            S_reduced.set(i, i, S.get(i, i));
        }

        // Przycięcie U i V
        SimpleMatrix U_reduced = U.cols(0, k);
        SimpleMatrix V_reduced = V.cols(0, k);

        // Rekonstrukcja macierzy
        SimpleMatrix reconstructed = U_reduced.mult(S_reduced).mult(V_reduced.transpose());

        // Konwersja wyniku na tablicę dwuwymiarową
        double[][] result = new double[reconstructed.numRows()][reconstructed.numCols()];
        for (int i = 0; i < reconstructed.numRows(); i++) {
            for (int j = 0; j < reconstructed.numCols(); j++) {
                result[i][j] = reconstructed.get(i, j);
            }
        }
        return result;
    }

    public static void main(String[] args) {
        // Przykładowe dane
        double[][] ratings = {
                {5, 4, 0, 0},
                {4, 0, 0, 3},
                {0, 0, 5, 4},
                {0, 3, 4, 5}
        };

        // Obliczanie SVD i przewidywanie brakujących ocen
        double[][] predictedRatings = computeSVD(ratings, 2);

        // Wyświetlanie wyników
        System.out.println("Zrekonstruowana macierz ocen:");
        for (double[] row : predictedRatings) {
            for (double value : row) {
                System.out.printf("%.2f ", value);
            }
            System.out.println();
        }
    }
}
