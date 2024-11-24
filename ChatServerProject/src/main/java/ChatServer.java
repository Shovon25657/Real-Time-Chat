import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

public class ChatServer {
    private static final int PORT = 12345;
    static Set<PrintWriter> clientWriters = new CopyOnWriteArraySet<>();
    static List<String> chatHistory = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        System.out.println("Chat server starting...");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is listening on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");
                try {
                    PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                    clientWriters.add(writer);
                    new ClientHandler(socket, writer).start();
                } catch (IOException e) {
                    System.out.println("Error setting up client connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    static void broadcast(String message) {
        synchronized (clientWriters) {
            for (Iterator<PrintWriter> it = clientWriters.iterator(); it.hasNext(); ) {
                PrintWriter writer = it.next();
                try {
                    writer.println(message);
                } catch (Exception e) {
                    System.out.println("Error sending message. Removing client.");
                    it.remove();
                }
            }
        }
        addMessageToHistory(message);
    }

    static void addMessageToHistory(String message) {
        if (chatHistory.size() > 50) {
            chatHistory.remove(0);
        }
        chatHistory.add(message);
    }

    static void sendChatHistory(PrintWriter writer) {
        synchronized (chatHistory) {
            int start = Math.max(0, chatHistory.size() - 10); // Limit to last 10 messages
            for (int i = start; i < chatHistory.size(); i++) {
                writer.println(chatHistory.get(i));
            }
        }
    }
}
