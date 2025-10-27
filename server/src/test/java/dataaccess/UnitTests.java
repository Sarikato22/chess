package dataaccess;

import dataaccess.DatabaseManager;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UnitTests {

    private static String[] createStatements = {
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

    @BeforeAll
    static void setupDatabase() throws Exception {
        DatabaseManager.createDatabase();
        try (Connection conn = DatabaseManager.getConnection()) {
            for (String stmt : createStatements) {
                conn.createStatement().executeUpdate(stmt);
            }
        }
    }

    @Test
    @DisplayName("Check if 'users' table exists")
    void testUsersTableExists() throws Exception {
        assertTrue(tableExists("users"), "Table 'users' should exist");
    }

    @Test
    @DisplayName("Check if 'auth_tokens' table exists")
    void testAuthTokensTableExists() throws Exception {
        assertTrue(tableExists("auth_tokens"), "Table 'auth_tokens' should exist");
    }

    @Test
    @DisplayName("Check if 'games' table exists")
    void testGamesTableExists() throws Exception {
        assertTrue(tableExists("games"), "Table 'games' should exist");
    }

    private boolean tableExists(String tableName) throws Exception {
        try (Connection conn = DatabaseManager.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet rs = metaData.getTables(null, null, tableName, new String[]{"TABLE"})) {
                return rs.next();
            }
        }
    }
}
