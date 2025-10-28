package dataaccess;

import chess.model.data.GameData;
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
    @DisplayName("Attempt to register a duplicated user")
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
    @DisplayName("Verify password is hashed after register")
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
    @DisplayName("Verify authToken gets generated on register")
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
    @DisplayName("Attempt to sucesfully log in")
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
    @DisplayName("Attempt to log in with wrong password")
    public void testLoginUser_WrongPassword() throws Exception {
        userService.register(new RegisterRequest("alice", "correctpassword", "alice@example.com"));
        SessionRequest loginReq = new SessionRequest("alice", "wrongpassword");
        SessionResult result = sessionService.login(loginReq);

        assertNotNull(result);
        assertFalse(result.isSuccess());
        assertEquals("Error: Incorrect password", result.getMessage());
    }

    @Test
    @DisplayName("Attempt to log in non-existent user")
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

        userService.register(new RegisterRequest(username, "password123", "alice@example.com"));

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO auth_tokens (authToken, username) VALUES (?, ?)")) {
            stmt.setString(1, token);
            stmt.setString(2, username);
            stmt.executeUpdate();
        }

        boolean result = dao.invalidateToken(token);
        assertTrue(result, "invalidateToken should return true for existing token");

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
        // First, create users so FK constraints are satisfied
        userService.register(new RegisterRequest("alice", "pass1", "alice@example.com"));
        userService.register(new RegisterRequest("bob", "pass2", "bob@example.com"));

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

    //getUsernamebyToken
    @Test
    @DisplayName("getUsernameByToken returns correct username for valid token")
    void testGetUsernameByToken_Valid() throws Exception {
        String username = "alice";

        RegisterResult result = userService.register(new RegisterRequest("alice", "password1", "alice@example.com"));
        String token = result.getAuthToken();

        //attempt to get the username
        String retrieved_username = dao.getUsernameByToken(token);
        assertNotNull(retrieved_username);
        assertEquals(username, retrieved_username);
    }
//Creategame tests
@Nested
@DisplayName("Game Creation Tests")
class GameCreationTests {

    @BeforeEach
    void setupGameUsers() throws Exception {
        // Insert only the users needed for these tests
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO users (username, password, email) VALUES (?, ?, ?)")) {

            String[][] users = {
                    {"alice", "hashedpass", "alice@example.com"},
                    {"bob", "hashedpass", "bob@example.com"},
                    {"charlie", "hashedpass", "charlie@example.com"},
                    {"dave", "hashedpass", "dave@example.com"}
            };

            for (String[] u : users) {
                stmt.setString(1, u[0]);
                stmt.setString(2, u[1]);
                stmt.setString(3, u[2]);
                try {
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    // ignore if user already exists
                }
            }
        }
    }

    @Test
    @DisplayName("createGame successfully inserts new game into database")
    void testCreateGame_Success() throws Exception {
        GameData game = new GameData(0, "Epic Match", "alice", "bob");
        GameData result = dao.createGame(game);

        assertNotNull(result);
        assertEquals("Epic Match", result.getGameName());
        assertEquals("alice", result.getWhiteUsername());
        assertEquals("bob", result.getBlackUsername());

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM games WHERE gameID = ?")) {
            stmt.setInt(1, result.getGameId());
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            assertEquals("Epic Match", rs.getString("gameName"));
        }
    }

    @Test
    @DisplayName("createGame allows null usernames for open slots")
    void testCreateGame_NullPlayers() throws Exception {
        GameData game = new GameData(0, "Open Game", null, null);
        GameData result = dao.createGame(game);

        assertNotNull(result);
        assertEquals("Open Game", result.getGameName());

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM games WHERE gameID = ?")) {
            stmt.setInt(1, result.getGameId());
            ResultSet rs = stmt.executeQuery();
            assertTrue(rs.next());
            assertNull(rs.getString("whiteUsername"));
            assertNull(rs.getString("blackUsername"));
        }
    }

    @Test
    @DisplayName("createGame assigns unique gameIDs")
    void testCreateGame_UniqueIDs() throws Exception {
        GameData g1 = dao.createGame(new GameData(0, "Game1", "alice", "bob"));
        GameData g2 = dao.createGame(new GameData(0, "Game2", "charlie", "dave"));

        assertNotEquals(g1.getGameId(), g2.getGameId());
    }
}




}