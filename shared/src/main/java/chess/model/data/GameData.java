package chess.model.data;

public class GameData {
    private final int gameId;
    private final String gameName;
    private final String creatorUsername;

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
}
