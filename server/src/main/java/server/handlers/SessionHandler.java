package server.handlers;

import chess.model.request.SessionRequest;
import chess.model.result.SessionResult;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.UnauthorizedException;
import io.javalin.http.Context;
import service.SessionService;

import java.util.Map;

public class SessionHandler {

    private final SessionService sessionService;
    private final Gson gson;

    public SessionHandler(SessionService sessionService) {
        this.sessionService = sessionService;
        this.gson = new Gson();
    }

    public void logout(Context ctx) {
        String authToken = ctx.header("authorization");

        if (authToken == null || authToken.isEmpty()) {
            ctx.status(401).result(gson.toJson(Map.of("message", "Error: unauthorized")));
            return;
        }

        try {
            SessionResult result = sessionService.logout(authToken);
            if (result.isSuccess()) {
                ctx.status(200).result(gson.toJson(Map.of("success", true, "message", "Logged out successfully")));

            } else {
                ctx.status(401).result(gson.toJson(Map.of("message", "Error: unauthorized")));
            }

        } catch (DataAccessException e) {
//            System.err.println("Database error during logout: " + e.getMessage());
            ctx.status(500).result(gson.toJson(Map.of("message", "Error: internal server error during logout")));
        } catch (UnauthorizedException e) {
            ctx.status(401).result(gson.toJson(Map.of("message", "Error: unexpected internal error during logout")));
        }
    }


    public void login(Context ctx) {
        SessionRequest sessionRequest;

        try {
            sessionRequest = gson.fromJson(ctx.body(), SessionRequest.class);
        } catch (Exception e) {
            ctx.status(400).result(gson.toJson(Map.of("message", "Error: bad request")));
            return;
        }

        try {
            SessionResult sessionResult = sessionService.login(sessionRequest);

            if (sessionResult.isSuccess()) {
                ctx.status(200).result(gson.toJson(sessionResult));
            } else {
                String message = sessionResult.getMessage() != null ? sessionResult.getMessage() : "Error: Bad request";

                if (message.contains("Invalid")) {
                    ctx.status(401).result(gson.toJson(Map.of("message", "Error: unauthorized")));
                } else if (message.contains("Bad request") || message.contains("required")) {
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
