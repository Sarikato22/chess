package chess.model.result;

public class JoinGameResult {
    private final boolean success;
    private final String message;

    public JoinGameResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }

    public static JoinGameResult success(String message) {
        return new JoinGameResult(true, message);
    }

    public static JoinGameResult failure(String message) {
        return new JoinGameResult(false, message);
    }
}
