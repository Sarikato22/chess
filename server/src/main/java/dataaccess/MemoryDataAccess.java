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
        authTokens.put(token, username);

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
            return null; // invalid login
        }

        String token = UUID.randomUUID().toString();
        authTokens.put(token, username);
        System.out.println("Generated token for " + username + ": " + token);

        return new SessionResult(username, token);

    }

    public boolean invalidateToken(String authToken) {

        if (authTokens.containsKey(authToken) == false) {
            return false;
        }
        authTokens.remove(authToken);
        return true;
    }

    @Override
    public String getUsernameByToken(String token) {
        return authTokens.get(token);
    }

    @Override
    public GameData createGame(GameData game, String authToken) throws DataAccessException {
        int id = nextGameId++;
        GameData newGame = new GameData(id, game.getGameName(), game.getWhiteUsername(), game.getBlackUsername());
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

    @Override
    public GameData getGameData(int gameID) throws DataAccessException {

        GameData game = games.get(gameID);
        if (game == null) {
            throw new DataAccessException("Game not found");
        }
        return game;
    }

    @Override
    public ChessGame getChessGame(int gameID) throws DataAccessException {
        return null;
    }

    @Override
    public void updateChessGame(int gameID, ChessGame game) throws DataAccessException {

    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        if (game == null || game.getGameId() <= 0) {
            throw new DataAccessException("Cannot update game with null ID");
        }

        if (!games.containsKey(game.getGameId())) {
            throw new DataAccessException("Game not found");
        }

        games.put(game.getGameId(), game);
    }


}
