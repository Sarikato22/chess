package services;

import dataaccess.DataAccess;
import chess.model.data.GameData;
import chess.model.request.GameRequest;
import chess.model.result.GameResult;

public class GameService {

    private final DataAccess dataAccess;

    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public GameResult createGame(String authToken, String gameName) {
        try {
            // 1. Validate authorization
            if (authToken == null || authToken.isEmpty()) {
                return GameResult.failure("Error: unauthorized");
            }

            String username = dataAccess.getUsernameByToken(authToken);
            if (username == null) {
                return GameResult.failure("Error: unauthorized");
            }

            // 2. Validate request body
            if (gameName == null || gameName.isEmpty()) {
                return GameResult.failure("Error: bad request");
            }

            // 3. Create a new GameData object
            GameData game = new GameData(0, gameName, username);

            // 4. Store it via DataAccess
            GameData createdGame = dataAccess.createGame(game);

            // 5. Return a successful result
            return GameResult.success(createdGame.getGameId());

        } catch (Exception e) {
            return GameResult.failure("Error: " + e.getMessage());
        }
    }
}

