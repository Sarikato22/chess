package dataaccess;

import chess.ChessGame;
import chess.model.data.GameData;
import chess.model.request.RegisterRequest;
import chess.model.request.SessionRequest;
import chess.model.result.RegisterResult;
import chess.model.result.SessionResult;

import java.util.*;

public class MemoryDataAccess implements DataAccess {

    private final Map<String, String> users = new HashMap<>();
    private final Map<String, String> authTokens = new HashMap<>();
    private final Map<Integer, GameData> games = new HashMap<>();
    private int nextGameId = 1;


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
        String token = UUID.randomUUID().toString();
        authTokens.put(username, token);

        return new RegisterResult(username, token);
    }

    public void clear() {
        games.clear();
        users.clear();
        authTokens.clear();
    }

    @Override
    public SessionResult loginUser(SessionRequest request) {

        String username = request.getUsername();
        String password = request.getPassword();

        if (!users.containsKey(username) || !users.get(username).equals(password)) {
            return null;
        }

        String token = UUID.randomUUID().toString();
        authTokens.put(username, token);
        System.out.println("Generated token for " + username + ": " + token);

        return new SessionResult(username, token);
    }

    @Override
    public boolean invalidateToken(String authToken) {
        String keyToRemove = null;
        for (Map.Entry<String, String> entry : authTokens.entrySet()) {
            if (entry.getValue().equals(authToken)) {
                keyToRemove = entry.getKey();
                break;
            }
        }
        if (keyToRemove != null) {
            authTokens.remove(keyToRemove);
            return true;
        }
        return false;
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
        GameData newGame = new GameData(id, game.getGameName(), null, null);
        games.put(id, newGame);
        return newGame;
    }
    public void joinPlayer(int gameID, String username, ChessGame.TeamColor color) throws DataAccessException {
        GameData game = games.get(gameID);
        if (game == null) throw new DataAccessException("Game not found");

        if (color == ChessGame.TeamColor.WHITE) {
            game = new GameData(game.getGameId(), game.getGameName(), username, game.getBlackUsername());
        } else {
            game = new GameData(game.getGameId(), game.getGameName(), game.getWhiteUsername(), username);
        }

        games.put(gameID, game);
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {

        try {
            return new ArrayList<>(games.values());
        } catch (Exception e) {
            throw new DataAccessException("Unable to list games: " + e.getMessage());
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {

        GameData game = games.get(gameID);
        if (game == null) {
            throw new DataAccessException("Game not found");
        }
        return game;
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        int id = game.getGameId(); // cannot be null
        if (!games.containsKey(id)) {
            throw new DataAccessException("Game not found");
        }
        games.put(id, game); // use the same object
    }



}
