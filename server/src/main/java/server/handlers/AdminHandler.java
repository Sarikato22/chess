package server.handlers;

import chess.model.result.ClearResult;
import com.google.gson.Gson;
import io.javalin.http.Context;
import service.ClearService;
import service.WebSocketGameService;

import java.util.Map;

public class AdminHandler {

    private final ClearService clearService;
    private final Gson gson;
    private final WebSocketGameService wsGameService;
    public AdminHandler(ClearService clearService, WebSocketGameService wsGameService) {
        this.clearService = clearService;
        this.gson = new Gson();
        this.wsGameService = wsGameService;
    }

    public void clear(Context ctx) {
        try {
            ClearResult result = clearService.clear();
            wsGameService.clearState();

            if (result.isSuccess()) {
                ctx.status(200).result(gson.toJson(result)); // serialize ClearResult
            } else {
                ctx.status(500).result(gson.toJson(Map.of("message", result.getMessage())));
            }
        } catch (Exception e) {
            ctx.status(500).result(gson.toJson(Map.of("message", "Internal server error")));
        }
    }
}
