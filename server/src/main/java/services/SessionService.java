package services;

import chess.model.request.SessionRequest;
import chess.model.result.SessionResult;
import dataaccess.DataAccess;

public class SessionService {

    private final DataAccess dao;

    public SessionService(DataAccess dao) {
        this.dao = dao;
    }

    // Login user
    public SessionResult login(SessionRequest request) {
        if (request.getUsername() == null || request.getUsername().isEmpty()) {
            return SessionResult.failure("Username is required");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            return SessionResult.failure("Password is required");
        }

        try {
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
    public SessionResult logout(String authToken) {
        try {
            boolean success = dao.invalidateToken(authToken);
            if (success) {
                return SessionResult.success("Logged out successfully");
            } else {
                return SessionResult.failure("Unauthorized");
            }
        } catch (Exception e) {
            return SessionResult.failure("Error: " + e.getMessage());
        }
    }

    // Optional helper for tests
    public String getUserByToken(String token) throws Exception {
        return dao.getUsernameByToken(token);
    }
}
