package server;

import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import io.javalin.Javalin;
import handlers.UserHandler;
import handlers.GameHandler;
import handlers.AdminHandler;

import io.javalin.*;
import services.UserService;

public class Server {

    private final Javalin javalin;

    public Server() {
        DataAccess dao = new MemoryDataAccess();
        UserService userService = new UserService(dao);
        UserHandler userHandler = new UserHandler(userService);
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // User endpoints
        javalin.post("/user", userHandler::register);
//        javalin.post("/session", userHandler::login);
//        javalin.delete("/session", userHandler::logout);

        // Game endpoints
        javalin.get("/game", GameHandler::listGames);
        javalin.post("/game", GameHandler::createGame);
        javalin.put("/game", GameHandler::joinGame);

        // Admin endpoint
        javalin.delete("/db", AdminHandler::clearDatabase);
    }


    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }

    public static void main(String[] args) {
        var server = new Server();
        server.run(8080);
        System.out.println("Server running on port 8080");
    }
}

