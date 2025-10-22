package chess.model.result;

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

    // Static helpers (for readability)
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

    public Integer getGameID() {
        return gameID;
    }
}
