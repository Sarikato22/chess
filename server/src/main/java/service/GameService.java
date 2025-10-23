package service;

import chess.ChessGame;
import chess.model.result.GameListResult;
import chess.model.result.JoinGameResult;
import dataaccess.DataAccess;
import chess.model.data.GameData;
import chess.model.result.GameResult;

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

        GameData newGame = dataAccess.createGame(new GameData(1, gameName, null, null));

        if (newGame == null) {
            return GameResult.failure("Error: internal failure");
        }

        return GameResult.success(newGame.getGameId());
    }


    public GameListResult listGames(String authToken) throws Exception {
        if (authToken == null || authToken.isEmpty()) {
            return GameListResult.failure("Error: unauthorized");
        }

        String username = dataAccess.getUsernameByToken(authToken);
        if (username == null) {
            return GameListResult.failure("Error: unauthorized");
        }

        List<GameData> allGames = dataAccess.listGames();

        return GameListResult.success(allGames);
    }



    public JoinGameResult joinGame(String authToken, ChessGame.TeamColor playerColor, int gameID) throws Exception {

        if (authToken == null || authToken.isEmpty()) {
            return JoinGameResult.failure("Error: unauthorized");
        }

        String username = dataAccess.getUsernameByToken(authToken);
        if (username == null) {
            return JoinGameResult.failure("Error: unauthorized");
        }

        if (playerColor != ChessGame.TeamColor.WHITE && playerColor != ChessGame.TeamColor.BLACK || gameID <= 0) {
            return JoinGameResult.failure("Error: bad request");
        }

        GameData game = dataAccess.getGame(gameID);
        if (game == null) {
            return JoinGameResult.failure("Error: bad request");
        }

        if (playerColor == ChessGame.TeamColor.WHITE) {
            if (game.getWhiteUsername() != null) {
                return JoinGameResult.failure("Error: already taken");
            }
            game.setWhiteUsername(username);
        } else {
            if (game.getBlackUsername() != null) {
                return JoinGameResult.failure("Error: already taken");
            }
            game.setBlackUsername(username);
        }
        dataAccess.updateGame(game);

        return JoinGameResult.success("Joined game successfully");
    }



}//end of class


