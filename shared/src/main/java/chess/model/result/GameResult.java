package chess.model.result;

import com.google.gson.annotations.SerializedName;

public class GameResult {
    private boolean success;
    private String message;
    private Integer gameID;  // nullable for error cases

    // Constructors
    public GameResult(boolean success, String message, Integer gameID) {
        this.success = success;
        this.message = message;
        this.gameID = gameID;
    }

    public static GameResult success(int gameID) {
        return new GameResult(true, null, gameID);
    }

    public static GameResult failure(String message) {
        return new GameResult(false, message, null);
    }

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    @SerializedName("gameID")
    public Integer getGameID() {
        return gameID;
    }
}
