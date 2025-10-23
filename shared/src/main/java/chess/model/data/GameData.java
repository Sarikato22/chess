package chess.model.data;

public class GameData {
    private final int gameId;
    private final String gameName;
    private String whiteUsername;
    private String blackUsername;


    public GameData(int gameId, String gameName, String whiteUsername, String blackUsername) {
        this.gameId = gameId;
        this.gameName = gameName;
        this.whiteUsername = whiteUsername;
        this.blackUsername = blackUsername;

    }

    public int getGameId() {
        return gameId;
    }

    public String getGameName() {
        return gameName;
    }

    public String getWhiteUsername() { return whiteUsername; }
    public void setWhiteUsername(String whiteUsername) { this.whiteUsername = whiteUsername; }

    public String getBlackUsername() { return blackUsername; }
    public void setBlackUsername(String blackUsername) { this.blackUsername = blackUsername; }

}
