package service;

import chess.model.data.GameData;
import chess.model.request.GameRequest;
import chess.model.request.RegisterRequest;
import chess.model.request.SessionRequest;
import chess.model.result.GameListResult;
import chess.model.result.GameResult;
import chess.model.result.RegisterResult;
import chess.model.result.SessionResult;
import dataaccess.MemoryDataAccess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UnitTests {

    private UserService userService;
    private SessionService sessionService;
    private GameService gameService;
    //Tests for clear:
    private ClearService clearService;

    @BeforeEach
    public void setup() {
        // Each test starts with a fresh in-memory database
        MemoryDataAccess dao = new MemoryDataAccess();
        userService = new UserService(dao);
        clearService = new ClearService(dao);
        sessionService = new SessionService(dao);
        gameService = new GameService(dao);
    }

    //Positive test for register
    @Test
    @DisplayName("Register new user successfully")
    public void testRegisterSuccess() {
        RegisterRequest request = new RegisterRequest("Alice", "password123", "alice@email.com");
        RegisterResult result = userService.register(request);

        assertTrue(result.isSuccess(), "Registration should succeed");
        assertEquals("Alice", result.getUsername());
        assertNotNull(result.getAuthToken(), "Auth token should be generated");
        assertNull(result.getMessage(), "Message should be null for success");
    }

    //Negative test for register, user already exists
    @Test
    @DisplayName("Register duplicate username fails")
    public void testRegisterDuplicateUsername() {
        RegisterRequest request1 = new RegisterRequest("Bob", "password123", "bob@email.com");
        RegisterRequest request2 = new RegisterRequest("Bob", "newpassword", "bob2@email.com");

        userService.register(request1); // first registration succeeds
        RegisterResult result = userService.register(request2); // second registration should fail

        assertFalse(result.isSuccess(), "Duplicate username should fail");
        assertEquals("Bob", result.getUsername(), "Username field may still be returned or null depending on " +
                "implementation");
        assertNull(result.getAuthToken(), "No auth token should be returned on failure");
        assertTrue(result.getMessage().contains("already taken"), "Error message should indicate username conflict");
    }

    //Missing Username, should fail
    @Test
    void testRegisterMissingUsername() {
        RegisterRequest request = new RegisterRequest(null, "pass", "email@example.com");
        RegisterResult result = userService.register(request);

        assertFalse(result.isSuccess(), "Missing username should fail");
        assertTrue(result.getMessage().toLowerCase().contains("username"));
    }

    //Missing password, should fail
    @Test
    void testRegisterMissingPassword() {
        RegisterRequest request = new RegisterRequest("Charlie", null, "email@example.com");
        RegisterResult result = userService.register(request);

        assertFalse(result.isSuccess(), "Missing password should fail");
        assertTrue(result.getMessage().toLowerCase().contains("password"));
    }

    @Test
    @DisplayName("Clear database successfully removes all users")
    public void testClearSuccess() {
        RegisterRequest request = new RegisterRequest("Diana", "secret", "diana@email.com");
        RegisterResult result = userService.register(request);
        assertTrue(result.isSuccess(), "Registration should succeed before clear");

        clearService.clear();

        RegisterResult resultAfterClear = userService.register(request);
        assertTrue(resultAfterClear.isSuccess(), "After clear, registration should succeed again");
    }

    @Test
    @DisplayName("Clear on empty database should not throw")
    public void testClearEmptyDatabase() {
        assertDoesNotThrow(() -> clearService.clear(), "Clearing an empty database should not throw an exception");
    }

    //Session tests:
    @Test
    @DisplayName("Login success with valid credentials")
    void testLoginSuccess() {
        RegisterRequest register = new RegisterRequest("Alice", "password123", "alice@email.com");
        userService.register(register);

        // Act: attempt to login
        SessionRequest login = new SessionRequest("Alice", "password123");
        SessionResult result = sessionService.login(login); // use instance, not static

        // Assert
        assertTrue(result.isSuccess(), "Login should succeed with correct credentials");
        assertEquals("Alice", result.getUsername());
        assertNotNull(result.getAuthToken(), "Auth token should be returned on successful login");
    }

    @Test
    @DisplayName("Login fails with wrong password")
    void testLoginWrongPassword() {
        RegisterRequest register = new RegisterRequest("Bob", "securePass", "bob@email.com");
        userService.register(register);

        // Act: attempt to login with wrong password
        SessionRequest login = new SessionRequest("Bob", "wrongPass");
        SessionResult result = sessionService.login(login); // use instance

        // Assert
        assertFalse(result.isSuccess(), "Login should fail with wrong password");
        assertNull(result.getAuthToken(), "No auth token should be returned on failure");
        assertTrue(result.getMessage().toLowerCase().contains("password"));
    }

    @Test
    @DisplayName("Logout invalidates session")
    void testLogout() throws Exception {
        RegisterRequest register = new RegisterRequest("Charlie", "pass123", "charlie@email.com");
        userService.register(register);
        SessionRequest login = new SessionRequest("Charlie", "pass123");
        SessionResult loginResult = sessionService.login(login); // use instance

        // Act: logout
        SessionResult logoutResult = sessionService.logout(loginResult.getAuthToken()); // use instance

        // Assert
        assertTrue(logoutResult.isSuccess(), "Logout should succeed");
        assertNull(sessionService.getUserByToken(loginResult.getAuthToken()), "Auth token should be invalidated"); //
        // instance
    }

    //tests for create game
    @Test
    @DisplayName("Create game succeeds with valid auth token and name")
    void testCreateGameSuccess() throws Exception {
        // Arrange: register and log in
        RegisterRequest register = new RegisterRequest("Alice", "password123", "alice@email.com");
        userService.register(register);
        SessionRequest login = new SessionRequest("Alice", "password123");
        SessionResult session = sessionService.login(login);

        // Act: create a game
        GameRequest request = new GameRequest("My First Game");
        GameResult result = gameService.createGame(request ,session.getAuthToken());

        // Assert
        assertTrue(result.isSuccess(), "Game creation should succeed");
        assertTrue(result.getGameID() > 0, "Game ID should be positive");
    }

    @Test
    @DisplayName("Create game fails with missing auth token")
    void testCreateGameUnauthorized() throws Exception {
        // Arrange
        GameRequest request = new GameRequest("Lonely Game");

        // Act
        GameResult result = gameService.createGame(null, request.getGameName());

        // Assert
        assertFalse(result.isSuccess(), "Should fail without auth token");
        assertTrue(result.getMessage().toLowerCase().contains("unauthorized"));
    }

    @Test
    @DisplayName("Create game fails with missing game name")
    void testCreateGameBadRequest() throws Exception {
        // Arrange: valid auth
        RegisterRequest register = new RegisterRequest("Bob", "pass123", "bob@email.com");
        userService.register(register);
        SessionRequest login = new SessionRequest("Bob", "pass123");
        SessionResult session = sessionService.login(login);

        // Act
        GameRequest request = new GameRequest(null);
        GameResult result = gameService.createGame(request, session.getAuthToken());

        // Assert
        assertFalse(result.isSuccess(), "Should fail with missing game name");
        assertTrue(result.getMessage().toLowerCase().contains("bad request"));
    }

    // list games tests
    @Test
    @DisplayName("List games succeeds with valid auth")
    void testListGamesSuccess() throws Exception {
        // Arrange: register, login, create some games
        RegisterRequest register = new RegisterRequest("Alice", "password123", "alice@email.com");
        userService.register(register);
        SessionRequest login = new SessionRequest("Alice", "password123");
        SessionResult session = sessionService.login(login);

        GameRequest game1 = new GameRequest("First Game");
        GameRequest game2 = new GameRequest("Second Game");
        gameService.createGame(game1,session.getAuthToken());
        gameService.createGame(game2,session.getAuthToken());


        GameListResult gameList = gameService.listGames(session.getAuthToken());
        assertEquals(2, gameList.size());
        assertTrue(gameList.isSuccess(), "Listing games should succeed");

    }

    @Test
    @DisplayName("List games fails with missing auth token")
    void testListGamesUnauthorized() throws Exception {
        // Act: call listGames(null)
        GameListResult gameList = gameService.listGames(null);
        // Assert: fail with unauthorized
        assertFalse(gameList.isSuccess(), "Should fail without auth token");
        assertTrue(gameList.getMessage().toLowerCase().contains("unauthorized"));
        assertNull(gameList.getGames(), "Games list should be null on failure");

    }

    @Test
    @DisplayName("List games returns empty list if no games exist")
    void testListGamesEmpty() throws Exception {
        // Arrange: register and login, but don't create any games
        RegisterRequest register = new RegisterRequest("Alice", "password123", "alice@email.com");
        userService.register(register);
        SessionRequest login = new SessionRequest("Alice", "password123");
        SessionResult session = sessionService.login(login);

        // Act: list games
        GameListResult gameList = gameService.listGames(session.getAuthToken());

        // Assert: success, but list is empty
        assertTrue(gameList.isSuccess(), "Listing games should succeed");
        assertEquals(0, gameList.size(), "Game list should be empty when no games exist");
        assertNotNull(gameList.getGames(), "Games list should not be null even if empty");
    }

    //
    @Test
    void testGameData() {
        GameData game = new GameData(1, "Debug Game", null, null);

        assertEquals(1, game.getGameId());
        assertEquals("Debug Game", game.getGameName());
        assertNull(game.getWhiteUsername());
        assertNull(game.getBlackUsername());
    }


}//end of class