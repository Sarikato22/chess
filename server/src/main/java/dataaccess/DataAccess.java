package dataaccess;

import chess.model.request.RegisterRequest;
import chess.model.request.SessionRequest;
import chess.model.result.RegisterResult;
import chess.model.result.SessionResult;

public interface DataAccess {
    RegisterResult registerUser(RegisterRequest request) throws Exception;
    void clear(); // for the /db endpoint

    SessionResult loginUser(SessionRequest request) throws Exception;
    boolean invalidateToken(String authToken) throws Exception;
    String getUsernameByToken(String authToken) throws Exception;


}
