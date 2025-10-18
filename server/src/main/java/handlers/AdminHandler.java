package handlers;

import io.javalin.http.Context;
import java.util.Map;

public class AdminHandler {

    // DELETE /db
    public static void clearDatabase(Context ctx) {
        Map<String, String> response = Map.of(
                "message", "All data cleared successfully"
        );
        ctx.json(response);
    }
}
