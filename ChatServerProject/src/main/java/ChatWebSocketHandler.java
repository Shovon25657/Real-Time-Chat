import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@WebSocket
public class ChatWebSocketHandler {
    private static final Map<Session, String> sessionUserMap = Collections.synchronizedMap(new HashMap<>());

    @OnWebSocketConnect
    public void onConnect(Session session) {
        // Initially, add the session without a username
        sessionUserMap.put(session, null);
        System.out.println("A client connected: " + session.getRemoteAddress());
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        if (sessionUserMap.get(session) == null) {
            // First message is the username
            sessionUserMap.put(session, message);
            broadcast("Server: " + message + " has joined the chat!");
        } else {
            // Broadcast messages prefixed with the user's username
            String username = sessionUserMap.get(session);
            broadcast(username + ": " + message);
        }
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        String username = sessionUserMap.get(session);
        sessionUserMap.remove(session);
        if (username != null) {
            broadcast("Server: " + username + " has left the chat.");
        }
        System.out.println("A client disconnected: " + session.getRemoteAddress());
    }

    private void broadcast(String message) {
        synchronized (sessionUserMap) {
            for (Session session : sessionUserMap.keySet()) {
                try {
                    session.getRemote().sendString(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
