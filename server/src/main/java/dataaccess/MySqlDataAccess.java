package dataaccess;

import chess.ChessGame;
import chess.model.data.GameData;
import chess.model.request.RegisterRequest;
import chess.model.request.SessionRequest;
import chess.model.result.RegisterResult;
import chess.model.result.SessionResult;
import dataaccess.UnauthorizedException;
import com.google.gson.Gson;
import service.PasswordUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static dataaccess.DatabaseManager.getConnection;

public class MySqlDataAccess implements DataAccess{
    private int nextGameId = 1;
    private final Gson gson = new Gson();

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

            // Check if the user exists by querying the database
            String checkUserSql = "SELECT password FROM users WHERE username = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkUserSql)) {
                checkStmt.setString(1, username);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (!rs.next()) {
                        return SessionResult.failure("Invalid Request: Username not found");
                    }
                    String storedHash = rs.getString("password");

                    if (!PasswordUtil.verifyPassword(password, storedHash)) {
                        return SessionResult.failure("Invalid Request: Incorrect password");
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
        try (Connection conn = getConnection()) {
            if (conn == null) {
                throw new DataAccessException("Unable to get DB connection");
            }

            String deleteQuery = "DELETE FROM auth_tokens WHERE authToken = ?";
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery)) {
                deleteStmt.setString(1, authToken);
                int rowsDeleted = deleteStmt.executeUpdate();

                if (rowsDeleted == 0) {
                    throw new UnauthorizedException("Token not found in the database");
                }

                return true;
            }

        } catch (SQLException e) {
            throw new DataAccessException("Database error during token invalidation: " + e.getMessage(), e);
        }
    }

    @Override
    public String getUsernameByToken(String authToken) throws Exception {
        if (authToken == null || authToken.isEmpty()) {
        }

        try (Connection conn = getConnection()) {
            if (conn == null) {
                throw new DataAccessException("Unable to get DB connection");
            }

            String getQuery = "SELECT username FROM auth_tokens WHERE authToken = ?";
            try (PreparedStatement stmt = conn.prepareStatement(getQuery)) {
                stmt.setString(1, authToken);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("username");
                    } else {
                        return null;
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database error during token lookup: " + e.getMessage(), e);
        }
    }



    @Override
    public GameData createGame(GameData game, String authToken) throws DataAccessException {
        try (Connection conn = getConnection()) {
            if (conn == null) {
                throw new DataAccessException("Unable to get DB connection");
            }
            String creatorUsername = getUsernameByToken(authToken);
            if (creatorUsername == null) {
                throw new UnauthorizedException("Unauthorized: invalid auth token");
            }
            String insertQuery = "INSERT INTO games (whiteUsername, blackUsername, gameName) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setNull(1, Types.VARCHAR);
                stmt.setNull(2, Types.VARCHAR);
                stmt.setString(3, game.getGameName());
                stmt.executeUpdate();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        return new GameData(id, game.getGameName(), null, null);
                    } else {
                        throw new DataAccessException("Failed to retrieve auto-generated gameID");
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException("Database error during game creation: " + e.getMessage(), e);
        }
    }
    @Override
    public List<GameData> listGames() throws DataAccessException {
        List<GameData> result = new ArrayList<>();;
        try (Connection conn = getConnection()) {
            if (conn == null) {
                throw new DataAccessException("Unable to get DB connection");
            }

            String getQuery = "SELECT * FROM games";
            try (PreparedStatement stmt = conn.prepareStatement(getQuery)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        int gameID = rs.getInt("gameID");
                        String gameName = rs.getString("gameName");
                        String whiteUsername = rs.getString("whiteUsername");
                        String blackUsername = rs.getString("blackUsername");

                        GameData game = new GameData(gameID, gameName, whiteUsername, blackUsername);

                        result.add(game);
                    }
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Database error during token invalidation: " + e.getMessage(), e);
        }
        return result;
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        try (Connection conn = getConnection()) {
            if (conn == null) {
                throw new DataAccessException("Unable to get DB connection");
            }
            String getGameQuery = "SELECT * FROM games WHERE gameID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(getGameQuery)) {
                stmt.setInt(1, game.getGameId());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        throw new DataAccessException("Game with ID " + game.getGameId() + " not found.");
                    }
                }
            }

            String updateGameQuery = "UPDATE games SET whiteUsername = ?, blackUsername = ? WHERE gameID = ?";

            try (PreparedStatement stmt = conn.prepareStatement(updateGameQuery)) {
                // Set the appropriate username based on the color
                if (game.getWhiteUsername() != null) {
                    stmt.setString(1, game.getWhiteUsername());
                } else {
                    stmt.setNull(1, Types.VARCHAR);
                }

                if (game.getBlackUsername() != null) {
                    stmt.setString(2, game.getBlackUsername());
                } else {
                    stmt.setNull(2, Types.VARCHAR);
                }

                stmt.setInt(3, game.getGameId());
                int rowsUpdated = stmt.executeUpdate();
                if (rowsUpdated == 0) {
                    throw new DataAccessException("Failed to update the game with ID: " + game.getGameId());
                }
            }

        } catch (DataAccessException e) {
            throw new DataAccessException("Database error during game update: " + e.getMessage(), e);

        } catch (Exception e) {
            throw new RuntimeException("Unexpected error during game update: " + e.getMessage(), e);
        }
    }


    @Override
    public GameData getGameData(int gameID) throws DataAccessException {

        try (Connection conn = getConnection()) {
            if (conn == null) {
                throw new DataAccessException("Unable to get DB connection");
            }

            String getQuery = "SELECT * FROM games WHERE gameID = ?";
            try (PreparedStatement stmt = conn.prepareStatement(getQuery)) {
                stmt.setString(1, String.valueOf(gameID));
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        int gameIdentificator = rs.getInt("gameID");
                        String gameName = rs.getString("gameName");
                        String whiteUsername = rs.getString("whiteUsername");
                        String blackUsername = rs.getString("blackUsername");

                        GameData result = new GameData(gameIdentificator, gameName, whiteUsername, blackUsername);
                        return result;
                    } else {
                        return null; // Return null if no game is found
                    }
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Database error during token invalidation: " + e.getMessage(), e);
        }
        }
    @Override
    public ChessGame getChessGame(int gameID) throws DataAccessException {
        try (var conn = getConnection();
             var stmt = conn.prepareStatement(
                     "SELECT game_state FROM games WHERE gameID = ?")) {  // gameID

            stmt.setInt(1, gameID);
            try (var rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    throw new DataAccessException("Game not found");
                }

                String json = rs.getString("game_state");

                if (json == null) {
                    // no state stored yet → fresh game
                    return new ChessGame();
                }

                return gson.fromJson(json, ChessGame.class);
            }
        } catch (SQLException ex) {
            throw new DataAccessException(ex.getMessage(), ex);
        }
    }

    @Override
    public void updateChessGame(int gameID, ChessGame game) throws DataAccessException {
        String json = gson.toJson(game);

        try (var conn = getConnection();
             var stmt = conn.prepareStatement(
                     "UPDATE games SET game_state = ? WHERE gameID = ?")) {  // gameID

            stmt.setString(1, json);
            stmt.setInt(2, gameID);

            int rows = stmt.executeUpdate();
            if (rows == 0) {
                throw new DataAccessException("Game not found");
            }
        } catch (SQLException ex) {
            throw new DataAccessException(ex.getMessage(), ex);
        }
    }

}
