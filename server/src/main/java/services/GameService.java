package services;

import chess.model.result.GameListResult;
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
        System.out.println("Creating GameResult with ID: " + newGame.getGameId());
        return GameResult.success(newGame.getGameId());
    }

    public GameListResult listGames(String authToken) throws Exception {
            try {
                dataAccess.getUsernameByToken(authToken);
                List<GameData> games = dataAccess.listGames();
                return GameListResult.success(games);

            } catch (Exception e) {
                return GameListResult.failure("Error: " + e.getMessage());
            }
    }


}

