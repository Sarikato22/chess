package dataaccess;

import chess.model.data.GameData;
import chess.model.request.RegisterRequest;
import chess.model.request.SessionRequest;
import chess.model.result.RegisterResult;
import chess.model.result.SessionResult;
import com.google.gson.Gson;
import service.PasswordUtil;

import java.sql.*;
import java.util.List;
import java.util.UUID;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;
public class MySqlDataAccess implements DataAccess{

    public MySqlDataAccess() throws Exception {
        configureDatabase();
    }

    private static String[] createStatements= {
            """
    CREATE TABLE IF NOT EXISTS users (
        username VARCHAR(50) PRIMARY KEY,
        password VARCHAR(255) NOT NULL,
        email VARCHAR(100) NOT NULL
    );
    """,

            """
    CREATE TABLE IF NOT EXISTS auth_tokens (
        authToken CHAR(36) PRIMARY KEY,
        username VARCHAR(50) NOT NULL,
        FOREIGN KEY (username) REFERENCES users(username) ON DELETE CASCADE
    );
    """,

            """
    CREATE TABLE IF NOT EXISTS games (
        gameID INT AUTO_INCREMENT PRIMARY KEY,
        whiteUsername VARCHAR(50),
        blackUsername VARCHAR(50),
        gameName VARCHAR(100) NOT NULL,
        game TEXT,
        FOREIGN KEY (whiteUsername) REFERENCES users(username) ON DELETE SET NULL,
        FOREIGN KEY (blackUsername) REFERENCES users(username) ON DELETE SET NULL
    );
    """
    };
    private void configureDatabase() throws Exception {
        DatabaseManager.createDatabase();
        try (Connection conn = DatabaseManager.getConnection()) {
            for (String statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException(String.format("Unable to configure database: %s", ex.getMessage()), ex);
        }
    }


    @Override
    public void clear() {

    }

    @Override
    public SessionResult loginUser(SessionRequest request) throws Exception {
        return null;
    }

    @Override
    public boolean invalidateToken(String authToken) throws Exception {
        return false;
    }

    @Override
    public String getUsernameByToken(String authToken) throws Exception {
        return null;
    }

    @Override
    public GameData createGame(GameData game) throws DataAccessException {
        return null;
    }

    @Override
    public List<GameData> listGames() throws DataAccessException {
        return null;
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {

    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return null;
    }
}
