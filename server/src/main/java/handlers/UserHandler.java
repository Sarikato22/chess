package handlers;

import io.javalin.http.Context;
import java.util.Map;

public class UserHandler {

    // POST /user
    public static void register(Context ctx) {
        Map<String, String> response = Map.of(
                "authToken", "fake-auth-token",
                "message", "User registered successfully"
        );
        ctx.json(response);
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
}
