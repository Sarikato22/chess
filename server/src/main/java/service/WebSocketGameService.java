package service;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import io.javalin.websocket.WsMessageContext;
import websocket.commands.*;
import websocket.messages.*;

public class WebSocketGameService {

    private final Gson gson = new Gson();
    private final DataAccess dataAccess;

    public WebSocketGameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

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

    private String getUsername(String authToken) throws Exception {
        return dataAccess.getUsernameByToken(authToken);
    }

    private void saveSession(int gameId, String username, WsMessageContext ctx) {
        // TODO: implement ConnectionManager later
    }

    private void sendMessage(WsMessageContext root, int gameId, ServerMessage msg) {
        String json = gson.toJson(msg);
        root.send(json);
    }

    private void connect(WsMessageContext ctx, String username, ConnectCommand command) throws Exception {
        int gameId = command.getGameID();

        var gameData = dataAccess.getGameData(gameId);
        if (gameData == null) {
            sendMessage(ctx, gameId, new ErrorMessage("Error: bad request"));
            return;
        }

        ChessGame game = dataAccess.getChessGame(gameId);

        var loadMsg = new LoadGameMessage(game);
        sendMessage(ctx, gameId, loadMsg);

        // 4) Later, you’ll also broadcast NOTIFICATION to other sessions in this game
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
