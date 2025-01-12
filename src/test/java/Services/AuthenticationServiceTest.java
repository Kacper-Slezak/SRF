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

    // --- TESTY REJESTRACJI ---

    @Test
    @DisplayName("Rejestracja powinna się nie powieść dla pustego hasła")
    void registerShouldFailWithEmptyPassword() {
        String username = "testUser";
        String password = "";

        assertThrows(IllegalArgumentException.class, () ->
                authService.register(username, password, password));
    }

    @Test
    @DisplayName("Rejestracja powinna się nie powieść dla zbyt krótkiej nazwy użytkownika")
    void registerShouldFailWithShortUsername() {
        String username = "ab"; // Zbyt krótkie
        String password = "validPassword";

        assertThrows(IllegalArgumentException.class, () ->
                authService.register(username, password, password));
    }

    @Test
    @DisplayName("Rejestracja powinna się nie powieść, gdy hasła nie pasują")
    void registerShouldFailWhenPasswordsDoNotMatch() {
        String username = "testUser";
        String password = "password123";
        String confirmPassword = "differentPassword";

        assertThrows(IllegalArgumentException.class, () ->
                authService.register(username, password, confirmPassword));
    }

    @Test
    @DisplayName("Rejestracja powinna się nie powieść, gdy użytkownik istnieje")
    void registerShouldFailWhenUserAlreadyExists() throws SQLException {
        String username = "existingUser";
        String password = "password123";

        // Mockowanie istniejącego użytkownika
        when(userDAO.findByUsername(username)).thenReturn(Optional.of(new User()));

        assertThrows(IllegalArgumentException.class, () ->
                authService.register(username, password, password));
    }

    @Test
    @DisplayName("Rejestracja powinna się powieść dla poprawnych danych")
    void registerShouldSucceedWithValidData() throws SQLException {
        String username = "newUser";
        String password = "password123";

        // Mockowanie braku użytkownika
        when(userDAO.findByUsername(username)).thenReturn(Optional.empty());

        // Mockowanie zapisu
        doNothing().when(userDAO).save(any(User.class));

        User registeredUser = authService.register(username, password, password);

        assertNotNull(registeredUser);
        assertEquals(username, registeredUser.getUsername());
        assertTrue(BCrypt.checkpw(password, registeredUser.getPasswordHash()));
    }

    // --- TESTY LOGOWANIA ---

    @Test
    @DisplayName("Logowanie powinno się nie powieść dla pustych danych")
    void loginShouldFailWithEmptyCredentials() {
        assertThrows(IllegalArgumentException.class, () ->
                authService.login("", ""));
    }

    @Test
    @DisplayName("Logowanie powinno się nie powieść dla nieistniejącego użytkownika")
    void loginShouldFailForNonExistentUser() throws SQLException {
        String username = "nonExistentUser";
        String password = "password123";

        // Mockowanie braku użytkownika
        when(userDAO.findByUsername(username)).thenReturn(Optional.empty());

        Optional<User> result = authService.login(username, password);
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Logowanie powinno się nie powieść dla błędnego hasła")
    void loginShouldFailForIncorrectPassword() throws SQLException {
        String username = "testUser";
        String password = "wrongPassword";

        User mockUser = new User();
        mockUser.setUsername(username);
        mockUser.setPasswordHash(BCrypt.hashpw("correctPassword", BCrypt.gensalt()));

        // Mockowanie użytkownika
        when(userDAO.findByUsername(username)).thenReturn(Optional.of(mockUser));

        Optional<User> result = authService.login(username, password);
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Logowanie powinno się powieść dla poprawnych danych")
    void loginShouldSucceedWithValidCredentials() throws SQLException {
        String username = "testUser";
        String password = "correctPassword";

        User mockUser = new User();
        mockUser.setUsername(username);
        mockUser.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt()));

        // Mockowanie użytkownika
        when(userDAO.findByUsername(username)).thenReturn(Optional.of(mockUser));

        Optional<User> result = authService.login(username, password);

        assertTrue(result.isPresent());
        assertEquals(username, result.get().getUsername());
    }

    @Test
    @DisplayName("Logowanie powinno przypisać identyfikator sesji")
    void loginShouldAssignSessionId() throws SQLException {
        String username = "testUser";
        String password = "correctPassword";

        User mockUser = new User();
        mockUser.setUsername(username);
        mockUser.setPasswordHash(BCrypt.hashpw(password, BCrypt.gensalt()));

        // Mockowanie użytkownika
        when(userDAO.findByUsername(username)).thenReturn(Optional.of(mockUser));

        Optional<User> result = authService.login(username, password);

        assertTrue(result.isPresent());
        assertNotNull(result.get().getSessionId());
    }
}
