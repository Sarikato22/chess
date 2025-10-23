package handlers;

import chess.model.request.RegisterRequest;
import chess.model.result.RegisterResult;
import service.UserService;
import io.javalin.http.Context;
import java.util.Map;

public class UserHandler {

    private final UserService userService;

    public UserHandler(UserService userService) {
        this.userService = userService;
    }

    public void register(Context ctx) {
        RegisterRequest request;
        try {
            request = ctx.bodyAsClass(RegisterRequest.class);
        } catch (Exception e) {
            ctx.status(400).json(Map.of("message", "Error: bad request"));
            return;
        }

        try {
            RegisterResult result = userService.register(request);

            if (result.isSuccess()) {
                // Success
                ctx.status(200).json(Map.of(
                        "username", result.getUsername(),
                        "authToken", result.getAuthToken()
                ));
            } else {
                // Failure
                String message = result.getMessage() != null ? result.getMessage() : "Internal error";

                if (message.contains("already taken")) {
                    ctx.status(403).json(Map.of("message", "Error: already taken"));
                } else if (message.contains("Bad request")) {
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
