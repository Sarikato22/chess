package server.handlers;

import chess.model.result.ClearResult;
import com.google.gson.Gson;
import io.javalin.http.Context;
import service.ClearService;

import java.util.Map;

public class AdminHandler {

    private final ClearService clearService;
    private final Gson gson; // Gson serializer

    public AdminHandler(ClearService clearService) {
        this.clearService = clearService;
        this.gson = new Gson(); // initialize Gson
    }

    public void clear(Context ctx) {
        try {
            ClearResult result = clearService.clear();

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
