package server;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import dataaccess.MySqlDataAccess;
import io.javalin.Javalin;
import server.handlers.WebSocketChessHandler;
import server.handlers.*;
import service.*;

public class Server {

    private final Javalin app;
    private final DataAccess dao;

    public Server() {
        this.dao = new MySqlDataAccess();

        UserService userService = new UserService(dao);
        ClearService clearService = new ClearService(dao);
        SessionService sessionService = new SessionService(dao);
        GameService gameService = new GameService(dao);
        WebSocketGameService wsGameService = new WebSocketGameService(dao);

        AdminHandler adminHandler = new AdminHandler(clearService,wsGameService);
        UserHandler userHandler = new UserHandler(userService);
        SessionHandler sessionHandler = new SessionHandler(sessionService);
        GameHandler gameHandler = new GameHandler(gameService);
        WebSocketChessHandler wsHandler = new WebSocketChessHandler(wsGameService);

        app = Javalin.create(config -> config.staticFiles.add("web"));

        // HTTP endpoints
        app.post("/user", userHandler::register);
        app.post("/session", sessionHandler::login);
        app.delete("/session", sessionHandler::logout);

        app.get("/game", gameHandler::listGames);
        app.post("/game", gameHandler::createGame);
        app.put("/game", gameHandler::joinGame);

        app.delete("/db", adminHandler::clear);

        // WebSocket endpoint
        app.ws("/ws", ws -> {
            ws.onConnect(wsHandler);
            ws.onMessage(wsHandler);
            ws.onClose(wsHandler);
        });
    }

    public int run(int desiredPort) {
        app.start(desiredPort);
        return app.port();
    }

    public void stop() {
        app.stop();
    }
}
