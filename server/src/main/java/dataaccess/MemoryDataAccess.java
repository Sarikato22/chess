package dataaccess;

import chess.model.request.RegisterRequest;
import chess.model.request.SessionRequest;
import chess.model.result.RegisterResult;
import chess.model.result.SessionResult;

import java.util.HashMap;
import java.util.Map;

public class MemoryDataAccess implements DataAccess {

    private final Map<String, String> users = new HashMap<>(); // username → password
    private final Map<String, String> authTokens = new HashMap<>(); // username → token

    public RegisterResult register(String username, String password, String email) {
        if (users.containsKey(username)) {
            return new RegisterResult(username, "Error: Username already taken");
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
    public RegisterResult registerUser(RegisterRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();
        String email = request.getEmail();

        if (users.containsKey(username)) {
            return RegisterResult.failure(username, "Error: already taken");
        }

        // Store user
        users.put(username, password);
        String token = "token_" + username;
        authTokens.put(username, token);

        return new RegisterResult(username, token);
    }

    public void clear() {
        users.clear();
        authTokens.clear();
    }
    //Session Code:
    @Override
    public SessionResult loginUser(SessionRequest request) {
        if (!users.containsKey(request.getUsername()) ||
                !users.get(request.getUsername()).equals(request.getPassword())) {
            return null; // invalid login
        }
        String token = "token_" + request.getUsername();
        authTokens.put(request.getUsername(), token);
        return new SessionResult(request.getUsername(), token);
    }

    @Override
    public boolean invalidateToken(String authToken) {
        return authTokens.values().remove(authToken);
    }

    @Override
    public String getUsernameByToken(String token) {
        return authTokens.entrySet().stream()
                .filter(e -> e.getValue().equals(token))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }


}
