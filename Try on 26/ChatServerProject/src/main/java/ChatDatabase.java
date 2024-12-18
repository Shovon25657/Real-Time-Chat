import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONArray;
import org.json.JSONObject;

public class ChatDatabase {
    public static void saveMessage(String username, String message) {
        String query = "INSERT INTO messages (username, message) VALUES (?, ?)";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, message);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void saveFileMessage(String username, String filePath, String fileType) {
        String query = "INSERT INTO messages (username, file_path, file_type) VALUES (?, ?, ?)";
        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, filePath);
            stmt.setString(3, fileType);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }






    public static String loadMessagesAsJson() {
        JSONArray messagesArray = new JSONArray();
        String query = "SELECT username, message, file_path, file_type, timestamp FROM messages ORDER BY timestamp ASC";

        try (Connection conn = DBConfig.getConnection();
             var stmt = conn.prepareStatement(query);
             var rs = stmt.executeQuery()) {

            while (rs.next()) {
                JSONObject messageObject = new JSONObject();
                messageObject.put("username", rs.getString("username"));
                messageObject.put("text", rs.getString("message")); // Text messages
                messageObject.put("file_path", rs.getString("file_path")); // File path for multimedia
                messageObject.put("file_type", rs.getString("file_type")); // File type
                messageObject.put("timestamp", rs.getString("timestamp"));
                messagesArray.put(messageObject);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return messagesArray.toString();
    }




    public static String loadMessages() {
        StringBuilder chatHistory = new StringBuilder();
        String query = "SELECT username, message, timestamp FROM messages ORDER BY timestamp ASC"; // Fetch in ascending order

        try (Connection conn = DBConfig.getConnection();
             var stmt = conn.prepareStatement(query);
             var rs = stmt.executeQuery()) {

            while (rs.next()) {
                String username = rs.getString("username");
                String message = rs.getString("message");
                String timestamp = rs.getString("timestamp");

                // Format message
                chatHistory.append("[").append(timestamp).append("] ")
                        .append(username).append(": ")
                        .append(message).append("\n");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return chatHistory.toString(); // Return the formatted chat history
    }


}
