package com.srf.services;

import org.ejml.simple.SimpleMatrix;
import org.ejml.simple.SimpleSVD;

public class SVDRecommender {
    public static double[][] computeSVD(double[][] ratings, int k) {
        if (ratings == null || ratings.length == 0) {
            throw new IllegalArgumentException("Rating matrix cannot be empty");
        }

        try {
            System.out.println("Starting SVD computation...");
            int numUsers = ratings.length;
            int numMovies = ratings[0].length;
            System.out.println("Matrix dimensions: " + numUsers + " x " + numMovies);

            // Calculate row means
            System.out.println("Calculating row means...");
            double[] rowMeans = new double[numUsers];
            for (int i = 0; i < numUsers; i++) {
                double sum = 0;
                int count = 0;
                for (double rating : ratings[i]) {
                    if (rating > 0) {
                        sum += rating;
                        count++;
                    }
                }
                rowMeans[i] = count > 0 ? sum / count : 0;
            }

            // Normalize ratings
            System.out.println("Normalizing ratings...");
            double[][] normalizedRatings = new double[numUsers][numMovies];
            for (int i = 0; i < numUsers; i++) {
                for (int j = 0; j < numMovies; j++) {
                    if (ratings[i][j] > 0) {
                        normalizedRatings[i][j] = ratings[i][j] - rowMeans[i];
                    }
                }
            }

            // Convert to SimpleMatrix
            System.out.println("Converting to SimpleMatrix...");
            SimpleMatrix ratingMatrix = new SimpleMatrix(normalizedRatings);
            normalizedRatings = null;
            System.gc();

            // Perform SVD
            System.out.println("Performing SVD decomposition...");
            SimpleSVD<SimpleMatrix> svd = ratingMatrix.svd();
            System.out.println("SVD completed successfully");
            ratingMatrix = null;
            System.gc();

            // Reduce dimensions
            k = Math.min(k, Math.min(numUsers, numMovies));
            System.out.println("Reducing to " + k + " dimensions");
            SimpleMatrix U = svd.getU().extractMatrix(0, numUsers, 0, k);
            SimpleMatrix S = svd.getW().extractMatrix(0, k, 0, k);
            SimpleMatrix V = svd.getV().extractMatrix(0, numMovies, 0, k);

            // Reconstruct matrix
            System.out.println("Reconstructing matrix...");
            SimpleMatrix reconstructed = U.mult(S).mult(V.transpose());

            // Convert back to array
            System.out.println("Converting results to array...");
            double[][] result = new double[numUsers][numMovies];
            for (int i = 0; i < numUsers; i++) {
                for (int j = 0; j < numMovies; j++) {
                    result[i][j] = reconstructed.get(i, j) + rowMeans[i];
                    result[i][j] = Math.max(1, Math.min(5, result[i][j]));
                }
            }

            System.out.println("SVD computation completed successfully");
            return result;

        } catch (Exception e) {
            System.err.println("Error in SVD computation: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("SVD computation failed: " + e.getMessage(), e);
        }
    }
}