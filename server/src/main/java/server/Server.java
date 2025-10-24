package server;

import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import io.javalin.Javalin;
import server.handlers.*;
import service.ClearService;
import service.GameService;
import service.SessionService;
import service.UserService;

public class Server {

    private final Javalin javalin;

    public Server() {
        DataAccess dao = new MemoryDataAccess();
        UserService userService = new UserService(dao);
        ClearService clearService = new ClearService(dao);
        AdminHandler adminHandler = new AdminHandler(clearService);
        UserHandler userHandler = new UserHandler(userService);
        //
        SessionService sessionService = new SessionService(dao);
        SessionHandler sessionHandler = new SessionHandler(sessionService);
        //
        GameService gameService = new GameService(dao);
        GameHandler gameHandler = new GameHandler(gameService);
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // User endpoints
        javalin.post("/user", userHandler::register);
        javalin.post("/session", sessionHandler::login);
        javalin.delete("/session", sessionHandler::logout);

        // Game endpoints
        javalin.get("/game", gameHandler::listGames);
        javalin.post("/game", gameHandler::createGame);
        javalin.put("/game", gameHandler::joinGame);

        // Admin endpoint
        javalin.delete("/db", adminHandler::clear);

    }



    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}

