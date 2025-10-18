package handlers;

import chess.model.request.RegisterRequest;
import chess.model.result.RegisterResult;
import services.UserService;
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
            ctx.status(400).json(Map.of("message", "Invalid request JSON"));
            return;
        }

        try {
            RegisterResult result = userService.register(request);
            ctx.json(result);
        } catch (Exception e) {
            ctx.status(500).json(Map.of("message", "Internal server error"));
        }
    }
}
