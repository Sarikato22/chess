package services;

import chess.ChessGame;
import chess.model.request.JoinGameRequest;
import chess.model.result.GameListResult;
import chess.model.result.JoinGameResult;
import chess.model.result.RegisterResult;
import dataaccess.DataAccess;
import chess.model.data.GameData;
import chess.model.request.GameRequest;
import chess.model.result.GameResult;
import dataaccess.DataAccessException;

import java.util.List;

public class GameService {

    private final DataAccess dataAccess;

    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public GameResult createGame(String authToken, String gameName) throws Exception {
        if (authToken == null || authToken.isEmpty()) {
            return GameResult.failure("Error: unauthorized");
        }

        String username = dataAccess.getUsernameByToken(authToken);
        if (username == null) {
            return GameResult.failure("Error: unauthorized");
        }

        if (gameName == null || gameName.isEmpty()) {
            return GameResult.failure("Error: bad request");
        }


        GameData newGame = dataAccess.createGame(new GameData(1, gameName, username));

        if (newGame == null) {
            return GameResult.failure("Error: internal failure");
        }
//        System.out.println("Creating GameResult with ID: " + newGame.getGameId());
        return GameResult.success(newGame.getGameId());
    }

    public GameListResult listGames(String authToken) throws Exception {
        if (authToken == null || authToken.isEmpty()) {
            return GameListResult.failure("Error: unauthorized");
        }
        try {
            dataAccess.getUsernameByToken(authToken);

            List<GameData> games = dataAccess.listGames();
            return GameListResult.success(games);

        } catch (Exception e) {
            return GameListResult.failure("Error: " + e.getMessage());
        }
    }

    public JoinGameResult joinGame(String authToken, ChessGame.TeamColor playerColor, int gameID) throws Exception {
        // 1. Auth check
        if (authToken == null || authToken.isEmpty()) {
            return JoinGameResult.failure("Error: unauthorized");
        }

        String username = dataAccess.getUsernameByToken(authToken);
        if (username == null) {
            return JoinGameResult.failure("Error: unauthorized");
        }

        // 2. Input validation
        if (playerColor == null || gameID <= 0) {
            return JoinGameResult.failure("Error: bad request");
        }

        GameData game = dataAccess.getGame(gameID);
        if (game == null) {
            return JoinGameResult.failure("Error: bad request");
        }

        // 3. Check and assign color
        if (playerColor.equals("WHITE")) {
            if (game.getWhiteUsername() != null) {
                return JoinGameResult.failure("Error: already taken");
            }
            game.setWhiteUsername(username);
        } else if (playerColor.equals("BLACK")) {
            if (game.getBlackUsername() != null) {
                return JoinGameResult.failure("Error: already taken");
            }
            game.setBlackUsername(username);
        } else {
            return JoinGameResult.failure("Error: bad request");
        }

        // 4. Save changes
        dataAccess.updateGame(game);

        // 5. Success
        return JoinGameResult.success("Joined game successfully");
    }



}//end of class


