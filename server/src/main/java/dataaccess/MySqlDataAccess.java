package dataaccess;

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

                    // If user found, check password
                    String storedHash = rs.getString("password");

                    if (!PasswordUtil.verifyPassword(password, storedHash)) {
                        return SessionResult.failure("Invalid Request: Incorrect password");
                    }
                }
            }

            // If the username exists and password is correct, generate an auth token
            String token = UUID.randomUUID().toString();
            System.out.println("Generated token for " + username + ": " + token);

            // Insert token into the database
            String insertAuthSql = "INSERT INTO auth_tokens (authToken, username) VALUES (?, ?)";
            try (PreparedStatement authStmt = conn.prepareStatement(insertAuthSql)) {
                authStmt.setString(1, token);
                authStmt.setString(2, username);
                authStmt.executeUpdate();
            }

            // Return the session result with the username and token
            return new SessionResult(username, token);

        } catch (SQLException e) {
            // If any SQL error happens, throw a DataAccessException, which will be handled by the service layer
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
                    // If no rows are deleted, it means the token was not found
                    throw new DataAccessException("Token not found in the database");
                }

                return true; // Successful invalidation
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
                        // Log if the token does not exist in the database
                        System.out.println("No username found for token: " + authToken);
                        return null; // Explicitly return null if not found
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Database error during token lookup: " + e.getMessage(), e);
        }
    }



    @Override
    public GameData createGame(GameData game, String authToken) throws DataAccessException {
        int id = nextGameId++;
        GameData newGame = new GameData(id, game.getGameName(), game.getWhiteUsername(), game.getBlackUsername());

        try (Connection conn = getConnection()) {
            if (conn == null) {
                throw new DataAccessException("Unable to get DB connection");
            }

            String whiteUsername = getUsernameByToken(authToken);
            if (whiteUsername == null) {
                throw new UnauthorizedException("Unauthorized: White player is not authorized");
            }

            String blackUsername = getUsernameByToken(authToken);
            if (blackUsername == null) {
                throw new UnauthorizedException("Unauthorized: Black player is not authorized");
            }

            String insertQuery = "INSERT INTO games (gameID, whiteUsername, blackUsername, gameName) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
                stmt.setString(1, String.valueOf(id));
                stmt.setString(2, newGame.getWhiteUsername());
                stmt.setString(3, newGame.getBlackUsername());
                stmt.setString(4, newGame.getGameName());
                stmt.executeUpdate();
            }

            return newGame;

        } catch (SQLException e) {
            throw new DataAccessException("Database error during game creation: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException(e);
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
            try (PreparedStatement Stmt = conn.prepareStatement(getQuery)) {
                try (ResultSet rs = Stmt.executeQuery()) {
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

            // Prepare the query to update the game based on which color the player is joining as
            String updateGameQuery = "UPDATE games SET whiteUsername = ?, blackUsername = ? WHERE gameID = ?";

            try (PreparedStatement stmt = conn.prepareStatement(updateGameQuery)) {
                // Set the appropriate username based on the color
                if (game.getWhiteUsername() != null) {
                    stmt.setString(1, game.getWhiteUsername());
                } else {
                    stmt.setNull(1, Types.VARCHAR);  // If whiteUsername is null, set it to SQL null
                }

                if (game.getBlackUsername() != null) {
                    stmt.setString(2, game.getBlackUsername());
                } else {
                    stmt.setNull(2, Types.VARCHAR);  // If blackUsername is null, set it to SQL null
                }

                // Set the gameID to ensure the correct game is updated
                stmt.setInt(3, game.getGameId());

                // Execute the update
                int rowsUpdated = stmt.executeUpdate();
                if (rowsUpdated == 0) {
                    throw new DataAccessException("Failed to update the game with ID: " + game.getGameId());
                }
            }

        } catch (SQLException e) {
            throw new DataAccessException("Database error during game update: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Unexpected error during game update: " + e.getMessage(), e);
        }
    }


    @Override
    public GameData getGame(int gameID) throws DataAccessException {

        try (Connection conn = getConnection()) {
            if (conn == null) {
                throw new DataAccessException("Unable to get DB connection");
            }

            String getQuery = "SELECT * FROM games WHERE gameID = ?";
            try (PreparedStatement Stmt = conn.prepareStatement(getQuery)) {
                Stmt.setString(1, String.valueOf(gameID));
                try (ResultSet rs = Stmt.executeQuery()) {
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
    }
