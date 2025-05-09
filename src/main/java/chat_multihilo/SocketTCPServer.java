package chat_multihilo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Clase que representa un servidor TCP multihilo para un sistema de chat.
 * Escucha conexiones entrantes en un puerto determinado y gestiona múltiples
 * salas de chat. Cada cliente es gestionado por una instancia de
 * {@code Process_Manager} en un hilo separado.
 *
 * Las salas de chat se almacenan en un mapa concurrente para permitir acceso
 * seguro desde múltiples hilos.
 *
 * Salas por defecto: general, PSPRO, DEINT, PMDMO, ACDAT.
 *
 * @author Ciso
 */
public final class SocketTCPServer {

    /**
     * Socket del servidor para aceptar conexiones entrantes.
     */
    private final ServerSocket serverSocket;

    /**
     * Mapa que asocia nombres de salas con listas de clientes conectados a cada
     * sala.
     */
    public static final Map<String, List<Process_Manager>> chatRooms = new ConcurrentHashMap<>();

    /**
     * Constructor del servidor que inicia la escucha en el puerto especificado,
     * crea las salas predeterminadas, acepta conexiones de clientes y
     * posteriormente cierra el servidor.
     *
     * @param port Puerto en el que se iniciará el servidor.
     * @throws IOException si ocurre un error al abrir el socket del servidor.
     */
    public SocketTCPServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        createRooms();
        acceptClientConnections();
        stop();
    }

    /**
     * Crea las salas de chat predeterminadas.
     */
    private void createRooms() {
        chatRooms.put("general", new CopyOnWriteArrayList<>());
        chatRooms.put("PSPRO", new CopyOnWriteArrayList<>());
        chatRooms.put("DEINT", new CopyOnWriteArrayList<>());
        chatRooms.put("PMDMO", new CopyOnWriteArrayList<>());
        chatRooms.put("ACDAT", new CopyOnWriteArrayList<>());
    }

    /**
     * Acepta conexiones de clientes de manera indefinida. Cada cliente se
     * gestiona en un nuevo hilo mediante una instancia de
     * {@code Process_Manager}.
     *
     * @throws IOException si ocurre un error al aceptar una conexión.
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

                clientHandler.start(); // Inicia el hilo de gestión del cliente
            } catch (IOException e) {
                System.err.println(" [Servidor] Error al aceptar conexión: " + e.getMessage());
            }
        }
    }

    /**
     * Cierra el socket del servidor, deteniendo así la recepción de nuevas
     * conexiones.
     *
     * @throws IOException si ocurre un error al cerrar el socket.
     */
    public void stop() throws IOException {
        System.out.println(" [Servidor] Cerrando todo.");
        serverSocket.close();
    }

    /**
     * Verifica si una sala de chat con el nombre dado existe.
     *
     * @param room Nombre de la sala.
     * @return {@code true} si la sala existe; {@code false} en caso contrario.
     */
    public static boolean roomExists(String room) {
        return chatRooms.containsKey(room);
    }

    /**
     * Devuelve una cadena con la lista de todas las salas disponibles.
     *
     * @return String con los nombres de las salas o un mensaje si no hay salas.
     */
    public static String getAvailableRooms() {
        if (chatRooms.isEmpty()) {
            return " [Servidor] No hay salas disponibles.";
        }
        StringBuilder roomList = new StringBuilder();
        chatRooms.forEach((room, clients) -> roomList.append("   ").append(room).append("\n"));
        return roomList.toString();
    }

    /**
     * Añade un cliente a una sala. Si la sala no existe, se crea.
     *
     * @param roomName Nombre de la sala.
     * @param client Cliente a añadir.
     */
    public static void addClientToRoom(String roomName, Process_Manager client) {
        chatRooms.computeIfAbsent(roomName, k -> new CopyOnWriteArrayList<>()).add(client);
    }

    /**
     * Elimina un cliente de una sala.
     *
     * @param roomName Nombre de la sala.
     * @param client Cliente a eliminar.
     */
    public static void removeClientFromRoom(String roomName, Process_Manager client) {
        List<Process_Manager> roomClients = chatRooms.get(roomName);
        if (roomClients != null) {
            roomClients.remove(client);
        }
    }

    /**
     * Envía un mensaje a todos los clientes de una sala, excepto al remitente.
     *
     * @param roomName Nombre de la sala.
     * @param msg Mensaje a enviar.
     * @param sender Cliente que envía el mensaje.
     */
    public static void broadcastToRoom(String roomName, String msg, Process_Manager sender) {
        List<Process_Manager> roomClients = chatRooms.get(roomName);
        if (roomName != null) {
            for (Process_Manager client : roomClients) {
                if (client != sender) {
                    client.sendMessageToThisClient(msg);
                }
            }
        }
    }

    /**
     * Método principal que inicia el servidor en el puerto 50000.
     *
     * @param args Argumentos de la línea de comandos (no usados).
     */
    public static void main(String[] args) {
        try {
            SocketTCPServer server = new SocketTCPServer(50000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
