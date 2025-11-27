package service;

import chess.model.request.RegisterRequest;
import chess.model.result.RegisterResult;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import io.javalin.websocket.WsMessageContext;
import websocket.commands.*;
import websocket.messages.ErrorMessage;

public class UserService {

    private final DataAccess dao;

    public UserService(DataAccess dao) {
        this.dao = dao;
    }

    public RegisterResult register(RegisterRequest request) {
        try {
            if (request == null) {
                return RegisterResult.failure(null, "Error: Request cannot be null");
            }

            if (request.getUsername() == null || request.getUsername().isEmpty()) {
                return RegisterResult.failure(null, "Bad request: Username is required");
            }

            if (request.getPassword() == null || request.getPassword().isEmpty()) {
                return RegisterResult.failure(request.getUsername(), "Bad request: Password is required");
            }

            if (request.getEmail() == null || request.getEmail().isEmpty()) {
                return RegisterResult.failure(request.getUsername(), "Bad request: Email is required");
            }

            return dao.registerUser(request);

        } catch (Exception e) {
            return RegisterResult.failure(request != null ? request.getUsername() : null,
                    "Error: " + e.getMessage());
        }
    }

    public static class WebSocketGameService {

        private final Gson gson = new Gson();

        public void handleMessage(WsMessageContext wsCtx) {
            int gameId = -1;

            try {
                String json = wsCtx.message();
                UserGameCommand command = gson.fromJson(json, UserGameCommand.class);
                gameId = command.getGameID();
                String username = getUsername(command.getAuthString());
                saveSession(gameId, wsCtx);

                switch (command.getCommandType()) {
                    case CONNECT -> connect(wsCtx, username, (ConnectCommand) command);
                    case MAKE_MOVE -> makeMove(wsCtx, username, (MakeMoveCommand) command);
                    case LEAVE -> leaveGame(wsCtx, username, (LeaveGameCommand) command);
                    case RESIGN -> resign(wsCtx, username, (ResignCommand) command);
                }
            } catch (Exception ex) {
                sendMessage(wsCtx, gameId, new ErrorMessage("Error: " + ex.getMessage()));
            }
        }

        // ===== stubs to fill in later =====

        private String getUsername(String authToken) {
            // TODO: use your auth DAO/service
            return null;
        }

        private void saveSession(int gameId, WsMessageContext ctx) {
            // TODO: store per-game sessions in a ConnectionManager
        }

        private void sendMessage(WsMessageContext root, int gameId, ErrorMessage msg) {
            String json = gson.toJson(msg);
            root.send(json);
        }

        private void connect(WsMessageContext ctx, String username, ConnectCommand command) {
            // TODO
        }

        private void makeMove(WsMessageContext ctx, String username, MakeMoveCommand command) {
            // TODO
        }

        private void leaveGame(WsMessageContext ctx, String username, LeaveGameCommand command) {
            // TODO
        }

        private void resign(WsMessageContext ctx, String username, ResignCommand command) {
            // TODO
        }
    }
}
