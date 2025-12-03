package ui;

import com.google.gson.Gson;
import jakarta.websocket.*;
import java.net.URI;

import websocket.commands.*;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

@ClientEndpoint
public class WebSocketCommunicator {
    private final Gson gson = new Gson();
    private final ServerMessageObserver observer;
    private Session session;


    public WebSocketCommunicator(ServerMessageObserver observer, String serverUrl) throws Exception {
        this.observer = observer;
        WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        String wsUrl = serverUrl.replaceFirst("^http", "ws") + "/ws";
        this.session = container.connectToServer(this, new URI(wsUrl));
    }

    @OnMessage
    public void onMessage(String json) {
        ServerMessage base = gson.fromJson(json, ServerMessage.class);
        switch (base.getServerMessageType()) {
            case LOAD_GAME -> {
                LoadGameMessage msg = gson.fromJson(json, LoadGameMessage.class);
                observer.notify(msg);
            }
            case NOTIFICATION -> {
                NotificationMessage msg = gson.fromJson(json, NotificationMessage.class);
                observer.notify(msg);
            }
            case ERROR -> {
                ErrorMessage msg = gson.fromJson(json, ErrorMessage.class);
                observer.notify(msg);
            }
        }
    }


    public void sendConnect(String authToken, int gameId) throws Exception {
        UserGameCommand cmd = new ConnectCommand(authToken, gameId);
        session.getBasicRemote().sendText(gson.toJson(cmd));
    }

    public void sendMakeMove(String authToken, int gameId, chess.ChessMove move) throws Exception {
        UserGameCommand cmd = new MakeMoveCommand(authToken, gameId, move);
        session.getBasicRemote().sendText(gson.toJson(cmd));
    }

    public void sendLeave(String authToken, int gameId) throws Exception {
        UserGameCommand cmd = new LeaveGameCommand(authToken, gameId);
        session.getBasicRemote().sendText(gson.toJson(cmd));
    }

    public void sendResign(String authToken, int gameId) throws Exception {
        UserGameCommand cmd = new ResignCommand(authToken, gameId);
        session.getBasicRemote().sendText(gson.toJson(cmd));
    }

    public void close() throws Exception {
        if (session != null) {
            session.close();
        }
    }
}