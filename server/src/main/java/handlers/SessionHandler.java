package handlers;

import chess.model.request.SessionRequest;
import chess.model.result.SessionResult;
import io.javalin.http.Context;
import services.SessionService;
import services.UserService;

import java.util.Map;

public class SessionHandler {

    private final SessionService sessionService;

    public SessionHandler(SessionService sessionService) {
        this.sessionService = sessionService;
    }


    public void logout(Context ctx) {
        String authToken = ctx.header("authorization");

        // No token → unauthorized immediately
        if (authToken == null || authToken.isEmpty()) {
            ctx.status(401).json(Map.of("message", "Error: unauthorized"));
            return;
        }

        try {
            SessionResult sessionResult = sessionService.logout(authToken);

            if (sessionResult.isSuccess()) {
                ctx.status(200).json(Map.of());
            } else {
                String message = sessionResult.getMessage() != null
                        ? sessionResult.getMessage()
                        : "Error: unauthorized";

                if (message.toLowerCase().contains("unauthorized")) {
                    ctx.status(401).json(Map.of("message", "Error: unauthorized"));
                } else {
                    ctx.status(500).json(Map.of("message", "Error: " + message));
                }
            }
        } catch (Exception e) {
            ctx.status(500).json(Map.of("message", "Error: " + e.getMessage()));
            }
        }//end of logout

        public void login(Context ctx) {
            SessionRequest sessionRequest;

            try {
                sessionRequest = ctx.bodyAsClass(SessionRequest.class);
            } catch (Exception e) {
                ctx.status(400).json(Map.of("message", "Error: bad request"));
                return;
            }

            try {
                SessionResult sessionResult = sessionService.login(sessionRequest);

                if (sessionResult.isSuccess()) {
                    // Success
                    ctx.status(200).json(Map.of(
                            "username", sessionResult.getUsername(),
                            "authToken", sessionResult.getAuthToken()
                    ));
                } else {
                    // Failure
                    String message = sessionResult.getMessage() != null ? sessionResult.getMessage() : "Error: Bad request";

                    if (message.contains("Invalid")) {
                        ctx.status(401).json(Map.of("message", "Error: unauthorized"));
                    } else if (message.contains("Bad request") || (message.contains("required"))) {
                        ctx.status(400).json(Map.of("message", "Error: bad request"));
                    }
                }
            } catch (Exception e) {
                ctx.status(500).json(Map.of("message", "Error: " + e.getMessage()));
            }
    }//end of method
} //end of class