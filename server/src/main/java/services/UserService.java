package services;

import chess.model.request.RegisterRequest;
import chess.model.result.RegisterResult;
import dataaccess.DataAccess;

public class UserService {

    private final DataAccess dao;

    public UserService(DataAccess dao) {
        this.dao = dao;
    }

    public RegisterResult register(RegisterRequest request) {
        try {
            if (request == null) {
                return RegisterResult.failure(null, "Error: Request cannot be null");
            }

            if (request.getUsername() == null || request.getUsername().isEmpty()) {
                return RegisterResult.failure(null, "Error: Username is required");
            }

            if (request.getPassword() == null || request.getPassword().isEmpty()) {
                return RegisterResult.failure(request.getUsername(), "Error: Password is required");
            }

            if (request.getEmail() == null || request.getEmail().isEmpty()) {
                return RegisterResult.failure(request.getUsername(), "Error: Email is required");
            }

            return dao.registerUser(request);

        } catch (Exception e) {
            return RegisterResult.failure(request != null ? request.getUsername() : null,
                    "Error: " + e.getMessage());
        }
    }

}
