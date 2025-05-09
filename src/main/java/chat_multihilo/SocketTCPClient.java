package chat_multihilo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

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
        socket = new Socket(serverIP, serverPort);
        System.out.println(" [Cliente] Conexión Establecida.");
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

        // Hilo para recibir mensajes del servidor
        new Thread(() -> {
            try {
                String msgFromServer;
                while ((msgFromServer = in.readLine()) != null) {
                    System.out.println(msgFromServer);
                }
                System.out.println(" [Cliente] Conexión cerrada por el servidor.");
            } catch (IOException e) {
                System.out.println(" [Cliente] ¡Vuelve pronto!");
            }
        }).start();

        // Bucle principal para leer mensajes del usuario
        BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
        String msg;
        boolean salir = false;
        while (!salir && (msg = keyboard.readLine()) != null) {
            sendMessage(msg);
        }
        // Finaliza la conexión
        stop();
    }

    private void sendMessage(String msg) {
        out.println(msg);
    }

    public void stop() throws IOException {
        System.out.println(" [Cliente] Cerrando Conexión.");
        socket.close();
        in.close();
        out.close();
        System.out.println(" [Cliente] Conexión Cerrada.");
    }

    public static void main(String[] args) {
        try {
            SocketTCPClient client = new SocketTCPClient("localhost", 50000);
            client.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
