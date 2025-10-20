package chess.model.result;

public class SessionResult {
    private boolean success;
    private String username;
    private String authToken;
    private String message;

    public SessionResult() {} // needed for JSON deserialization

    // Success constructor
    public SessionResult(String username, String authToken) {
        this.success = true;
        this.username = username;
        this.authToken = authToken;
        this.message = null;
    }

    // Failure factory
    public static SessionResult failure(String message) {
        SessionResult result = new SessionResult();
        result.success = false;
        result.username = null;
        result.authToken = null;
        result.message = message;
        return result;
    }

    // Optional success factory for messages
    public static SessionResult success(String message) {
        SessionResult result = new SessionResult();
        result.success = true;
        result.username = null;
        result.authToken = null;
        result.message = message;
        return result;
    }

    // Getters and setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getAuthToken() { return authToken; }
    public void setAuthToken(String authToken) { this.authToken = authToken; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
