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
            RegisterResult result = dao.registerUser(request);

            return result;
        } catch (Exception e) {
            // Return failure with message
            return new RegisterResult("Error: " + e.getMessage());
        }
    }
}

