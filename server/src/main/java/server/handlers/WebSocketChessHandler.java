package server.handlers;

import com.google.gson.Gson;
import io.javalin.websocket.*;
import org.jetbrains.annotations.NotNull;
import service.UserService;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;

public class WebSocketChessHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {

    private final UserService.WebSocketGameService service = new UserService.WebSocketGameService();
    private final Gson gson = new Gson();

    @Override
    public void handleConnect(@NotNull WsConnectContext ctx) {
        ctx.enableAutomaticPings();
        System.out.println("Websocket connected");
    }

    @Override
    public void handleMessage(@NotNull WsMessageContext ctx) {
        // For now, just delegate raw JSON to the service:
        service.handleMessage(ctx);
    }

    @Override
    public void handleClose(@NotNull WsCloseContext ctx) {
        System.out.println("Websocket closed");
    }
}
