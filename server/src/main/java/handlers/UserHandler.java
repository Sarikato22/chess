package handlers;

import chess.model.request.RegisterRequest;
import chess.model.result.RegisterResult;
import dataaccess.MemoryDataAccess;
import io.javalin.http.Context;
import services.UserService;

import java.util.Map;

public class UserHandler {
    private static final UserService userService = new UserService(new MemoryDataAccess());

    public static void register(Context ctx) {
        RegisterRequest request = ctx.bodyAsClass(RegisterRequest.class);
        var result = userService.register(request);
        ctx.json(result);
    }


    // POST /session
    public static void login(Context ctx) {
        Map<String, String> response = Map.of(
                "authToken", "fake-auth-token",
                "message", "User logged in successfully"
        );
        ctx.json(response);
    }

    // DELETE /session
    public static void logout(Context ctx) {
        Map<String, String> response = Map.of(
                "message", "User logged out successfully"
        );
        ctx.json(response);
    }
}//end of class
