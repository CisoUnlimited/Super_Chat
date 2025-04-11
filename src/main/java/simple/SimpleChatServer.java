package simple;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SimpleChatServer {

    private ServerSocket serverSocket;

    public SimpleChatServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("Server started on port " + port);
    }

    public void start() throws IOException {
        System.out.println("Waiting for client connection...");
        Socket clientSocket = serverSocket.accept();
        SocketAddress clientAddress = clientSocket.getRemoteSocketAddress();
        System.out.println("Client connected: " + clientAddress);

        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)
        ) {
            String message;
            while ((message = in.readLine()) != null) {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_TIME);
                String logMessage = timestamp + " - " + clientAddress + ": " + message;
                System.out.println("(Server) Received: " + logMessage);
                // In this simplified version, we just echo the message back to the client
                out.println(message);
                if (message.equalsIgnoreCase("close")) {
                    break;
                }
            }
            System.out.println("Client " + clientAddress + " disconnected.");
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            clientSocket.close();
        }
    }

    public void stop() throws IOException {
        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
            System.out.println("Server stopped.");
        }
    }

    public static void main(String[] args) {
        try {
            SimpleChatServer server = new SimpleChatServer(50000);
            server.start();
        } catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
        }
    }
}