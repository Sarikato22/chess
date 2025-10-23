package handlers;

import chess.model.request.JoinGameRequest;
import chess.model.result.GameListResult;
import chess.model.result.JoinGameResult;
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
    public void listGames(Context ctx) {
        String authToken = ctx.header("authorization");
        if (authToken == null || authToken.isEmpty()) {
            ctx.status(400).json(Map.of("message", "Error: bad request"));
        }
        try {
           GameListResult gameList =  gameService.listGames(authToken);

            if (gameList.isSuccess()){
                ctx.status(200).json(Map.of("games", gameList.getGames()));
            } else {
                String message = gameList.getMessage() != null ? gameList.getMessage() : "Internal error";

                if (message.contains ("unauthorized")) {
                    ctx.status(401).json(Map.of("message", "Error: unauthorized"));
                } else {
                    ctx.status(500).json(Map.of("message", "Error: " + message));
                }
            }
        } catch (Exception e ) {
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
