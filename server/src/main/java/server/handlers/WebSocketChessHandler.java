package server.handlers;

import com.google.gson.Gson;
import io.javalin.websocket.*;
import org.jetbrains.annotations.NotNull;
import service.WebSocketGameService;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;

public class WebSocketChessHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {

    private final WebSocketGameService service;
    private final Gson gson = new Gson();

    public WebSocketChessHandler(WebSocketGameService service) {
        this.service = service;
    }

    @Override
    public void handleConnect(@NotNull WsConnectContext ctx) {
        ctx.enableAutomaticPings();
        System.out.println("Websocket connected");
    }

    @Override
    public void handleMessage(@NotNull WsMessageContext ctx) {
        service.handleMessage(ctx);
    }

    @Override
    public void handleClose(@NotNull WsCloseContext ctx) {
        System.out.println("Websocket closed");
    }
}
