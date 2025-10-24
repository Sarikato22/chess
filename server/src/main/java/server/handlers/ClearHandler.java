package server.handlers;

import chess.model.result.ClearResult;
import dataaccess.DataAccess;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import service.ClearService;

import java.util.Map;

public class ClearHandler implements Handler {

    private final ClearService clearService;

    public ClearHandler(DataAccess dao) {
        this.clearService = new ClearService(dao);
    }

    @Override
    public void handle(Context ctx) {
        try {
            String authToken = ctx.header("authorization");

            if (authToken == null || authToken.isEmpty()) {
                ctx.status(401).json(Map.of("message", "Error: unauthorized"));
                return;
            }

            ClearResult result = clearService.clear();

            if (result.isSuccess()) {
                ctx.status(200).json(result);
            } else {
                ctx.status(500).json(result);
            }

        } catch (Exception e) {
            ctx.status(500).json(new ClearResult(false, "Error: " + e.getMessage()));
        }
    }

}

