package handlers;

import io.javalin.http.Context;
import services.ClearService;
import chess.model.result.ClearResult;
import java.util.Map;

public class AdminHandler {

    private final ClearService clearService;

    public AdminHandler(ClearService clearService) {
        this.clearService = clearService;
    }

    public void clear(Context ctx) {
        try {
            ClearResult result = clearService.clear();
            if (result.isSuccess()) {
                ctx.status(200).json(result);
            } else {
                ctx.status(500).json(Map.of("message", result.getMessage()));
            }
        } catch (Exception e) {
            ctx.status(500).json(Map.of("message", "Internal server error"));
        }
    }
}
