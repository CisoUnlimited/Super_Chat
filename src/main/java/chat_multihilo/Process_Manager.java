package chat_multihilo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

/**
 * Clase encargada de gestionar la comunicación con un cliente individual en el
 * servidor de chat multihilo. Cada instancia se ejecuta en un hilo
 * independiente para gestionar las interacciones del cliente.
 *
 * Maneja la identificación del usuario, el procesamiento de comandos y el
 * envío/recepción de mensajes. También gestiona su incorporación a una sala de
 * chat y su desconexión.
 *
 * @author Ciso
 */
public class Process_Manager extends Thread {

    /**
     * Socket asociado al cliente.
     */
    private final Socket socket;

    /**
     * Flujo de entrada desde el cliente.
     */
    private BufferedReader in;

    /**
     * Flujo de salida hacia el cliente.
     */
    private PrintWriter out;

    /**
     * Nombre de usuario del cliente.
     */
    private String user;

    /**
     * Sala de chat actual del cliente.
     */
    private String currentRoom;

    /**
     * Constructor que inicializa el socket del cliente.
     * Asigna un nombre de usuario temporal basado en el puerto del socket.
     * 
     * @param socket Socket del cliente conectado.
     */
    public Process_Manager(Socket socket) {
        this.socket = socket;
        this.user = "Anónimo#" + this.socket.getPort();
    }

    /**
     * Método principal del hilo que gestiona la comunicación con el cliente.
     * Inicializa flujos, solicita nombre de usuario, muestra comandos,
     * y entra en un bucle para procesar mensajes y comandos.
     */
    @Override
    public void run() {
        try {
            openStreams();
            identifyUser();
            sendMessageToThisClient("\n [Servidor] Bienvenido, " + user + ".");
            showCommands();
            sendMessageToThisClient(" [Servidor] Para empezar a usar el chat, debes seleccionar una sala escribiendo '/join nombreDeSala'");
            showRooms();

            String msg;
            while ((msg = in.readLine()) != null) {
                inspectMsg(msg);
            }

        } catch (SocketException e) {
            System.err.println(" [Servidor] Un cliente se desconectó de forma inesperada: " + user);
        } catch (IOException e) {
            System.err.println(" [Servidor] Error con cliente " + user + ": " + e.getMessage());
        } finally {
            try {
                SocketTCPServer.removeClientFromRoom(currentRoom, this);
                sendMessageToRoom(" [Servidor]: " + user + " ha salido de la sala.");
                closeStreams();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {

            }
        }
    }

    /**
     * Solicita al cliente que introduzca su nombre de usuario.
     * 
     * @throws IOException si hay un error de entrada/salida al leer del cliente.
     */
    private void identifyUser() throws IOException {
        sendMessageToThisClient(" [Servidor] Introduce tu nombre: ");
        user = in.readLine();
    }

    /**
     * Inspecciona el mensaje recibido: si comienza con "/", lo interpreta como comando.
     * En otro caso, lo retransmite a la sala actual.
     * 
     * @param msg Mensaje recibido del cliente.
     * @throws IOException si hay error al procesar el mensaje.
     */
    private void inspectMsg(String msg) throws IOException {
        if (msg.startsWith("/")) {
            runCommand(msg.substring(1));
        } else if (currentRoom != null) {
            sendMessageToThisClient(user + ": " + msg);
            sendMessageToRoom(user + ": " + msg);
        }
    }

    /**
     * Envía un mensaje exclusivamente a este cliente.
     * 
     * @param msg Mensaje a enviar.
     */
    public void sendMessageToThisClient(String msg) {
        if (out != null) {
            out.println(msg);
        } else {
            System.err.println(" [Servidor] Error al enviar mensaje. Salida no disponible.");
        }
    }

    /**
     * Envía un mensaje a todos los clientes conectados a la misma sala.
     * 
     * @param msg Mensaje a enviar a la sala.
     */
    public void sendMessageToRoom(String msg) {
        SocketTCPServer.broadcastToRoom(currentRoom, msg, this);
    }

    /**
     * Interpreta y ejecuta un comando recibido desde el cliente.
     * 
     * @param command Comando a ejecutar (sin la barra "/").
     * @throws IOException si hay error al cambiar de sala o comunicarse con el cliente.
     */
    private void runCommand(String command) throws IOException {
        String room = "";
        if (command.startsWith("join ")) {
            room = command.substring(5);
            command = "join";
        }
        switch (command) {
            case "help":
                showCommands();
                break;
            case "rooms":
                showRooms();
                sendMessageToThisClient(" [Servidor] Recuerda, '/join [nombre de la sala]' para unirte.");
                break;
            case "join":
                joinRoom(room);
                break;
            case "exit":
                exit();
                break;
            default:
                out.println(" [Servidor] '/" + command + "' no es un comando válido.");
                showCommands();
                break;
        }
    }

    /**
     * Muestra la lista de comandos disponibles al cliente.
     */
    private void showCommands() {
        sendMessageToThisClient(" [Servidor] Comandos disponibles:");
        sendMessageToThisClient("   /help - Muestra esta guía");
        sendMessageToThisClient("   /rooms - Muestra las salas disponibles");
        sendMessageToThisClient("   /join [nombre de la sala] - Permite seleccionar y unirse a una sala");
        sendMessageToThisClient("   /exit - Salir del chat");
    }

    /**
     * Muestra al cliente las salas disponibles en el servidor.
     */
    private void showRooms() {
        sendMessageToThisClient(" [Servidor] Salas disponibles:");
        sendMessageToThisClient(SocketTCPServer.getAvailableRooms());
    }

    /**
     * Permite al cliente unirse a una sala existente.
     * 
     * @param room Nombre de la sala a la que desea unirse.
     * @throws IOException si hay error al cambiar de sala.
     */
    private void joinRoom(String room) throws IOException {
        if (SocketTCPServer.roomExists(room)) {
            currentRoom = room;
            SocketTCPServer.addClientToRoom(currentRoom, this);
            sendMessageToThisClient(" [Servidor] Te has unido a la sala: " + currentRoom);
            SocketTCPServer.broadcastToRoom(currentRoom, " [Servidor] " + this.user + " se ha unido a la sala.", this);
        } else {
            sendMessageToThisClient(" [Servidor] La sala '" + room + "' no existe. Por favor, elige otra.");
            showRooms();
        }
    }

    /**
     * Desconecta al cliente del servidor y cierra su socket.
     */
    private void exit() {
        if (currentRoom != null) {
            SocketTCPServer.removeClientFromRoom(currentRoom, this);
        }
        sendMessageToThisClient(" [Servidor] Gracias por usar el chat, ¡hasta pronto!");
        try {
            closeStreams();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Abre los flujos de entrada y salida para el cliente.
     * 
     * @throws IOException si ocurre un error al abrir los flujos.
     */
    private void openStreams() throws IOException {
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
    }

    /**
     * Cierra los flujos de entrada y salida del cliente.
     * 
     * @throws IOException si ocurre un error al cerrarlos.
     */
    private void closeStreams() throws IOException {
        in.close();
        out.close();
    }
}
