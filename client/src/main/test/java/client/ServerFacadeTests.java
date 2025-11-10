package client;

import chess.model.request.RegisterRequest;
import chess.server.ResponseException;
import chess.server.ServerFacade;
import org.junit.jupiter.api.*;
import server.Server;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);

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
        var facade = new ServerFacade("http://localhost:8080");

        var request = new RegisterRequest("testUser1", "password123", "test@example.com");
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
}
