package chat_multihilo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Clase que implementa un servidor TCP multicliente para un sistema de chat.
 * Utiliza un {@link ServerSocket} para aceptar conexiones entrantes y crea un
 * hilo {@link Process_Manager} para cada cliente conectado.
 *
 * Los mensajes recibidos por un cliente se reenvían a todos los demás clientes
 * conectados mediante un mecanismo de broadcast.
 *
 * La lista de clientes se gestiona con {@link CopyOnWriteArrayList} para
 * garantizar la seguridad en entornos multihilo, evitando inconsistencias
 * cuando varios hilos acceden simultáneamente a la lista.
 *
 * @author Ciso
 */
public class SocketTCPServer {

    /**
     * Socket del servidor que escucha conexiones entrantes.
     */
    private final ServerSocket serverSocket;

    /**
     * Lista compartida de clientes conectados al servidor. Se utiliza
     * {@link CopyOnWriteArrayList} para garantizar la seguridad de la lista en
     * un entorno multihilo.
     */
    public static final List<Process_Manager> clients = new CopyOnWriteArrayList<>();

    /**
     * Constructor que inicializa el servidor TCP en el puerto especificado. El
     * servidor comienza a escuchar continuamente las conexiones entrantes en el
     * puerto dado. Cada vez que se establece una conexión, se crea un nuevo
     * hilo {@link Process_Manager} para manejar al cliente y se agrega a la
     * lista de clientes activos.
     *
     * Además, registra un {@link Runtime#addShutdownHook(Thread)} para asegurar
     * que el servidor se cierre adecuadamente al recibir una señal de
     * terminación (por ejemplo, al presionar Ctrl+C o detener el proceso).
     *
     * @param port El puerto en el que el servidor escuchará las conexiones
     * entrantes.
     * @throws IOException Si ocurre un error al abrir el socket del servidor o
     * al aceptar las conexiones de los clientes.
     */
    public SocketTCPServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        acceptClientConnections();
        // Agregar un ShutdownHook para manejar la terminación controlada
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                stop();
            } catch (IOException e) {
                System.err.println("Error al cerrar el servidor: " + e.getMessage());
            }
        }));
    }

    /**
     * Acepta conexiones entrantes de forma indefinida. Cada vez que se
     * establece una conexión, se lanza un nuevo hilo {@link Process_Manager}
     * para manejar al cliente. Cada cliente se añade a la lista compartida
     * {@code clients}.
     *
     * @throws IOException Si ocurre un error al aceptar una conexión.
     */
    private void acceptClientConnections() throws IOException {
        System.out.println(" [Servidor] A la espera de conexiones.");
        while (true) {
            try {
                // Acepta la conexión entrante
                Socket socket = serverSocket.accept();
                System.out.println(" [Servidor] Conexión establecida con: " + socket.getRemoteSocketAddress());

                // Crea un manejador de cliente y lo inicia en un hilo
                Process_Manager clientHandler = new Process_Manager(socket);
                clients.add(clientHandler); // Añade el cliente a la lista global
                clientHandler.start(); // Inicia el hilo de gestión del cliente
            } catch (IOException e) {
                System.err.println(" [Servidor] Error al aceptar conexión: " + e.getMessage());
            }
        }
    }

    /**
     * Envía un mensaje a todos los clientes conectados, excepto al cliente que
     * lo envió. Este método se utiliza para realizar un broadcast de mensajes
     * entre los clientes.
     *
     * @param msg Mensaje que se enviará a los demás clientes.
     * @param pm Cliente que ha enviado el mensaje. Este cliente no recibirá el
     * mensaje.
     */
    public static void broadcast(String msg, Process_Manager pm) {
        for (Process_Manager client : clients) {
            if (client != pm) {
                client.sendMessageToThisClient(msg);
            }
        }
    }

    /**
     * Cierra el servidor y libera el puerto ocupado. Este método debe ser
     * llamado cuando se desee detener el servidor.
     *
     * @throws IOException Si ocurre un error al cerrar el socket del servidor.
     */
    public void stop() throws IOException {
        System.out.println(" [Servidor] Cerrando todo.");
        serverSocket.close();
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
