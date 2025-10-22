package dataaccess;

import chess.model.data.GameData;
import chess.model.request.RegisterRequest;
import chess.model.request.SessionRequest;
import chess.model.result.RegisterResult;
import chess.model.result.SessionResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemoryDataAccess implements DataAccess {

    private final Map<String, String> users = new HashMap<>();
    private final Map<String, String> authTokens = new HashMap<>();
    private final Map<Integer, GameData> games = new HashMap<>();
    private int nextGameId = 1;

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

    @Override
    public GameData createGame(GameData game) throws DataAccessException {
        int id = nextGameId++;
        GameData newGame = new GameData(id, game.getGameName(), game.getCreatorUsername());

        games.put(id, newGame);
        return newGame;
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {

        try {

            return new ArrayList<>(games.values());
        } catch (Exception e) {
            throw new DataAccessException("Unable to list games: " + e.getMessage());
        }
    }


}
