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

import static dataaccess.DatabaseManager.getConnection;
import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;
public class MySqlDataAccess implements DataAccess{

    public MySqlDataAccess() {
        try {
            configureDatabase();
        } catch (DataAccessException ex) {
            throw new RuntimeException("Failed to initialize MySQL DataAccess", ex);
        }
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
    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (Connection conn = getConnection()) {
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
    public RegisterResult registerUser(RegisterRequest request) throws Exception {
        String username = request.getUsername();
        String password = request.getPassword();
        String email = request.getEmail();


        try (Connection conn = getConnection()) {
            String checkUserSql = "SELECT username FROM users WHERE username = ?";
            try (PreparedStatement checkStatement = conn.prepareStatement(checkUserSql)) {
                checkStatement.setString(1, username);
                ResultSet result = checkStatement.executeQuery();
                if (result.next()) {
                    System.out.println("Duplicate username found: " + result.getString(1));
                    return RegisterResult.failure(username, "Error: already taken");
                }
            }

            String hashedPassword = PasswordUtil.hashPassword(password);
            String insertUserSql = "INSERT INTO users (username, password, email) VALUES (?, ?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertUserSql)) {
                insertStmt.setString(1, username);
                insertStmt.setString(2, hashedPassword);
                insertStmt.setString(3, email);
                insertStmt.executeUpdate();
            }
            String token = UUID.randomUUID().toString();
            String insertAuthSql = "INSERT INTO auth_tokens (authToken, username) VALUES (?, ?)";
            try (PreparedStatement authStmt = conn.prepareStatement(insertAuthSql)) {
                authStmt.setString(1, token);
                authStmt.setString(2, username);
                authStmt.executeUpdate();
            }

            return new RegisterResult(username, token);

        } catch (SQLException e) {
            throw new DataAccessException("Unable to register user: " + e.getMessage(), e);
        }
    }

    @Override
    public void clear() {
        String[] tables = { "auth_tokens", "games", "users" }; // child → parent order
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 0;");
            for (String table : tables) {
                stmt.executeUpdate("TRUNCATE TABLE " + table + ";");
            }
            stmt.executeUpdate("SET FOREIGN_KEY_CHECKS = 1;");
        } catch (DataAccessException | SQLException ex) {
            throw new RuntimeException("Failed to initialize MySQL DataAccess", ex);
        }
    }

    @Override
    public SessionResult loginUser(SessionRequest request) throws Exception {
        String username = request.getUsername();
        String password = request.getPassword();

        try (Connection conn = getConnection()) {
            if (conn == null) {
                throw new DataAccessException("Unable to get DB connection");
            }

            String checkUserSql = "SELECT password FROM users WHERE username = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkUserSql)) {
                checkStmt.setString(1, username);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (!rs.next()) {
                        return SessionResult.failure("Error: Username not found");
                    }
                    String storedHash = rs.getString("password");

                    if (!PasswordUtil.verifyPassword(password, storedHash)) {
                        return SessionResult.failure("Error: Incorrect password");
                    }
                }
            }

            String token = UUID.randomUUID().toString();
            System.out.println("Generated token for " + username + ": " + token);

            String insertAuthSql = "INSERT INTO auth_tokens (authToken, username) VALUES (?, ?)";
            try (PreparedStatement authStmt = conn.prepareStatement(insertAuthSql)) {
                authStmt.setString(1, token);
                authStmt.setString(2, username);
                authStmt.executeUpdate();
            }

            return new SessionResult(username, token);

        } catch (SQLException e) {
            throw new DataAccessException("Database error during login: " + e.getMessage(), e);
        }

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
