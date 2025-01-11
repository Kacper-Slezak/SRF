package Services;

import com.laby.models.Rating;
import com.laby.services.RecommendationService;
import com.laby.dao.RatingDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class RecommendationServiceTest {
    private RecommendationService recommendationService;
    private RatingDAO mockRatingDAO;

    @BeforeEach
    public void setUp() {
        mockRatingDAO = Mockito.mock(RatingDAO.class);
        recommendationService = new RecommendationService(mockRatingDAO);
    }

    @Test
    public void testGenerateRecommendationsForUser() throws SQLException {
        // Mockowanie danych zwracanych przez RatingDAO
        List<Rating> mockRatings = Arrays.asList(
                new Rating(1, 1, 4.0), // Użytkownik 1 ocenił film 1 na 4.0
                new Rating(1, 2, 5.0), // Użytkownik 1 ocenił film 2 na 5.0
                new Rating(2, 1, 3.0), // Użytkownik 2 ocenił film 1 na 3.0
                new Rating(2, 3, 2.0)  // Użytkownik 2 ocenił film 3 na 2.0
        );
        when(mockRatingDAO.getAllRatings()).thenReturn(mockRatings);

        // Wywołanie rekomendacji dla użytkownika 1
        List<String> recommendations = recommendationService.generateRecommendationsForUser(1, 2);

        // Weryfikacja wyników
        // Sprawdź, czy lista rekomendacji zawiera filmy nieocenione przez użytkownika 1
        assertEquals(1, recommendations.size());
        assertEquals("Film ID: 3 | Przewidywana ocena: 0.0", recommendations.get(0));

        // Weryfikacja, że metoda DAO została wywołana raz
        verify(mockRatingDAO, times(1)).getAllRatings();
    }
}
