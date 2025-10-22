package handlers;

import io.javalin.http.Context;
import java.util.Map;
import java.util.List;
import chess.model.request.GameRequest;
import chess.model.result.GameResult;
import services.GameService;

public class GameHandler {
    private final GameService gameService;

    public GameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    // GET /game
    public static void listGames(Context ctx) {
        List<Map<String, Object>> fakeGames = List.of(
                Map.of("id", 1, "name", "Fake Game 1"),
                Map.of("id", 2, "name", "Fake Game 2")
        );
        ctx.json(Map.of("games", fakeGames));
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
    public static void joinGame(Context ctx) {
        Map<String, Object> response = Map.of(
                "message", "Joined game successfully"
        );
        ctx.json(response);
    }
}
