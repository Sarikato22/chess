package chess.model.data;

public class GameData {
    private final int gameId;
    private final String gameName;
    private final String creatorUsername;
    private String whiteUsername;
    private String blackUsername;

    public GameData(int gameId, String gameName, String creatorUsername) {
        this.gameId = gameId;
        this.gameName = gameName;
        this.creatorUsername = creatorUsername;
    }

    public int getGameId() {
        return gameId;
    }

    public String getGameName() {
        return gameName;
    }

    public String getCreatorUsername() {
        return creatorUsername;
    }
    public String getWhiteUsername() { return whiteUsername; }
    public void setWhiteUsername(String whiteUsername) { this.whiteUsername = whiteUsername; }

    public String getBlackUsername() { return blackUsername; }
    public void setBlackUsername(String blackUsername) { this.blackUsername = blackUsername; }

}
