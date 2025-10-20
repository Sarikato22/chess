package services;

import chess.model.request.RegisterRequest;
import chess.model.result.RegisterResult;
import dataaccess.MemoryDataAccess;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class UnitTests {

    private UserService userService;

    @BeforeEach
    public void setup() {
        // Each test starts with a fresh in-memory database
        MemoryDataAccess dao = new MemoryDataAccess();
        userService = new UserService(dao);
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
        assertEquals("Bob", result.getUsername(), "Username field may still be returned or null depending on implementation");
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

}//end of class