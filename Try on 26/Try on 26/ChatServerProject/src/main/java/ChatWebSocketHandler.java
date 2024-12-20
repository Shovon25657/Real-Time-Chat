import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.json.JSONObject;

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
    private static final int MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    static {
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        sessionUserMap.put(session, null); // Assign a null username initially
        System.out.println("Client connected: " + session.getRemoteAddress());

        try {
            // Load chat history in JSON format and send it to the client
            String chatHistoryJson = ChatDatabase.loadMessagesAsJson();
            session.getRemote().sendString(chatHistoryJson);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            // First message is the username
            sessionUserMap.put(session, message);
            broadcast("Server: " + message + " has joined the chat.");
        } else {
            // Save the text message to the database
            ChatDatabase.saveMessage(username, message);

            // Broadcast the text message to all clients
            JSONObject jsonMessage = new JSONObject();
            jsonMessage.put("username", username);
            jsonMessage.put("text", message);
            jsonMessage.put("timestamp", new java.util.Date().toString());
            broadcast(jsonMessage.toString());
        }
    }

    @OnWebSocketMessage
    public void onMessage(Session session, byte[] payload, int offset, int length) {
        try {
            if (length > MAX_FILE_SIZE) {
                session.getRemote().sendString("{\"error\":\"File size exceeds the 10 MB limit.\"}");
                return;
            }

            String username = sessionUserMap.get(session);
            String fileName;
            String fileType;

            // Determine the file type
            if (isPng(payload)) {
                fileType = "image";
                fileName = "file_" + System.currentTimeMillis() + ".png";
            } else if (isMp4(payload)) {
                fileType = "video";
                fileName = "file_" + System.currentTimeMillis() + ".mp4";
            } else {
                session.getRemote().sendString("{\"error\":\"Unsupported file type. Only PNG and MP4 are allowed.\"}");
                return;
            }

            // Save the file to the uploads directory
            File file = new File(UPLOAD_DIR + fileName);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(payload, offset, length);
            }

            // Save the multimedia message to the database
            ChatDatabase.saveFileMessage(username, fileName, fileType);

            // Broadcast the multimedia message to all clients
            JSONObject message = new JSONObject();
            message.put("username", username);
            message.put("file_path", fileName);
            message.put("file_type", fileType);
            message.put("timestamp", new java.util.Date().toString());
            broadcast(message.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private boolean isPng(byte[] data) {
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

    private boolean isMp4(byte[] data) {
        if (data.length < 12) return false;
        for (int i = 0; i < data.length - 8; i++) {
            if (data[i + 4] == (byte) 0x66 && // 'f'
                    data[i + 5] == (byte) 0x74 && // 't'
                    data[i + 6] == (byte) 0x79 && // 'y'
                    data[i + 7] == (byte) 0x70) { // 'p'
                return true;
            }
        }
        return false;
    }
}
