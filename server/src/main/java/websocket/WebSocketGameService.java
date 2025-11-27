package websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.*;
import websocket.messages.ErrorMessage;

public class WebSocketGameService {

    private final Gson gson = new Gson();

    public void handleMessage(Session session, String json) {
        int gameId = -1;

        try {
            UserGameCommand command = gson.fromJson(json, UserGameCommand.class);
            gameId = command.getGameID();
            String username = getUsername(command.getAuthString());
            saveSession(gameId, session);

            switch (command.getCommandType()) {
                case CONNECT -> connect(session, username, (ConnectCommand) command);
                case MAKE_MOVE -> makeMove(session, username, (MakeMoveCommand) command);
                case LEAVE -> leaveGame(session, username, (LeaveGameCommand) command);
                case RESIGN -> resign(session, username, (ResignCommand) command);
            }
        } catch (Exception ex) {
            sendMessage(session, gameId, new ErrorMessage("Error: " + ex.getMessage()));
        }
    }

    private String getUsername(String authToken) { return null; }

    private void saveSession(int gameId, Session session) {}

    private void sendMessage(Session root, int gameId, ErrorMessage msg) {
        // gson.toJson(msg) then root.getRemote().sendString(...)
    }

    private void connect(Session session, String username, ConnectCommand command) {}

    private void makeMove(Session session, String username, MakeMoveCommand command) {}

    private void leaveGame(Session session, String username, LeaveGameCommand command) {}

    private void resign(Session session, String username, ResignCommand command) {}
}

