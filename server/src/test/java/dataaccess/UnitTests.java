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
    void registerUser_positive() throws Exception {
        RegisterRequest request = new RegisterRequest("alice", "password123", "alice@example.com");
        RegisterResult result = dao.registerUser(request);
        assertNotNull(result);
        assertEquals("alice", result.getUsername());
        assertNotNull(result.getAuthToken());
    }

    @Test
    @Order(2)
    void registerUser_negative_duplicateUsername() throws Exception {
        RegisterRequest request = new RegisterRequest("alice", "newpass", "alice2@example.com");
        RegisterResult result = dao.registerUser(request);
        assertTrue(result.getMessage().contains("already taken"));
    }

    @Test
    @Order(3)
    void loginUser_positive() throws Exception {
        SessionRequest req = new SessionRequest("alice", "password123");
        SessionResult result = dao.loginUser(req);
        assertEquals("alice", result.getUsername());
        assertNotNull(result.getAuthToken());
    }

    @Test
    @Order(4)
    void loginUser_negative_wrongPassword() throws Exception {
        SessionRequest req = new SessionRequest("alice", "wrongpass");
        SessionResult result = dao.loginUser(req);
        assertTrue(result.getMessage().contains("Incorrect password"));
    }


    @Test
    @Order(5)
    void invalidateToken_positive() throws Exception {
        SessionRequest req = new SessionRequest("alice", "password123");
        SessionResult result = dao.loginUser(req);
        assertTrue(dao.invalidateToken(result.getAuthToken()));
    }

    @Test
    @Order(6)
    void invalidateToken_negative_invalidToken() {
        assertThrows(UnauthorizedException.class, () -> dao.invalidateToken("nonexistent-token"));
    }
    @Test
    @Order(7)
    void getUsernameByToken_positive() throws Exception {
        SessionRequest req = new SessionRequest("alice", "password123");
        SessionResult result = dao.loginUser(req);
        String username = dao.getUsernameByToken(result.getAuthToken());
        assertEquals("alice", username);
    }

    @Test
    @Order(8)
    void getUsernameByToken_negative_invalidToken() throws Exception {
        String username = dao.getUsernameByToken("fake-token");
        assertNull(username);
    }

    @Test
    @Order(9)
    void createGame_positive() throws Exception {
        SessionRequest req = new SessionRequest("alice", "password123");
        SessionResult result = dao.loginUser(req);

        GameData game = new GameData(0, "Chess Battle", "alice", null);
        GameData created = dao.createGame(game, result.getAuthToken());

        assertNotNull(created);
        assertEquals("Chess Battle", created.getGameName());
        assertEquals("alice", created.getWhiteUsername());
    }

    @Test
    @Order(10)
    @DisplayName("Test createGame with invalid token (Unauthorized)")
    void createGame_negative_unauthorized() {
        // Create the GameData object
        GameData game = new GameData(0, "Unauthorized Game", "bob", "charlie");

        // Ensure the invalid token is passed
        String invalidToken = "invalid-token";

        // You should see logging in createGame indicating entry
        assertThrows(UnauthorizedException.class, () -> {
            dao.createGame(game, invalidToken);  // Test target
        });
    }



    @Test
    @Order(11)
    void listGames_positive() throws Exception {
        List<GameData> games = dao.listGames();
        assertNotNull(games);
        assertFalse(games.isEmpty());
    }

    @Test
    @Order(12)
    void listGames_negative_emptyDatabase() throws DataAccessException {
        dao.clear();
        List<GameData> games = dao.listGames();
        assertTrue(games.isEmpty());
    }

    @Test
    @Order(13)
    void updateGame_positive() throws Exception {
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
    void updateGame_negative_notFound() {
        GameData nonexistent = new GameData(999, "Ghost Game", null, null);
        assertThrows(DataAccessException.class, () -> dao.updateGame(nonexistent));
    }

    @Test
    @Order(15)
    void getGame_positive() throws Exception {
        List<GameData> games = dao.listGames();
        assertFalse(games.isEmpty());
        GameData first = games.get(0);
        GameData result = dao.getGame(first.getGameId());
        assertNotNull(result);
        assertEquals(first.getGameId(), result.getGameId());
    }

    @Test
    @Order(16)
    void getGame_negative_notFound() throws Exception {
        GameData result = dao.getGame(9999);
        assertNull(result);
    }
    @Test
    @Order(17)
    void clear_positive() throws DataAccessException {
        dao.clear();
        List<GameData> games = dao.listGames();
        assertTrue(games.isEmpty());
    }
}
