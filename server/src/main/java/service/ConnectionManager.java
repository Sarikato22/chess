package service;

import com.google.gson.Gson;
import io.javalin.websocket.WsMessageContext;
import websocket.messages.ServerMessage;

import java.util.HashMap;
import java.util.Map;

class ConnectionManager {

    private final Map<String, WsMessageContext> sessions = new HashMap<>();

    void addPlayer(String username, WsMessageContext ctx) {
        sessions.put(username, ctx);
    }

    void removePlayer(String username) {
        sessions.remove(username);
    }

    void broadcastToOthers(String exceptUsername, ServerMessage msg, Gson gson) {
        String json = gson.toJson(msg);
        for (var entry : sessions.entrySet()) {
            if (!entry.getKey().equals(exceptUsername)) {
                entry.getValue().send(json);
            }
        }
    }

    void broadcastToAll(ServerMessage msg, Gson gson) {
        String json = gson.toJson(msg);
        for (var entry : sessions.entrySet()) {
            entry.getValue().send(json);
        }
    }
}//end of class