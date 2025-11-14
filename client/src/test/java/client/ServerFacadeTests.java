package client;

import chess.ChessGame;
import chess.model.request.GameRequest;
import chess.model.request.JoinGameRequest;
import chess.model.request.RegisterRequest;
import chess.model.request.SessionRequest;
import chess.model.result.GameListResult;
import chess.model.result.RegisterResult;
import chess.server.ResponseException;
import chess.server.ServerFacade;
import org.junit.jupiter.api.*;
import server.Server;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade("http://localhost:" + port);

    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }


    @Test
    public void sampleTest() {
        assertTrue(true);
    }

    //Register tests
    @Test
    public void testRegisterSuccess() throws Exception {
        facade.clear();

        var request = new RegisterRequest("testUser", "password123", "test@example.com");
        var result = facade.register(request);

        System.out.println("Result: success=" + result.isSuccess() +
                ", message=" + result.getMessage() +
                ", username=" + result.getUsername());


        assertNotNull(result);
        assertTrue(result.isSuccess(), "Expected registration to succeed");
        assertEquals("testUser", result.getUsername());
        assertNotNull(result.getAuthToken(), "Auth token should not be null");
        assertNull(result.getMessage(), "There should be no error message on success");
    }

    @Test
    public void testRegisterDuplicateUser() throws Exception {
        var facade = new ServerFacade("http://localhost:8080");

        var request = new RegisterRequest("existingUser", "password123", "dup@example.com");
        facade.register(request);
        try {
            facade.register(request);
            fail("Expected ResponseException for duplicate user");
        } catch (ResponseException ex) {
            assertEquals(ResponseException.Code.ClientError, ex.code());
        }
    }

    //Test for createGame
    @Test
    public void testCreateGameSuccess() throws Exception {
        facade.clear();

        // Register a user first
        var registerRequest = new RegisterRequest("testUser1", "password123", "test@example.com");
        var registerResult = facade.register(registerRequest);

        assertTrue(registerResult.isSuccess(), "Registration should succeed");

        // Create game using the auth token
        var gameRequest = new GameRequest("MyFirstGame");
        var authToken = registerResult.getAuthToken();
        var gameResult = facade.createGame(gameRequest, authToken);


        System.out.println("Result: success=" + gameResult.isSuccess() +
                ", message=" + gameResult.getMessage() +
                ", gameId=" + gameResult.getGameID());

        assertNotNull(gameResult);
        assertTrue(gameResult.isSuccess(), "Expected game creation to succeed");
        assertNotNull(gameResult.getGameID(), "Game ID should not be null");
        assertNull(gameResult.getMessage(), "There should be no error message on success");
    }

    @Test
    public void testListGames() throws Exception {
        facade.clear();

        var registerRequest = new RegisterRequest("testUser1", "password123", "test@example.com");
        var registerResult = facade.register(registerRequest);
        assertTrue(registerResult.isSuccess(), "User registration should succeed");
        String authToken = registerResult.getAuthToken();

        var gameRequest1 = new GameRequest("GameOne");
        var gameRequest2 = new GameRequest("GameTwo");
        var createResult1 = facade.createGame(gameRequest1, authToken);
        var createResult2 = facade.createGame(gameRequest2, authToken);
        assertTrue(createResult1.isSuccess());
        assertTrue(createResult2.isSuccess());


        GameListResult listResult = facade.listGames(authToken);

        System.out.println("Games retrieved:");
        for (var game : listResult.getGames()) {
            System.out.println("GameID=" + game.getGameId() + ", Name=" + game.getGameName());
        }

        // Assertions
        assertNotNull(listResult);
        assertTrue(listResult.isSuccess(), "Listing games should succeed");
        assertEquals(2, listResult.size(), "There should be 2 games listed");
        List<String> gameNames = listResult.getGames().stream().map(g -> g.getGameName()).toList();
        assertTrue(gameNames.contains("GameOne"));
        assertTrue(gameNames.contains("GameTwo"));
    }
    //login test
    @Test
    public void testLoginSuccess() throws Exception {
        facade.clear();

        var registerRequest = new RegisterRequest("loginUser", "password123", "login@example.com");
        var registerResult = facade.register(registerRequest);

        assertTrue(registerResult.isSuccess(), "Registration should succeed for login test");
        assertNotNull(registerResult.getAuthToken(), "Auth token should not be null after registration");

        var loginRequest = new SessionRequest("loginUser", "password123");
        var loginResult = facade.login(loginRequest);

        System.out.println("Login Result: success=" + loginResult.isSuccess() +
                ", username=" + loginResult.getUsername() +
                ", message=" + loginResult.getMessage() +
                ", authToken=" + loginResult.getAuthToken());

        assertNotNull(loginResult);
        assertTrue(loginResult.isSuccess(), "Expected login to succeed");
        assertEquals("loginUser", loginResult.getUsername(), "Usernames should match");
        assertNotNull(loginResult.getAuthToken(), "Auth token should not be null on successful login");
        assertNull(loginResult.getMessage(), "Message should be null on success");
    }
    //logout test
    @Test
    public void testLogoutSuccess() throws Exception {
        facade.clear();

        var registerReq = new RegisterRequest("logoutUser", "password123", "logout@example.com");
        var registerRes = facade.register(registerReq);
        assertTrue(registerRes.isSuccess());

        var loginReq = new SessionRequest("logoutUser", "password123");
        var loginRes = facade.login(loginReq);
        assertTrue(loginRes.isSuccess());
        assertNotNull(loginRes.getAuthToken());

        var logoutRes = facade.logout(loginRes.getAuthToken());
        assertNotNull(logoutRes);
        assertTrue(logoutRes.isSuccess(), "Logout should succeed");
    }

    @Test
    public void testLogoutFailureInvalidToken() throws ResponseException {
        facade.clear();
        var badToken = "not-a-real-token";
        assertThrows(ResponseException.class, () -> facade.logout(badToken));
    }

    //test JoinGame
    @Test
    public void testJoinGameSuccess() throws Exception {
        facade.clear();
        var userReq = new RegisterRequest("joinUser", "pass123", "email@example.com");
        var userRes = facade.register(userReq);
        assertTrue(userRes.isSuccess());

        var loginReq = new SessionRequest("joinUser", "pass123");
        var loginRes = facade.login(loginReq);
        assertTrue(loginRes.isSuccess());
        String authToken = loginRes.getAuthToken();

        var createReq = new GameRequest("Cool Match");
        var createRes = facade.createGame(createReq, authToken);
        assertTrue(createRes.isSuccess());
        assertNotNull(createRes.getGameID());

        var joinReq = new JoinGameRequest(ChessGame.TeamColor.WHITE, createRes.getGameID());
        var joinRes = facade.joinGame(authToken, joinReq);

        assertTrue(joinRes.isSuccess());
        assertEquals("Joined game successfully", joinRes.getMessage());
    }

    @Test
    public void testJoinGameUnauthorized() throws Exception {

        facade.clear();

        var regReq = new RegisterRequest("joinUserNeg", "password", "joinUserNeg@example.com");
        var regRes = facade.register(regReq);
        assertTrue(regRes.isSuccess());


        var headers = regRes.getAuthToken();
        var createReq = new GameRequest("UnauthorizedGame");
        var createRes = facade.createGame(createReq,headers);
        assertTrue(createRes.isSuccess());
        assertNotNull(createRes.getGameID());

        // Try to join with an INVALID token
        var invalidHeaders = Map.of("authorization", "invalid-token");
        var joinReq = new JoinGameRequest(ChessGame.TeamColor.WHITE, createRes.getGameID());

        ResponseException thrown = assertThrows(ResponseException.class, () -> {
            facade.joinGame("invalid-token", joinReq);
        });

        assertEquals(ResponseException.Code.Unauthorized, thrown.getCode(), "Expected unauthorized error code");
        assertTrue(thrown.getMessage().toLowerCase().contains("unauthorized"), "Expected 'unauthorized' in error message");
    }

    @Test
    public void testCreateGameUnauthorized() throws Exception {
        facade.clear();

        var gameRequest = new GameRequest("BadGame");

        assertThrows(ResponseException.class, () -> {
            facade.createGame(gameRequest, "invalid-token");
        });
    }

    @Test
    public void testListGamesUnauthorized() throws ResponseException {
        facade.clear();

        assertThrows(ResponseException.class, () -> {
            facade.listGames("invalid-token");
        });
    }

    @Test
    public void testClearSuccess() throws Exception {
        var registerRequest = new RegisterRequest("clearUser", "password123", "clear@example.com");
        var registerResult = facade.register(registerRequest);

        assertTrue(registerResult.isSuccess());
        assertNotNull(registerResult.getAuthToken());

        assertDoesNotThrow(() -> facade.clear(), "Clear should not throw an exception");

        var loginRequest = new SessionRequest("clearUser", "password123");

        assertThrows(ResponseException.class, () -> {
            facade.login(loginRequest);
        }, "Expected login to fail after clear() resets the database");
    }


}
