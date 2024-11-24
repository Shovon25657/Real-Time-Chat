import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class WebSocketServer {
    public static void main(String[] args) throws Exception {
        // Create Jetty server on port 8080
        Server server = new Server(8080);

        // Create a context handler for WebSocket
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        // Add WebSocket servlet using ServletHolder
        ServletHolder wsHolder = new ServletHolder("ws-chat", ChatWebSocketServlet.class);
        context.addServlet(wsHolder, "/chat");

        // Start the server
        server.start();
        System.out.println("WebSocket server started on ws://localhost:8080/chat");
        server.join();
    }
}
