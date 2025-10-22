package dataaccess;

import chess.model.data.GameData;
import chess.model.request.RegisterRequest;
import chess.model.request.SessionRequest;
import chess.model.result.GameListResult;
import chess.model.result.GameResult;
import chess.model.result.RegisterResult;
import chess.model.result.SessionResult;

import java.util.List;

public interface DataAccess {
    RegisterResult registerUser(RegisterRequest request) throws Exception;
    void clear(); // for the /db endpoint

    SessionResult loginUser(SessionRequest request) throws Exception;
    boolean invalidateToken(String authToken) throws Exception;
    String getUsernameByToken(String authToken) throws Exception;

    GameData createGame(GameData game) throws DataAccessException;

    List<GameData> listGames() throws DataAccessException;
    void joinGame(int gameID, String username, chess.ChessGame.TeamColor color) throws DataAccessException;
}
