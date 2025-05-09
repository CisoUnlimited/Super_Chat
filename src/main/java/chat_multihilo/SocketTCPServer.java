package chat_multihilo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class SocketTCPServer {

    private final ServerSocket serverSocket;

    public static final Map<String, List<Process_Manager>> chatRooms = new ConcurrentHashMap<>();

    public SocketTCPServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        createRooms();
        acceptClientConnections();
        stop();
    }

    private void createRooms() {
        chatRooms.put("general", new CopyOnWriteArrayList<>());
        chatRooms.put("PSPRO", new CopyOnWriteArrayList<>());
        chatRooms.put("DEINT", new CopyOnWriteArrayList<>());
        chatRooms.put("PMDMO", new CopyOnWriteArrayList<>());
        chatRooms.put("ACDAT", new CopyOnWriteArrayList<>());
    }

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

    public void stop() throws IOException {
        System.out.println(" [Servidor] Cerrando todo.");
        serverSocket.close();
    }

    public static boolean roomExists(String room) {
        return chatRooms.containsKey(room);
    }

    public static String getAvailableRooms() {
        if (chatRooms.isEmpty()) {
            return " [Servidor] No hay salas disponibles.";
        }
        StringBuilder roomList = new StringBuilder();
        chatRooms.forEach((room, clients) -> roomList.append("   ").append(room).append("\n"));
        return roomList.toString();
    }

    public static void addClientToRoom(String roomName, Process_Manager client) {
        chatRooms.computeIfAbsent(roomName, k -> new CopyOnWriteArrayList<>()).add(client);
    }

    public static void removeClientFromRoom(String roomName, Process_Manager client) {
        List<Process_Manager> roomClients = chatRooms.get(roomName);
        if (roomClients != null) {
            roomClients.remove(client);
        }
    }

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

    public static void main(String[] args) {
        try {
            SocketTCPServer server = new SocketTCPServer(50000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
