package chess.model.result;

public class SessionResult {

    private final boolean success;
    private final String username;
    private final String authToken;
    private final String message;

    // Constructor for success
    public SessionResult(String username, String authToken) {
        this.success = true;
        this.username = username;
        this.authToken = authToken;
        this.message = null;
    }

    // Constructor for failure
    private SessionResult(String message) {
        this.success = false;
        this.username = null;
        this.authToken = null;
        this.message = message;
    }

    // Internal constructor used by factory success
    private SessionResult(boolean success, String username, String authToken, String message) {
        this.success = success;
        this.username = username;
        this.authToken = authToken;
        this.message = message;
    }

    // Factory method for failure
    public static SessionResult failure(String message) {
        return new SessionResult(message);
    }

    // Factory method for success (optional)
    public static SessionResult success(String message) {
        return new SessionResult(true, null, null, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getUsername() {
        return username;
    }

    public String getAuthToken() {
        return authToken;
    }

    public String getMessage() {
        return message;
    }
}
