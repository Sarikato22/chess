package server.handlers;

import chess.model.data.GameData;
import chess.model.request.GameRequest;
import chess.model.request.JoinGameRequest;
import chess.model.result.GameListResult;
import chess.model.result.GameResult;
import chess.model.result.JoinGameResult;
import com.google.gson.Gson;
import dataaccess.UnauthorizedException;
import io.javalin.http.Context;
import service.GameService;

import java.util.Map;

public class GameHandler {

    private final GameService gameService;
    private final Gson gson; // Gson serializer

    public GameHandler(GameService gameService) {
        this.gameService = gameService;
        this.gson = new Gson(); // initialize Gson
    }

    // GET /game
    public void listGames(Context ctx) {
        String authToken = ctx.header("authorization");
        if (authToken == null || authToken.isEmpty()) {
            ctx.status(401).result(gson.toJson(Map.of("message", "Error: bad request")));
            return;
        }

        try {
            GameListResult gameList = gameService.listGames(authToken);

            if (gameList.isSuccess()) {
                ctx.status(200).result(gson.toJson(Map.of("games", gameList.getGames())));
            } else {
                String message = gameList.getMessage() != null ? gameList.getMessage() : "Internal error";
                if (message.contains("unauthorized")) {
                    ctx.status(401).result(gson.toJson(Map.of("message", "Error: unauthorized")));
                } else {
                    ctx.status(500).result(gson.toJson(Map.of("message", "Error: " + message)));
                }
            }
        } catch (Exception e) {
            ctx.status(500).result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        }
    }


    public void createGame(Context ctx) {
        String authToken = ctx.header("authorization");
            GameRequest request;

            try {
                request = gson.fromJson(ctx.body(), GameRequest.class);
            } catch (Exception e) {
                ctx.status(400).result(gson.toJson(Map.of("message", "Error: bad request")));
                return;
            }

            try {
                GameResult result = gameService.createGame(authToken, request.getGameName());

                if (result.isSuccess()) {
                    ctx.status(200).result(gson.toJson(Map.of(
                            "success", result.isSuccess(),
                            "gameID", result.getGameID()
                    )));
                } else {
                    String message = result.getMessage() != null ? result.getMessage() : "Internal error";

                    if (message.contains("Unauthorized")) {
                        ctx.status(401).result(gson.toJson(Map.of("message", "Error: unauthorized")));
                    } else if (message.contains("bad request")) {
                        ctx.status(400).result(gson.toJson(Map.of("message", "Error: bad request")));
                    } else {
                        ctx.status(500).result(gson.toJson(Map.of("message", "Error: " + message)));
                    }
                }
            } catch (UnauthorizedException e) {
                ctx.status(401).result(gson.toJson(Map.of("message", e.getMessage())));
            } catch (Exception e) {
                ctx.status(500).result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
            }
        }


    // PUT /game
    public void joinGame(Context ctx) {
        String authToken = ctx.header("authorization");
        JoinGameRequest req;

        try {
            req = gson.fromJson(ctx.body(), JoinGameRequest.class);
        } catch (Exception e) {
            ctx.status(400).result(gson.toJson(Map.of("message", "Error: bad request")));
            return;
        }

        try {
            JoinGameResult result = gameService.joinGame(authToken, req.getPlayerColor(), req.getGameID());

            if (result.isSuccess()) {
                ctx.status(200).result(""); // empty response
            } else {
                String message = result.getMessage() != null ? result.getMessage() : "";
                if (message.contains("unauthorized")) {
                    ctx.status(401).result(gson.toJson(Map.of("message", "Error: unauthorized")));
                } else if (message.contains("already taken")) {
                    ctx.status(403).result(gson.toJson(Map.of("message", "Error: already taken")));
                } else if (message.contains("bad request")) {
                    ctx.status(400).result(gson.toJson(Map.of("message", "Error: bad request")));
                } else {
                    ctx.status(500).result(gson.toJson(Map.of("message", "Error: " + message)));
                }
            }
        } catch (Exception e) {
            ctx.status(500).result(gson.toJson(Map.of("message", "Error: " + e.getMessage())));
        }
    }
}
