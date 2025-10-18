package handlers;

import io.javalin.http.Context;
import java.util.Map;
import java.util.List;

public class GameHandler {

    // GET /game
    public static void listGames(Context ctx) {
        List<Map<String, Object>> fakeGames = List.of(
                Map.of("id", 1, "name", "Fake Game 1"),
                Map.of("id", 2, "name", "Fake Game 2")
        );
        ctx.json(Map.of("games", fakeGames));
    }

    // POST /game
    public static void createGame(Context ctx) {
        Map<String, Object> response = Map.of(
                "gameId", 123,
                "message", "Game created successfully"
        );
        ctx.json(response);
    }

    // PUT /game
    public static void joinGame(Context ctx) {
        Map<String, Object> response = Map.of(
                "message", "Joined game successfully"
        );
        ctx.json(response);
    }
}
