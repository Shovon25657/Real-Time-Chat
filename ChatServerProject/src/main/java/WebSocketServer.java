import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

public class WebSocketServer {
    public static void main(String[] args) throws Exception {
        // Create a Jetty server on port 8080
        Server server = new Server(8080);

        // Create a context handler for the WebSocket
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.setResourceBase("resources/"); // Directory where index.html resides
        context.addServlet(DefaultServlet.class, "/");


        // Add WebSocket support
        WebSocketHandler wsHandler = new WebSocketHandler() {
            @Override
            public void configure(WebSocketServletFactory factory) {
                factory.register(ChatWebSocketHandler.class);
            }
        };
        context.setHandler(wsHandler);

        // Start the server
        server.start();
        System.out.println("WebSocket server started on ws://localhost:8080");
        server.join();
    }
}
