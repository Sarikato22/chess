package chess.model.result;

public class RegisterResult {
    private boolean success;
    private String username;
    private String authToken;
    private String message;

    public RegisterResult() {
    } // required for JSON parsing

    // Success constructor
    public RegisterResult(String username, String authToken) {
        this.success = true;
        this.username = username;
        this.authToken = authToken;
        this.message = null;
    }

    // Failure factory method
    public static RegisterResult failure(String username, String message) {
        RegisterResult result = new RegisterResult();
        result.success = false;
        result.username = username;
        result.authToken = null;
        result.message = message;
        return result;
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
