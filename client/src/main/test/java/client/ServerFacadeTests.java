package client;

import chess.model.request.GameRequest;
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
        try {
            facade.register(request);
            fail("Expected ResponseException for duplicate user");
        } catch (ResponseException ex) {
            assertEquals(ResponseException.Code.ServerError, ex.code());
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
        var headers = Map.of("Authorization", registerResult.getAuthToken());
        var gameResult = facade.createGame(gameRequest, headers);

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

        Map<String, String> headers = Map.of("authorization", authToken);

        var gameRequest1 = new GameRequest("GameOne");
        var gameRequest2 = new GameRequest("GameTwo");
        var createResult1 = facade.createGame(gameRequest1, headers);
        var createResult2 = facade.createGame(gameRequest2, headers);
        assertTrue(createResult1.isSuccess());
        assertTrue(createResult2.isSuccess());


        GameListResult listResult = facade.listGames(headers);

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
    public void testLogoutFailure_invalidToken() throws ResponseException {
        facade.clear();
        var badToken = "not-a-real-token";
        assertThrows(ResponseException.class, () -> facade.logout(badToken));
    }




}
