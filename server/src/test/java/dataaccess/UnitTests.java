package dataaccess;

import chess.model.data.GameData;
import chess.model.request.RegisterRequest;
import chess.model.request.SessionRequest;
import chess.model.result.RegisterResult;
import chess.model.result.SessionResult;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UnitTests {

    private static MySqlDataAccess dao;

    @BeforeAll
    static void setup() {
        dao = new MySqlDataAccess();
        dao.clear(); // start clean
    }

    @Test
    @Order(1)
    void registerUserPositive() throws Exception {
        RegisterRequest request = new RegisterRequest("alice", "password123", "alice@example.com");
        RegisterResult result = dao.registerUser(request);
        assertNotNull(result);
        assertEquals("alice", result.getUsername());
        assertNotNull(result.getAuthToken());
    }

    @Test
    @Order(2)
    void registerUserNegativeDuplicateUsername() throws Exception {
        RegisterRequest request = new RegisterRequest("alice", "newpass", "alice2@example.com");
        RegisterResult result = dao.registerUser(request);
        assertTrue(result.getMessage().contains("already taken"));
    }

    @Test
    @Order(3)
    void loginUserPositive() throws Exception {
        SessionRequest req = new SessionRequest("alice", "password123");
        SessionResult result = dao.loginUser(req);
        assertEquals("alice", result.getUsername());
        assertNotNull(result.getAuthToken());
    }

    @Test
    @Order(4)
    void loginUserNegativeWrongPassword() throws Exception {
        SessionRequest req = new SessionRequest("alice", "wrongpass");
        SessionResult result = dao.loginUser(req);
        assertTrue(result.getMessage().contains("Incorrect password"));
    }

    @Test
    @Order(5)
    void invalidateTokenPositive() throws Exception {
        SessionRequest req = new SessionRequest("alice", "password123");
        SessionResult result = dao.loginUser(req);
        assertTrue(dao.invalidateToken(result.getAuthToken()));
    }

    @Test
    @Order(6)
    void invalidateTokenNegativeInvalidToken() {
        assertThrows(UnauthorizedException.class, () -> dao.invalidateToken("nonexistent-token"));
    }

    @Test
    @Order(7)
    void getUsernameByTokenPositive() throws Exception {
        SessionRequest req = new SessionRequest("alice", "password123");
        SessionResult result = dao.loginUser(req);
        String username = dao.getUsernameByToken(result.getAuthToken());
        assertEquals("alice", username);
    }

    @Test
    @Order(8)
    void getUsernameByTokenNegativeInvalidToken() throws Exception {
        String username = dao.getUsernameByToken("fake-token");
        assertNull(username);
    }

    @Test
    @Order(9)
    void createGamePositive() throws Exception {
        SessionRequest req = new SessionRequest("alice", "password123");
        SessionResult result = dao.loginUser(req);

        GameData game = new GameData(0, "Chess Battle", null, null);
        GameData created = dao.createGame(game, result.getAuthToken());

        assertNotNull(created);
        assertEquals("Chess Battle", created.getGameName());
    }

    @Test
    @Order(10)
    @DisplayName("Test createGame with invalid token (Unauthorized)")
    void createGameNegativeUnauthorized() {
        // Create the GameData object
        GameData game = new GameData(0, "Unauthorized Game", "bob", "charlie");

        // Ensure the invalid token is passed
        String invalidToken = "invalid-token";

        // You should see logging in createGame indicating entry
        assertThrows(DataAccessException.class, () -> {
            dao.createGame(game, invalidToken);  // Test target
        });
    }

    @Test
    @Order(11)
    void listGamesPositive() throws Exception {
        List<GameData> games = dao.listGames();
        assertNotNull(games);
        assertFalse(games.isEmpty());
    }

    @Test
    @Order(12)
    void listGamesNegativeEmptyDatabase() throws DataAccessException {
        dao.clear();
        List<GameData> games = dao.listGames();
        assertTrue(games.isEmpty());
    }

    @Test
    @Order(13)
    void updateGamePositive() throws Exception {
        dao.registerUser(new RegisterRequest("bob", "bobpass", "bob@example.com"));
        SessionResult session = dao.loginUser(new SessionRequest("bob", "bobpass"));
        GameData game = new GameData(0, "Match1", "bob", null);
        GameData created = dao.createGame(game, session.getAuthToken());

        dao.registerUser(new RegisterRequest("alice", "alicepass", "alice@example.com"));

        created.setBlackUsername("alice");
        dao.updateGame(created);

        GameData updated = dao.getGame(created.getGameId());
        assertEquals("alice", updated.getBlackUsername());
    }

    @Test
    @Order(14)
    void updateGameNegativeNotFound() {
        GameData nonexistent = new GameData(999, "Ghost Game", null, null);
        assertThrows(DataAccessException.class, () -> dao.updateGame(nonexistent));
    }

    @Test
    @Order(15)
    void getGamePositive() throws Exception {
        List<GameData> games = dao.listGames();
        assertFalse(games.isEmpty());
        GameData first = games.get(0);
        GameData result = dao.getGame(first.getGameId());
        assertNotNull(result);
        assertEquals(first.getGameId(), result.getGameId());
    }

    @Test
    @Order(16)
    void getGameNegativeNotFound() throws Exception {
        GameData result = dao.getGame(9999);
        assertNull(result);
    }

    @Test
    @Order(17)
    void clearPositive() throws DataAccessException {
        dao.clear();
        List<GameData> games = dao.listGames();
        assertTrue(games.isEmpty());
    }

    @Test
    @Order(18)
    void updateGameNegativeInvalidGameId() {

        GameData nonexistentGame = new GameData(9999, "Non-existent Game", "bob", "alice");

        assertThrows(DataAccessException.class, () -> dao.updateGame(nonexistentGame));
    }

}
