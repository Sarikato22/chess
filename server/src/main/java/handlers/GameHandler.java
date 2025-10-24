package handlers;

import chess.model.data.GameData;
import chess.model.request.GameRequest;
import chess.model.request.JoinGameRequest;
import chess.model.result.GameListResult;
import chess.model.result.GameResult;
import chess.model.result.JoinGameResult;
import com.google.gson.Gson;
import io.javalin.http.Context;
import service.GameService;

import java.util.List;
import java.util.Map;

public class GameHandler {
    private final GameService gameService;

    public GameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    // GET /game

    public void listGames(Context ctx) {
        String authToken = ctx.header("authorization");
        if (authToken == null || authToken.isEmpty()) {
            ctx.status(401).json(Map.of("message", "Error: bad request"));
            return;
        }
        try {
            GameListResult gameList = gameService.listGames(authToken);

            // Peek at JSON
//            Gson gson = new Gson();
//            String json = gson.toJson(Map.of("games",gameList.getGames()));  // serialize the games list

            if (gameList.isSuccess()) {
                Gson gson = new Gson();
                String json = gson.toJson(Map.of("games",gameList.getGames()));  // serialize the games list
                ctx.status(200).json(json);
            } else {
                String message = gameList.getMessage() != null ? gameList.getMessage() : "Internal error";
                if (message.contains("unauthorized")) {
                    ctx.status(401).json(Map.of("message", "Error: unauthorized"));
                } else {
                    ctx.status(500).json(Map.of("message", "Error: " + message));
                }
            }
        } catch (Exception e) {
            ctx.status(500).json(Map.of("message", "Error: " + e.getMessage()));
        }
    }


    // POST /game
    public void createGame(Context ctx) {
        String authToken = ctx.header("authorization");
        GameRequest request;

        try {
            request = ctx.bodyAsClass(GameRequest.class);
        } catch (Exception e) {
            ctx.status(400).json(Map.of("message", "Error: bad request"));
            return;
        }

        try {
            GameResult result = gameService.createGame(authToken, request.getGameName());

            if (result.isSuccess()) {
                // Success
                ctx.status(200).json(Map.of("gameID", result.getGameID()));

            } else {
                // Failure
                String message = result.getMessage() != null ? result.getMessage() : "Internal error";

                if (message.contains("unauthorized")) {
                    ctx.status(401).json(Map.of("message", "Error: unauthorized"));
                } else if (message.contains("bad request")) {
                    ctx.status(400).json(Map.of("message", "Error: bad request"));
                } else {
                    ctx.status(500).json(Map.of("message", "Error: " + message));
                }
            }
        } catch (Exception e) {
            ctx.status(500).json(Map.of("message", "Error: " + e.getMessage()));
        }
    }

    // PUT /game
    public void joinGame(Context ctx) {
        try {
            String authToken = ctx.header("authorization");

            JoinGameRequest req;
            try {
                req = ctx.bodyAsClass(JoinGameRequest.class);
            } catch (Exception e) {
                ctx.status(400).json(Map.of("message", "Error: bad request"));
                return;
            }

            JoinGameResult result = gameService.joinGame(authToken, req.getPlayerColor(), req.getGameID());

            if (result.isSuccess()) {
                ctx.status(200).result("");
            } else {
                String message = result.getMessage() == null ? "" : result.getMessage();
                if (message.contains("unauthorized")) {
                    ctx.status(401).json(Map.of("message", "Error: unauthorized"));
                } else if (message.contains("already taken")) {
                    ctx.status(403).json(Map.of("message", "Error: already taken"));
                } else if (message.contains("bad request")) {
                    ctx.status(400).json(Map.of("message", "Error: bad request"));
                } else {
                    ctx.status(500).json(Map.of("message", "Error: " + message));
                }
            }

        } catch (Exception e) {
            ctx.status(500).json(Map.of("message", "Error: " + e.getMessage()));
        }
    }


}
