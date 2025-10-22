package chess.model.request;

import chess.ChessGame;

public class JoinGameRequest {
    private ChessGame.TeamColor playerColor;
    private int gameID;

    public ChessGame.TeamColor getPlayerColor() { return playerColor; }
    public int getGameID() { return gameID; }
}
