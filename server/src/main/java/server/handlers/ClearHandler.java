package server.handlers;

import chess.model.result.ClearResult;
import dataaccess.DataAccess;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import service.ClearService;
import com.google.gson.Gson;

import java.util.Map;

public class ClearHandler implements Handler {

    private final ClearService clearService;
    private final Gson gson; // Gson serializer

    public ClearHandler(DataAccess dao) {
        this.clearService = new ClearService(dao);
        this.gson = new Gson(); // initialize Gson
    }

    @Override
    public void handle(Context ctx) {
        try {
            String authToken = ctx.header("authorization");

            if (authToken == null || authToken.isEmpty()) {
                ctx.status(401).result(gson.toJson(Map.of("message", "Error: unauthorized")));
                return;
            }

            ClearResult result = clearService.clear();

            if (result.isSuccess()) {
                ctx.status(200).result(gson.toJson(result)); // serialize ClearResult to JSON
            } else {
                ctx.status(500).result(gson.toJson(result)); // serialize ClearResult to JSON
            }

        } catch (Exception e) {
            ctx.status(500).result(gson.toJson(new ClearResult(false, "Error: " + e.getMessage())));
        }
    }
}

