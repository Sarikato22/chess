package dataaccess;

import chess.model.request.RegisterRequest;
import chess.model.result.RegisterResult;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MemoryDataAccess implements DataAccess {

    private final Map<String, String> users = new HashMap<>(); // username -> password
    private final Map<String, String> authTokens = new HashMap<>(); // authToken -> username

    @Override
    public RegisterResult registerUser(RegisterRequest request) throws Exception {
        if (users.containsKey(request.getUsername())) {
            return new RegisterResult(false, "Error: username already taken", null);
        }

        users.put(request.getUsername(), request.getPassword()); // (no hashing yet)
        String authToken = UUID.randomUUID().toString();
        authTokens.put(authToken, request.getUsername());

        return new RegisterResult(true, null, authToken);
    }

    @Override
    public void clear() {
        users.clear();
        authTokens.clear();
    }
}
