package simple;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SimpleChatClient {

    private String serverIP;
    private int serverPort;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private BufferedReader userInput;

    public SimpleChatClient(String serverIP, int serverPort) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
        this.userInput = new BufferedReader(new InputStreamReader(System.in));
    }

    public void start() throws IOException {
        System.out.println("Connecting to server " + serverIP + ":" + serverPort + "...");
        socket = new Socket(serverIP, serverPort);
        System.out.println("Connected to server.");

        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        String serverMessage;
        while ((serverMessage = in.readLine()) != null) {
            System.out.println("(Server): " + serverMessage);
            if (serverMessage.equalsIgnoreCase("close")) {
                break;
            }
            System.out.print("Enter message (or 'close' to exit): ");
            String clientMessage = userInput.readLine();
            out.println(clientMessage);
            if (clientMessage.equalsIgnoreCase("close")) {
                break;
            }
        }
    }

    public void stop() throws IOException {
        if (out != null) out.close();
        if (in != null) in.close();
        if (socket != null && !socket.isClosed()) {
            socket.close();
            System.out.println("Disconnected from server.");
        }
        if (userInput != null) userInput.close();
    }

    public static void main(String[] args) {
        SimpleChatClient client = new SimpleChatClient("localhost", 50000); // Use "localhost" for testing on the same machine
        try {
            client.start();
        } catch (IOException e) {
            System.err.println("Error connecting or communicating with server: " + e.getMessage());
        } finally {
            try {
                client.stop();
            } catch (IOException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
}