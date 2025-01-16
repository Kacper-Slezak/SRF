package DAOs;

import com.srf.dao.RatingDAO;
import com.srf.models.Rating;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RatingDAOTest {

    private Connection connection;
    private RatingDAO ratingDAO;

    @BeforeEach
    void setUp() throws SQLException {
        connection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        Statement stmt = connection.createStatement();
        stmt.execute("CREATE TABLE IF NOT EXISTS ratings (user_id INT, movie_id INT, rating DOUBLE)");
        ratingDAO = new RatingDAO(connection);
    }

    @Test
    void testGetAllRatings() throws SQLException {
        Statement stmt = connection.createStatement();
        stmt.execute("INSERT INTO ratings (user_id, movie_id, rating) VALUES (1, 101, 4.5)");

        List<Rating> ratings = ratingDAO.getAllRatings();

        assertEquals(1, ratings.size());
        Rating rating = ratings.get(0);
        assertEquals(1, rating.getUserId());
        assertEquals(101, rating.getMovieId());
        assertEquals(4.5, rating.getRating());
    }

    @Test
    void testAddRating() throws SQLException {
        Rating rating = new Rating(2, 202, 3.7);
        ratingDAO.addRating(rating);

        Statement stmt = connection.createStatement();
        var resultSet = stmt.executeQuery("SELECT * FROM ratings WHERE user_id = 2 AND movie_id = 202");

        assertTrue(resultSet.next());
        assertEquals(2, resultSet.getInt("user_id"));
        assertEquals(202, resultSet.getInt("movie_id"));
        assertEquals(3.7, resultSet.getDouble("rating"));
    }
}
