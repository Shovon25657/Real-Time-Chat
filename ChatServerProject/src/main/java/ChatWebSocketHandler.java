import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@WebSocket
public class ChatWebSocketHandler {
    private static Set<Session> sessions = Collections.synchronizedSet(new HashSet<>());

    @OnWebSocketConnect
    public void onConnect(Session session) {
        sessions.add(session);
        broadcast("A new user has joined the chat!");
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        broadcast(message);
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        sessions.remove(session);
        broadcast("A user has left the chat.");
    }

    private void broadcast(String message) {
        synchronized (sessions) {
            for (Session session : sessions) {
                try {
                    session.getRemote().sendString(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
