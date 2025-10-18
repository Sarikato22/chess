package chess.model.result;

public class RegisterResult {
    private boolean success;
    private String username;
    private String authToken;
    private String message;

    public RegisterResult() {}

    // Success constructor
    public RegisterResult(String username, String authToken) {
        this.success = true;
        this.username = username;
        this.authToken = authToken;
        this.message = null;
    }

    // Failure constructor
    public RegisterResult(String message) {
        this.success = false;
        this.username = null;
        this.authToken = null;
        this.message = message;
    }

    // Getters and setters
    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
