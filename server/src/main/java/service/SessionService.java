package service;

import chess.model.request.SessionRequest;
import chess.model.result.SessionResult;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;

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
    public SessionResult logout(String authToken) throws DataAccessException {
        try {
            // Attempt to invalidate the token
            boolean success = dao.invalidateToken(authToken);

            // If successful, return a success result
            if (success) {
                return SessionResult.success("Logged out successfully");
            } else {
                // If the token wasn't found or could not be invalidated, return failure
                return SessionResult.failure("Unauthorized");
            }
        } catch (DataAccessException e) {
            // Propagate the database error back to the handler
            throw new DataAccessException("Error during token invalidation: " + e.getMessage(), e);
        } catch (Exception e) {
            // Catch any other unexpected errors and rethrow them
            throw new RuntimeException("Unexpected error during logout: " + e.getMessage(), e);
        }
    }

    // Optional helper for tests
    public String getUserByToken(String token) throws Exception {
        return dao.getUsernameByToken(token);
    }
}
