package chess.model.result;

public class RegisterResult {
    private String username;
    private String authToken;
    private String message;

    public RegisterResult() {}

    public RegisterResult(String username, String authToken, String message) {
        this.username = username;
        this.authToken = authToken;
        this.message = message;
    }

    public String getUsername() { return username; }
    public String getAuthToken() { return authToken; }
    public String getMessage() { return message; }

    public void setUsername(String username) { this.username = username; }
    public void setAuthToken(String authToken) { this.authToken = authToken; }
    public void setMessage(String message) { this.message = message; }
}