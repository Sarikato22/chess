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
            return dao.registerUser(request);
        } catch (Exception e) {
            return new RegisterResult(request.getUsername(), "Error: " + e.getMessage());
        }
    }
}
