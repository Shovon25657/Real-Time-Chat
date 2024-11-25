import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@WebSocket
public class ChatWebSocketHandler {
    private static final Map<Session, String> sessionUserMap = Collections.synchronizedMap(new HashMap<>());
    private static final String UPLOAD_DIR = "uploads/";

    static {
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        sessionUserMap.put(session, null);
        System.out.println("Client connected: " + session.getRemoteAddress());
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        String username = sessionUserMap.remove(session);
        if (username != null) {
            broadcast("Server: " + username + " has left the chat.");
        }
        System.out.println("Client disconnected: " + session.getRemoteAddress());
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) {
        String username = sessionUserMap.get(session);
        if (username == null) {
            // First message is treated as the username
            sessionUserMap.put(session, message);
            broadcast("Server: " + message + " has joined the chat.");
        } else {
            broadcast(username + ": " + message);
        }
    }

    @OnWebSocketMessage
    public void onMessage(Session session, byte[] payload, int offset, int length) {
        try {
            String username = sessionUserMap.get(session);
            String fileName = "file_" + System.currentTimeMillis() + ".png";
            File file = new File(UPLOAD_DIR + fileName);

            // Validate that the payload is a PNG image
            if (!isPng(payload)) {
                session.getRemote().sendString("Server: Only PNG files are allowed.");
                return;
            }

            // Save the image
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(payload, offset, length);
            }
            System.out.println("PNG file received from " + username + " and saved as " + fileName);

            // Convert the file to a Base64 string for embedding
            String base64Image = Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath()));
            String imageHtml = "<img src='data:image/png;base64," + base64Image + "' style='max-width: 200px;' />";

            // Broadcast the embedded image
            broadcast(username + " shared an image:<br>" + imageHtml);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isPng(byte[] data) {
        // PNG files start with an 8-byte signature: 89 50 4E 47 0D 0A 1A 0A
        if (data.length < 8) return false;
        return data[0] == (byte) 0x89 &&
                data[1] == (byte) 0x50 &&
                data[2] == (byte) 0x4E &&
                data[3] == (byte) 0x47 &&
                data[4] == (byte) 0x0D &&
                data[5] == (byte) 0x0A &&
                data[6] == (byte) 0x1A &&
                data[7] == (byte) 0x0A;
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
