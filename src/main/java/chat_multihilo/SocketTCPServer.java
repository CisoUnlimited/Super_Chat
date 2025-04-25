package chat_multihilo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Clase que implementa un servidor TCP multicliente para un sistema de chat.
 * Utiliza un {@link ServerSocket} para aceptar conexiones entrantes y crea un
 * hilo {@link Process_Manager} por cada cliente conectado.
 *
 * Los mensajes recibidos por un cliente se reenvían al resto de clientes
 * conectados utilizando un mecanismo de broadcast.
 *
 * La lista de clientes se gestiona con {@link CopyOnWriteArrayList} para
 * garantizar la seguridad en entornos multihilo.
 *
 * @author Ciso
 */
public class SocketTCPServer {

    /**
     * Socket del servidor que escucha conexiones entrantes.
     */
    private final ServerSocket serverSocket;

    /**
     * Lista compartida de clientes conectados al servidor.
     */
    public static final List<Process_Manager> clients = new CopyOnWriteArrayList<>(); // Es más seguro que ArrayList para manejar hilos

    /**
     * Constructor que inicia el servidor en el puerto especificado. Escucha
     * continuamente conexiones entrantes y lanza un hilo para cada cliente.
     *
     * @param port Puerto en el que el servidor escuchará conexiones.
     * @throws IOException si ocurre un error al abrir el socket del servidor.
     */
    public SocketTCPServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println(" (Servidor) A la espera de conexiones.");
        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println(" (Servidor) Conexión establecida.");
            Process_Manager clientHandler = new Process_Manager(socket);
            clients.add(clientHandler); // Añade el cliente a la lista global
            clientHandler.start(); // Inicia el hilo de gestión del cliente
        }
    }

    /**
     * Cierra el servidor liberando el puerto ocupado.
     *
     * @throws IOException si ocurre un error al cerrar el socket.
     */
    public void stop() throws IOException {
        System.out.println(" (Servidor) Cerrando todo.");
        serverSocket.close();
    }

    /**
     * Envía un mensaje a todos los clientes conectados excepto al remitente.
     *
     * @param msg Mensaje a enviar.
     * @param pm Cliente que ha enviado el mensaje.
     */
    public static void broadcast(String msg, Process_Manager pm) {
        for (Process_Manager client : clients) {
            if (client != pm) {
                client.sendMessage(msg);
            }
        }
    }

    /**
     * Método principal que lanza el servidor en el puerto 50000.
     *
     * @param args Argumentos de línea de comandos (no se usan).
     */
    public static void main(String[] args) {
        try {
            SocketTCPServer server = new SocketTCPServer(50000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
