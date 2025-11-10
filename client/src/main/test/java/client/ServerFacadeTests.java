package client;

import chess.model.request.GameRequest;
import chess.model.request.RegisterRequest;
import chess.model.result.RegisterResult;
import chess.server.ResponseException;
import chess.server.ServerFacade;
import org.junit.jupiter.api.*;
import server.Server;

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


}
