package chess.server;

import chess.model.data.GameData;
import chess.model.request.GameRequest;
import chess.model.request.RegisterRequest;
import chess.model.result.GameListResult;
import chess.model.result.GameResult;
import chess.model.result.RegisterResult;
import com.google.gson.Gson;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.*;
import java.net.http.*;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Map;

public class ServerFacade {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;

    public ServerFacade(String url) {serverUrl = url; }

    public RegisterResult register(RegisterRequest req) throws ResponseException {
        var request = buildRequest("POST", "/user", req, null);
        var response = sendRequest(request);
        var result = handleResponse(response, RegisterResult.class);
        if (!result.isSuccess()) {
            throw new ResponseException(ResponseException.Code.ClientError, result.getMessage());
        }
        return result;
    }

    public GameResult createGame(GameRequest req, Map<String, String> headers) throws ResponseException {
        var request = buildRequest("POST", "/game", req, headers);
        var response = sendRequest(request);
        var result = handleResponse(response, GameResult.class);
        if (result.getGameID() == null) {
            throw new ResponseException(ResponseException.Code.ClientError, result.getMessage());
        }
        return result;
    }
    public GameListResult listGames(Map<String, String> headers) throws ResponseException {
        var request = buildRequest("GET", "/game", null, headers);
        var response = sendRequest(request);
        GameListResult result = handleResponse(response, GameListResult.class);

        if (!result.isSuccess()) {
            throw new ResponseException(ResponseException.Code.ClientError, result.getMessage());
        }

        return result;
    }
    public void clear() throws ResponseException {
        var request = buildRequest("DELETE", "/db", null, null);
        sendRequest(request);
    }

    private HttpRequest buildRequest(String method, String path, Object body, Map<String, String> headers) {
        var builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .method(method, makeRequestBody(body));

        if (body != null) {
            builder.setHeader("Content-Type", "application/json");
        }

        if (headers != null) {
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    builder.setHeader(entry.getKey(), entry.getValue());
                }
            }

        }

        return builder.build();
    }

    private BodyPublisher makeRequestBody(Object request) {
        if (request != null) {
            return BodyPublishers.ofString(new Gson().toJson(request));
        } else {
            return BodyPublishers.noBody();
        }
    }
    private HttpResponse<String> sendRequest(HttpRequest request) throws ResponseException {
        try {
            return client.send(request, BodyHandlers.ofString());
        } catch (Exception ex) {
            throw new ResponseException(ResponseException.Code.ServerError, ex.getMessage());
        }
    }
    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseClass) throws ResponseException {

        var status = response.statusCode();
        System.out.println("HTTP status: " + response.statusCode());
        System.out.println("Response body: " + response.body());

        if (!isSuccessful(status)) {
            var body = response.body();
            if (body != null) {
                throw ResponseException.fromJson(body);
            }

            throw new ResponseException(ResponseException.fromHttpStatusCode(status), "other failure: " + status);
        }

        if (responseClass != null) {
            return new Gson().fromJson(response.body(), responseClass);
        }

        return null;
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }


}

