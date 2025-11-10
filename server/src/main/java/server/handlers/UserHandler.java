package server.handlers;

import chess.model.request.RegisterRequest;
import chess.model.result.RegisterResult;
import com.google.gson.Gson;
import io.javalin.http.Context;
import service.UserService;

import java.util.Map;

public class UserHandler {

    private final UserService userService;
    private final Gson gson; // Gson serializer

    public UserHandler(UserService userService) {
        this.userService = userService;
        this.gson = new Gson(); // initialize Gson
    }

    public void register(Context ctx) {
        RegisterRequest request;
        try {
            // Use Gson to deserialize JSON request body
            request = gson.fromJson(ctx.body(), RegisterRequest.class);
        } catch (Exception e) {
            ctx.status(400).result(gson.toJson(Map.of("message", "Error: bad request")));
            return;
        }

        try {
            RegisterResult result = userService.register(request);

            if (result.isSuccess()) {
                ctx.status(200).result(gson.toJson(result));

            } else {
                String message = result.getMessage() != null ? result.getMessage() : "Internal error";

                if (message.contains("already taken")) {
                    ctx.status(403).result(gson.toJson(Map.of("message", "Error: already taken")));
                } else if (message.contains("Bad request")) {
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
