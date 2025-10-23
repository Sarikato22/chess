package chess.model.request;

public class GameRequest {
    private String gameName;

    // Required for JSON deserialization
    public GameRequest() {
    }

    public GameRequest(String gameName) {
        this.gameName = gameName;
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }
}
