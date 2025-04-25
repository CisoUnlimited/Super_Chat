package chat_multihilo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 *
 * @author Propietario
 */
public class SocketTCPClient {

    private final String serverIP;
    private final int serverPort;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    public SocketTCPClient(String serverIP, int serverPort) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
    }

    public void start() throws IOException {
        System.out.println("(Cliente) Estableciendo Conexión.");
        socket = new Socket(serverIP, serverPort);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        System.out.println("(Cliente) Conexión Establecida.");
        
        new Thread(() -> {
            try {
                String msgFromServer;
                while ((msgFromServer = in.readLine()) != null) {                    
                    System.out.println(">> " + msgFromServer);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
        
        BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
        String msg;
        while ((msg = keyboard.readLine()) != null) {
            out.println(msg);
        }
    }

    public void stop() throws IOException {
        System.out.println("(Cliente) Cerrando Conexión.");
        in.close();
        out.close();
        socket.close();
        System.out.println("(Cliente) Conexión Cerrada.");
    }

    public static void main(String[] args) {
        try {
            SocketTCPClient client = new SocketTCPClient("10.208.6.1", 50000);
            client.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
