package service;

import com.google.gson.Gson;
import io.javalin.websocket.WsMessageContext;
import websocket.commands.*;
import websocket.messages.ErrorMessage;

public class WebSocketGameService {

    private final Gson gson = new Gson();

    public void handleMessage(WsMessageContext wsCtx) {
        int gameId = -1;

        try {
            String json = wsCtx.message();
            UserGameCommand command = gson.fromJson(json, UserGameCommand.class);
            gameId = command.getGameID();
            String username = getUsername(command.getAuthString());
            saveSession(gameId, username, wsCtx);

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

    private void saveSession(int gameId, String username, WsMessageContext ctx) {
        // TODO: store per-game connections
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