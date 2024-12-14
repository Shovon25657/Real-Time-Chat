import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

    public static String loadMessages() {
        StringBuilder chatHistory = new StringBuilder();
        String query = "SELECT username, message, timestamp FROM messages ORDER BY timestamp DESC LIMIT 50"; // Get last 50 messages

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String username = rs.getString("username");
                String message = rs.getString("message");
                String timestamp = rs.getString("timestamp");

                // Format message
                chatHistory.append("<div><strong>").append(username).append("</strong>: ")
                        .append(message).append(" <em>[").append(timestamp).append("]</em></div>");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Return the chat history as a formatted string
        return chatHistory.toString();
    }
}
