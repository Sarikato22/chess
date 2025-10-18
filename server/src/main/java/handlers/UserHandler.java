package handlers;
import java.util.HashMap;
import java.util.Map;
import io.javalin.http.Context;

public class UserHandler {
    public static void register(Context ctx) {
        Map<String, String> response = new HashMap<>();
        response.put("message", "User registered successfully");
        ctx.json(response);

    }
}
