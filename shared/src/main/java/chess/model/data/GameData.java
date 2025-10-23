package chess.model.data;

import com.google.gson.annotations.SerializedName;

public class GameData {
    @SerializedName("gameID")
    private final int gameID;
    private final String gameName;
    private String whiteUsername;
    private String blackUsername;

    public GameData(int gameID, String gameName, String whiteUsername, String blackUsername) {
        this.gameID = gameID;
        this.gameName = gameName;
        this.whiteUsername = whiteUsername;
        this.blackUsername = blackUsername;

    }

    @SerializedName("gameID")
    public int getGameId() {
        return gameID;
    }

    public String getGameName() {
        return gameName;
    }

    public String getWhiteUsername() {
        return whiteUsername;
    }

    public void setWhiteUsername(String whiteUsername) {
        this.whiteUsername = whiteUsername;
    }

    public String getBlackUsername() {
        return blackUsername;
    }

    public void setBlackUsername(String blackUsername) {
        this.blackUsername = blackUsername;
    }

}
