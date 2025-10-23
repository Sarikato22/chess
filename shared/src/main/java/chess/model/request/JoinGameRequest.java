package chess.model.request;

import chess.ChessGame;

public class JoinGameRequest {
    private ChessGame.TeamColor playerColor;
    private int gameID;

    public JoinGameRequest() {}

    public JoinGameRequest(ChessGame.TeamColor playerColor, int gameID) {
        this.playerColor = playerColor;
        this.gameID = gameID;
    }

    public ChessGame.TeamColor getPlayerColor() { return playerColor; }
    public void setPlayerColor(ChessGame.TeamColor playerColor) { this.playerColor = playerColor; }

    public int getGameID() { return gameID; }
    public void setGameID(int gameID) { this.gameID = gameID; }
}

