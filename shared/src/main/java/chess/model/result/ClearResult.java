package chess.model.result;

public class ClearResult {
    private boolean success;
    private String message;

    public ClearResult() {}

    public ClearResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
