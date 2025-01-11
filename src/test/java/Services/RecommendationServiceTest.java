package Services;

import com.laby.services.RecommendationService;
import com.laby.dao.RatingDAO;
import com.laby.models.Rating;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import javafx.concurrent.Task;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RecommendationServiceTest {
    @Mock
    private RecommendationService recommendationService;
    private RatingDAO ratingDAO;

    @BeforeEach
    void setUp() {
        ratingDAO = mock(RatingDAO.class);
        recommendationService = new RecommendationService(ratingDAO);
    }


    @Test
    @DisplayName("Generowanie rekomendacji powinno działać dla poprawnych danych")
    void generateRecommendationsShouldWorkWithValidData() throws SQLException {
        // given
        List<Rating> ratings = Arrays.asList(
                new Rating(1, 1, 5.0),
                new Rating(1, 2, 4.0),
                new Rating(2, 1, 3.0),
                new Rating(2, 2, 4.0)
        );
        when(ratingDAO.getAllRatings()).thenReturn(ratings);

        // when
        Task<List<RecommendationService.MovieRecommendation>> task =
                recommendationService.generateRecommendationsAsync(1, 2);

        // then
        assertNotNull(task);
        assertDoesNotThrow(() -> task.run());
    }

}