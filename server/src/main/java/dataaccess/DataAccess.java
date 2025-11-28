package dataaccess;

import chess.ChessGame;
import chess.model.data.GameData;
import chess.model.request.RegisterRequest;
import chess.model.request.SessionRequest;
import chess.model.result.RegisterResult;
import chess.model.result.SessionResult;

import java.util.List;

public interface DataAccess {
    RegisterResult registerUser(RegisterRequest request) throws Exception;

    void clear(); // for the /db endpoint

    SessionResult loginUser(SessionRequest request) throws Exception;

    boolean invalidateToken(String authToken) throws Exception;

    String getUsernameByToken(String authToken) throws Exception;

    GameData createGame(GameData game, String authToken) throws DataAccessException;

    List<GameData> listGames() throws DataAccessException;

    void updateGame(GameData game) throws DataAccessException;

    GameData getGameData(int gameID) throws DataAccessException;
    ChessGame getChessGame(int gameID) throws DataAccessException;

    void updateChessGame(int gameID, ChessGame game) throws DataAccessException;

}
