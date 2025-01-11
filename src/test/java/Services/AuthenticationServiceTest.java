// AuthenticationServiceTest.java
package Services;

import com.srf.services.AuthenticationService;
import com.srf.dao.UserDAO;
import com.srf.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthenticationServiceTest {
    @Mock
    private UserDAO userDAO;
    private AuthenticationService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authService = new AuthenticationService(userDAO);
    }

    @Test
    @DisplayName("Rejestracja powinna się nie powieść dla pustego hasła")
    void registerShouldFailWithEmptyPassword() {
        String username = "testUser";
        String password = "";

        assertThrows(IllegalArgumentException.class, () -> authService.register(username, password));
    }
}
