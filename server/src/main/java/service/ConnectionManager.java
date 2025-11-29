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

    void broadcastToAll(ServerMessage msg, Gson gson) {
        String json = gson.toJson(msg);
        var it = sessions.entrySet().iterator();
        while (it.hasNext()) {
            var entry = it.next();
            try {
                entry.getValue().send(json);
            } catch (Exception e) {
                // channel is dead; drop this session
                it.remove();
            }
        }
    }

    void broadcastToOthers(String exceptUsername, ServerMessage msg, Gson gson) {
        String json = gson.toJson(msg);
        var it = sessions.entrySet().iterator();
        while (it.hasNext()) {
            var entry = it.next();
            if (entry.getKey().equals(exceptUsername)) {
                continue;
            }
            try {
                entry.getValue().send(json);
            } catch (Exception e) {
                it.remove();
            }
        }
    }
}//end of class