package chess.model.result;

import chess.model.data.GameData;

import java.util.List;

public class GameListResult {
    private boolean success;
    private String message;
    private List<GameData> games;


    public GameListResult(boolean success, String message, List<GameData> games) {
        this.success = success;
        this.message = message;
        this.games = games;
    }

    public static GameListResult success(List<GameData> games) {
        return new GameListResult(true, null, games);
    }

    public static GameListResult failure(String message) {
        return new GameListResult(false, message, null);
    }
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public List<GameData> getGames() {
        return games;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setGames(List<GameData> games) {
        this.games = games;
    }


}

