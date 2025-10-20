package dataaccess;

import chess.model.request.RegisterRequest;
import chess.model.result.RegisterResult;

public interface DataAccess {
    RegisterResult registerUser(RegisterRequest request) throws Exception;
    void clear(); // for the /db endpoint


}
