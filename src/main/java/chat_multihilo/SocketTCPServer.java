package chat_multihilo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 *
 * @author Ciso
 */
public class SocketTCPServer {
    
    private final ServerSocket serverSocket;
    public static final List<Process_Manager> clients = new CopyOnWriteArrayList<>(); // Es más seguro que ArrayList para manejar hilos
    
    public SocketTCPServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println(" (Servidor) A la espera de conexiones.");
        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println(" (Servidor) Conexión establecida.");
            Process_Manager clientHandler = new Process_Manager(socket);
            clients.add(clientHandler);
            clientHandler.start();
        }
    }
    
    public void stop() throws IOException {
        System.out.println(" (Servidor) Cerrando todo.");
        serverSocket.close();
    }
    
    public static void broadcast(String msg, Process_Manager pm) {
        for (Process_Manager client : clients) {
            if (client != pm) { // Esto evita que se reenvíe el mensaje al remitente
                client.sendMessage(msg);
            }
        }
    }
    
    public static void main(String[] args) {
        try {
            SocketTCPServer server = new SocketTCPServer(50000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
