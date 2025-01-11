// SVDRecommenderTest.java
package Services;

import com.laby.services.SVDRecommender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

class SVDRecommenderTest {

    @Test
    @DisplayName("SVD powinno obsłużyć macierz wypełnioną zerami")
    void svdShouldHandleZeroMatrix() {
        double[][] ratings = {
                {0.0, 0.0, 0.0},
                {0.0, 0.0, 0.0}
        };

        double[][] predicted = SVDRecommender.computeSVD(ratings, 2);

        for (double[] row : predicted) {
            for (double value : row) {
                assertEquals(0.0, value);
            }
        }
    }

    @Test
    @DisplayName("SVD powinno rzucić wyjątek dla niepoprawnej liczby komponentów")
    void svdShouldThrowForInvalidComponents() {
        double[][] ratings = {
                {5.0, 3.0},
                {4.0, 0.0}
        };

        assertThrows(IllegalArgumentException.class, () -> SVDRecommender.computeSVD(ratings, -1));
    }
}
