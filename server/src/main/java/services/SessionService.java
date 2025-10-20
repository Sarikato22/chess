package services;

import chess.model.request.SessionRequest;
import chess.model.result.SessionResult;
import dataaccess.DataAccess;

public class SessionService {

    private static DataAccess dao = null;

    public SessionService(DataAccess dao) {
        this.dao = dao;
    }

    // Login user
    public static SessionResult login(SessionRequest request) {
        // Validate input
        if (request.getUsername() == null || request.getUsername().isEmpty()) {
            return SessionResult.failure("Username is required");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            return SessionResult.failure("Password is required");
        }

        try {
            // Attempt to authenticate via DAO
            SessionResult result = dao.loginUser(request);

            if (result == null) {
                return SessionResult.failure("Invalid username or password");
            }
            return result;
        } catch (Exception e) {
            return SessionResult.failure("Error: " + e.getMessage());
        }
    }

    // Logout user
    public static SessionResult logout(String authToken) {
        try {
            boolean success = dao.invalidateToken(authToken);
            if (success) {
                return SessionResult.success("Logged out successfully");
            } else {
                return SessionResult.failure("Invalid auth token");
            }
        } catch (Exception e) {
            return SessionResult.failure("Error: " + e.getMessage());
        }
    }

    // Optional: helper for tests
    public static String getUserByToken(String token) throws Exception {
        return dao.getUsernameByToken(token);
    }
}
