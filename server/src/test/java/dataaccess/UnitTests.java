package dataaccess;

import chess.model.request.RegisterRequest;
import chess.model.request.SessionRequest;
import chess.model.result.RegisterResult;
import chess.model.result.SessionResult;
import dataaccess.DatabaseManager;
import org.junit.jupiter.api.*;
import service.ClearService;
import service.GameService;
import service.SessionService;
import service.UserService;

import javax.xml.crypto.Data;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UnitTests {

    private UserService userService;
    private SessionService sessionService;
    private GameService gameService;
    private ClearService clearService;
    private MySqlDataAccess dao;

    @BeforeEach
    public void setup() {
        dao = new MySqlDataAccess();
        userService = new UserService(dao);
        clearService = new ClearService(dao);
        clearService.clear();
        sessionService = new SessionService(dao);
        gameService = new GameService(dao);
    }

    private static String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS users (
                username VARCHAR(50) PRIMARY KEY,
                password VARCHAR(255) NOT NULL,
                email VARCHAR(100) NOT NULL
            );
            """,
            """
            CREATE TABLE IF NOT EXISTS auth_tokens (
                authToken CHAR(36) PRIMARY KEY,
                username VARCHAR(50) NOT NULL,
                FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
            );
            """,
            """
            CREATE TABLE IF NOT EXISTS games (
                gameID INT AUTO_INCREMENT PRIMARY KEY,
                whiteUsername VARCHAR(50),
                blackUsername VARCHAR(50),
                gameName VARCHAR(100) NOT NULL,
                game TEXT,
                FOREIGN KEY (whiteUsername) REFERENCES users(username) ON DELETE SET NULL,
                FOREIGN KEY (blackUsername) REFERENCES users(username) ON DELETE SET NULL
            );
            """
    };

    @BeforeAll
    static void setupDatabase() throws Exception {
        DatabaseManager.createDatabase();
        try (Connection conn = DatabaseManager.getConnection()) {
            for (String stmt : createStatements) {
                conn.createStatement().executeUpdate(stmt);
            }
        }
    }

    @Test
    @DisplayName("Check if 'users' table exists")
    void testUsersTableExists() throws Exception {
        assertTrue(tableExists("users"), "Table 'users' should exist");
    }

    @Test
    @DisplayName("Check if 'auth_tokens' table exists")
    void testAuthTokensTableExists() throws Exception {
        assertTrue(tableExists("auth_tokens"), "Table 'auth_tokens' should exist");
    }

    @Test
    @DisplayName("Check if 'games' table exists")
    void testGamesTableExists() throws Exception {
        assertTrue(tableExists("games"), "Table 'games' should exist");
    }

    private boolean tableExists(String tableName) throws Exception {
        try (Connection conn = DatabaseManager.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet rs = metaData.getTables(null, null, tableName, new String[]{"TABLE"})) {
                return rs.next();
            }
        }
    }

    @Test
    public void testRegisterUser_Success() throws DataAccessException, SQLException {
        // Arrange
        RegisterRequest request = new RegisterRequest("alice", "password123", "alice@example.com");

        // Act
        RegisterResult result = userService.register(request);

        // Assert
        assertNotNull(result, "RegisterResult should not be null");

        // Debug: if null, fail fast
        if (result == null) {
            fail("registerUser returned null, check the DAO or DB setup");
        }

        assertEquals("alice", result.getUsername(), "Username should match request");
        assertNotNull(result.getAuthToken(), "Auth token should be generated");
        assertFalse(result.getAuthToken().isEmpty(), "Auth token should not be empty");
        assertTrue(result.isSuccess(), "Registration should be successful");

        // Optional: verify directly from DB that token exists
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT authToken FROM auth_tokens WHERE username = ?")) {
            stmt.setString(1, "alice");
            ResultSet rs = stmt.executeQuery();

            assertTrue(rs.next(), "Auth token should exist in DB for 'alice'");
            String tokenInDb = rs.getString("authToken");
            assertEquals(result.getAuthToken(), tokenInDb, "Token in DB should match returned token");
        }
    }


    @Test
    public void testRegisterUser_DuplicateUsername() throws DataAccessException {
        // Arrange
        RegisterRequest request1 = new RegisterRequest("bob", "pass1", "bob@example.com");
        RegisterRequest request2 = new RegisterRequest("bob", "pass2", "bob2@example.com");

        // Act
        userService.register(request1);
        RegisterResult result = userService.register(request2);

        // Assert
        assertFalse(result.isSuccess());
        assertEquals("bob", result.getUsername());
        assertEquals("Error: already taken", result.getMessage());
    }

    @Test
    public void testRegisterUser_PasswordIsHashed() throws DataAccessException, SQLException {
        // Arrange
        String rawPassword = "mySecretPass";
        RegisterRequest request = new RegisterRequest("charlie", rawPassword, "charlie@example.com");

        // Act
        userService.register(request);

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT password FROM users WHERE username = ?")) {
            stmt.setString(1, "charlie");
            ResultSet rs = stmt.executeQuery();

            assertTrue(rs.next());
            String storedPassword = rs.getString("password");
            assertNotEquals(rawPassword, storedPassword); // raw password should NOT be stored
            assertTrue(storedPassword.startsWith("$2a$")); // bcrypt hashes start with $2a$, $2b$ or $2y$
        }
    }
    @Test
    public void testRegisterUser_AuthTokenExists() throws DataAccessException, SQLException {
        // Arrange
        RegisterRequest request = new RegisterRequest("dave", "pass123", "dave@example.com");

        // Act
        userService.register(request);

        // Verify token exists in DB
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(true); // ensure we see committed data from other connections

            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT authToken FROM auth_tokens WHERE username = ?")) {
                stmt.setString(1, "dave");
                ResultSet rs = stmt.executeQuery();

                assertTrue(rs.next(), "Auth token should exist for the user");
                String tokenInDb = rs.getString("authToken");
                assertNotNull(tokenInDb, "Auth token should not be null");
                assertFalse(tokenInDb.isEmpty(), "Auth token should not be empty");
            }
        }
    }
    //// Tests for login
    @Test
    public void testLoginUser_Success() throws Exception {
        // Arrange: register user first
        userService.register(new RegisterRequest("alice", "password123", "alice@example.com"));

        // Act: login with correct credentials
        SessionRequest loginReq = new SessionRequest("alice", "password123");
        SessionResult result = sessionService.login(loginReq);

        // Assert
        assertNotNull(result, "Result should not be null");
        assertEquals("alice", result.getUsername());
        assertNotNull(result.getAuthToken(), "Auth token should be generated");
    }

    @Test
    public void testLoginUser_WrongPassword() throws Exception {
        userService.register(new RegisterRequest("alice", "correctpassword", "alice@example.com"));
        SessionRequest loginReq = new SessionRequest("alice", "wrongpassword");
        SessionResult result = sessionService.login(loginReq);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("Error: Incorrect password", result.getMessage());
    }

    @Test
    public void testLoginUser_NonExistentUser() throws Exception {
        SessionRequest loginReq = new SessionRequest("bob", "password123");
        SessionResult result = sessionService.login(loginReq);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("Error: Username not found", result.getMessage());
    }
    //Invalidate Token tests

    @Test
    @DisplayName("Invalidate existing token succeeds")
    void testInvalidateToken_Existing() throws Exception {
        String token = "test-token-123";
        String username = "alice";

        // insert token
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO auth_tokens (authToken, username) VALUES (?, ?)")) {
            stmt.setString(1, token);
            stmt.setString(2, username);
            stmt.executeUpdate();
        }

        boolean result = dao.invalidateToken(token);
        assertTrue(result, "invalidateToken should return true for existing token");

        // verify it's gone
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM auth_tokens WHERE authToken = ?")) {
            stmt.setString(1, token);
            ResultSet rs = stmt.executeQuery();
            assertFalse(rs.next(), "Token should be removed from DB");
        }
    }

    @Test
    @DisplayName("Invalidate non-existent token returns false")
    void testInvalidateToken_NonExistent() throws Exception {
        boolean result = dao.invalidateToken("non-existent-token");
        assertFalse(result, "invalidateToken should return false for non-existent token");
    }

    @Test
    @DisplayName("Multiple tokens remain unaffected")
    void testInvalidateToken_OtherTokensRemain() throws Exception {
        String token1 = "token1";
        String token2 = "token2";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO auth_tokens (authToken, username) VALUES (?, ?)")) {
            stmt.setString(1, token1);
            stmt.setString(2, "alice");
            stmt.executeUpdate();

            stmt.setString(1, token2);
            stmt.setString(2, "bob");
            stmt.executeUpdate();
        }

        boolean result = dao.invalidateToken(token1);
        assertTrue(result, "invalidateToken should return true for token1");

        // token2 should still exist
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM auth_tokens WHERE authToken = ?")) {
            stmt.setString(1, token2);
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next(), "Other tokens should remain in the table");
        }
    }
}
