package service;

import chess.ChessGame;
import chess.InvalidMoveException;
import chess.model.data.GameData;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import io.javalin.websocket.WsMessageContext;
import websocket.commands.*;
import websocket.messages.*;

import java.util.HashMap;
import java.util.Map;

public class WebSocketGameService {

    private final Gson gson = new Gson();
    private final Map<Integer, ConnectionManager> connections = new HashMap<>();
    private final Map<Integer, Boolean> gameOver = new HashMap<>();
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

            switch (command.getCommandType()) {
                case CONNECT -> connect(wsCtx, username, command);
                case MAKE_MOVE -> makeMove(wsCtx, username, command);
                case LEAVE   -> leaveGame(wsCtx, username, command);
                case RESIGN  -> resign(wsCtx, username, command);
            }
        } catch (Exception ex) {
            sendMessage(wsCtx, gameId, new ErrorMessage("Error: " + ex.getMessage()));
        }
    }

    private String getUsername(String authToken) throws Exception {
        return dataAccess.getUsernameByToken(authToken);
    }


    private void sendMessage(WsMessageContext root, int gameId, ServerMessage msg) {
        String json = gson.toJson(msg);
        root.send(json);
    }

    private ConnectionManager getConnectionManager(int gameId) {
        return connections.computeIfAbsent(gameId, id -> new ConnectionManager());
    }

    private void connect(WsMessageContext ctx, String username, UserGameCommand command)  throws Exception {
        int gameId = command.getGameID();
        var gameData = dataAccess.getGameData(gameId);
        if (gameData == null) {
            sendMessage(ctx, gameId, new ErrorMessage("Error: bad request"));
            return;
        }

        ChessGame game = dataAccess.getChessGame(gameId);

        ConnectionManager manager = getConnectionManager(gameId);
        manager.addPlayer(username, ctx);

        sendMessage(ctx, gameId, new LoadGameMessage(game));

        String noteText;
        if (username.equals(gameData.getWhiteUsername())) {
            noteText = username + " joined as WHITE";
        } else if (username.equals(gameData.getBlackUsername())) {
            noteText = username + " joined as BLACK";
        } else {
            noteText = username + " joined as OBSERVER";
        }
        manager.broadcastToOthers(username, new NotificationMessage(noteText), gson);
    }
    private void makeMove(WsMessageContext ctx, String username, UserGameCommand command) {
        int gameId = command.getGameID();
        var move = command.getMove();
        try {
            GameData gameData = dataAccess.getGameData(gameId);

            if (Boolean.TRUE.equals(gameOver.get(gameId))) {
                sendMessage(ctx, gameId, new ErrorMessage("Error: game already over"));
                return;
            }
            if (gameData == null) {
                sendMessage(ctx, gameId, new ErrorMessage("Error: bad request"));
                return;
            }

            //Determine the color this user is allowed to move
            ChessGame.TeamColor playerColor = null;
            if (username.equals(gameData.getWhiteUsername())) {
                playerColor = ChessGame.TeamColor.WHITE;
            } else if (username.equals(gameData.getBlackUsername())) {
                playerColor = ChessGame.TeamColor.BLACK;
            } else {
                sendMessage(ctx, gameId, new ErrorMessage("Error: observer cannot move"));
                return;
            }

            ChessGame game = dataAccess.getChessGame(gameId);

            if (game.getTeamTurn() != playerColor) {
                sendMessage(ctx, gameId, new ErrorMessage("Error: not your turn"));
                return;
            }

            try {
                game.makeMove(move);
            } catch (InvalidMoveException e) {
                sendMessage(ctx, gameId, new ErrorMessage("Error: invalid move"));
                return;
            }

            dataAccess.updateChessGame(gameId, game);
            ConnectionManager manager = getConnectionManager(gameId);
            LoadGameMessage load = new LoadGameMessage(game);
            manager.broadcastToAll(load, gson);
            String moveText = username + " moved from " +
                    move.getStartPosition() + " to " + move.getEndPosition();
            NotificationMessage moveNote = new NotificationMessage(moveText);
            manager.broadcastToOthers(username, moveNote, gson);

            ChessGame.TeamColor opponent =
                    (playerColor == ChessGame.TeamColor.WHITE)
                            ? ChessGame.TeamColor.BLACK
                            : ChessGame.TeamColor.WHITE;

            if (game.isInCheckmate(opponent)) {
                manager.broadcastToAll(
                        new NotificationMessage("Checkmate against " + opponent), gson);
            } else if (game.isInCheck(opponent)) {
                manager.broadcastToAll(
                        new NotificationMessage("Check against " + opponent), gson);
            } else if (game.isInStalemate(opponent)) {
                manager.broadcastToAll(
                        new NotificationMessage("Stalemate for " + opponent), gson);
            }

        } catch (Exception ex) {
            sendMessage(ctx, gameId, new ErrorMessage("Error: " + ex.getMessage()));
        }
    }

        private void leaveGame (WsMessageContext ctx, String username, UserGameCommand command){
            int gameId = command.getGameID();
            try {
                var gameData = dataAccess.getGameData(gameId);
                if (gameData == null) {
                    sendMessage(ctx, gameId, new ErrorMessage("Error: bad request"));
                    return;
                }

                boolean isPlayer = false;
                if (username.equals(gameData.getWhiteUsername())) {
                    gameData.setWhiteUsername(null);
                    isPlayer = true;
                } else if (username.equals(gameData.getBlackUsername())) {
                    gameData.setBlackUsername(null);
                    isPlayer = true;
                }

                if (isPlayer) {
                    dataAccess.updateGame(gameData);
                }
                ConnectionManager manager = connections.get(gameId);
                if (manager != null) {
                    manager.removePlayer(username);

                    String noteText = username + " left the game";
                    manager.broadcastToOthers(username, new NotificationMessage(noteText), gson);
                }
            } catch (Exception ex) {
                sendMessage(ctx, gameId, new ErrorMessage("Error: " + ex.getMessage()));
            }
        }

    private void resign(WsMessageContext ctx, String username, UserGameCommand command) {
        int gameId = command.getGameID();

        try {
            var gameData = dataAccess.getGameData(gameId);
            if (gameData == null) {
                sendMessage(ctx, gameId, new ErrorMessage("Error: bad request"));
                return;
            }

            boolean isWhite = username.equals(gameData.getWhiteUsername());
            boolean isBlack = username.equals(gameData.getBlackUsername());
            if (!isWhite && !isBlack) {
                // observers cannot resign
                sendMessage(ctx, gameId, new ErrorMessage("Error: cannot resign"));
                return;
            }
            if (Boolean.TRUE.equals(gameOver.get(gameId))) {
                sendMessage(ctx, gameId, new ErrorMessage("Error: game already over"));
                return;
            }

            gameOver.put(gameId, true);
            ConnectionManager manager = connections.get(gameId);
            if (manager != null) {
                String noteText = username + " resigned";
                NotificationMessage note = new NotificationMessage(noteText);
                manager.broadcastToAll(note, gson);
            }

        } catch (Exception ex) {
            sendMessage(ctx, gameId, new ErrorMessage("Error: " + ex.getMessage()));
        }
    }
}
