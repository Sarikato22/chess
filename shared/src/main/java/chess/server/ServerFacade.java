package chess.server;

import chess.model.request.RegisterRequest;
import chess.model.result.RegisterResult;

import java.net.http.HttpClient;

public class ServerFacade {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;

    public ServerFacade(String url) {serverUrl = url; }

    public RegisterResult register(RegisterRequest req) throws ResponseException {


        return RegisterResult result;
    }

}

