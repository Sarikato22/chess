package service;

import chess.ChessGame;
import chess.ChessPosition;
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

    public void clearState() {
        gameOver.clear();
        connections.clear();
    }


    public void handleMessage(WsMessageContext wsCtx) {
        int gameId = -1;

        try {
            String json = wsCtx.message();
            UserGameCommand base = gson.fromJson(json, UserGameCommand.class);
            gameId = base.getGameID();
            String username = getUsername(base.getAuthString());

            switch (base.getCommandType()) {
                case CONNECT -> {
                    ConnectCommand cmd = gson.fromJson(json, ConnectCommand.class);
                    connect(wsCtx, username, cmd);
                }
                case MAKE_MOVE -> {
                    MakeMoveCommand cmd = gson.fromJson(json, MakeMoveCommand.class);
                    makeMove(wsCtx, username, cmd);
                }
                case LEAVE -> {
                    LeaveGameCommand cmd = gson.fromJson(json, LeaveGameCommand.class);
                    leaveGame(wsCtx, username, cmd);
                }
                case RESIGN -> {
                    ResignCommand cmd = gson.fromJson(json, ResignCommand.class);
                    resign(wsCtx, username, cmd);
                }
            }

        } catch (Exception ex) {
            sendMessage(wsCtx, gameId, new ErrorMessage("Error: " + ex.getMessage()));
        }
    }



    private String getUsername(String authToken) throws Exception {
        String username = dataAccess.getUsernameByToken(authToken);
        if (username == null) {
            throw new Exception("Error: unauthorized");
        }
        return username;
    }

    private void sendMessage(WsMessageContext root, int gameId, ServerMessage msg) {
        String json = gson.toJson(msg);
        root.send(json);
    }

    private ConnectionManager getConnectionManager(int gameId) {
        return connections.computeIfAbsent(gameId, id -> new ConnectionManager());
    }

    private void connect(WsMessageContext ctx, String username, ConnectCommand command) throws Exception {
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

    private String formatSquare(ChessPosition pos) {
        char file = (char) ('a' + pos.getColumn() - 1); // 1→'a'
        char rank = (char) ('0' + pos.getRow());        // 1→'1'
        return "" + file + rank;
    }

    private void makeMove(WsMessageContext ctx, String username, MakeMoveCommand command) {
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

            // Determine the color this user is allowed to move
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

            String from = formatSquare(move.getStartPosition());
            String to   = formatSquare(move.getEndPosition());
            String moveText = username + " moved " + from + " to " + to;
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
    private void leaveGame(WsMessageContext ctx, String username, LeaveGameCommand command) {
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


    private void resign(WsMessageContext ctx, String username, ResignCommand command) {
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
                sendMessage(ctx, gameId, new ErrorMessage("Error: cannot resign"));
                return;
            }

            // Only block *second* (or later) resigns
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
