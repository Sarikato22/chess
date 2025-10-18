package dataaccess;

import chess.model.request.RegisterRequest;
import chess.model.result.RegisterResult;
import java.util.HashMap;
import java.util.Map;

public class MemoryDataAccess implements DataAccess {

    private final Map<String, String> users = new HashMap<>(); // username → password
    private final Map<String, String> authTokens = new HashMap<>(); // username → token

    public RegisterResult register(String username, String password, String email) {
        if (users.containsKey(username)) {
            return new RegisterResult("Username already taken");
        }

        // Store the user in memory
        users.put(username, password);

        // Create fake auth token
        String token = "token_" + username;
        authTokens.put(username, token);

        // Return a success result
        return new RegisterResult(username, token);
    }

    @Override
    public RegisterResult registerUser(RegisterRequest request) throws Exception {
        return null;
    }

    // Optional: for testing
    public void clear() {
        users.clear();
        authTokens.clear();
    }
}
