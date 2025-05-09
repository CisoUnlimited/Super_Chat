package chat_multihilo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;

public class Process_Manager extends Thread {

    private final Socket socket;

    private BufferedReader in;

    private PrintWriter out;

    private String user;

    private String currentRoom;

    public Process_Manager(Socket socket) {
        this.socket = socket;
        this.user = "Anónimo#" + this.socket.getPort();
    }

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

    private void identifyUser() throws IOException {
        sendMessageToThisClient(" [Servidor] Introduce tu nombre: ");
        user = in.readLine();
    }

    private void inspectMsg(String msg) throws IOException {
        if (msg.startsWith("/")) {
            runCommand(msg.substring(1));
        } else if (currentRoom != null) {
            sendMessageToThisClient(user + ": " + msg);
            sendMessageToRoom(user + ": " + msg);
        }
    }

    public void sendMessageToThisClient(String msg) {
        if (out != null) {
            out.println(msg);
        } else {
            System.err.println(" [Servidor] Error al enviar mensaje. Salida no disponible.");
        }
    }

    public void sendMessageToRoom(String msg) {
        SocketTCPServer.broadcastToRoom(currentRoom, msg, this);
    }

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
                sendMessageToThisClient(" [Servidor] Recuerda, '/join nombreDeSala' para unirte.");
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

    private void showCommands() {
        sendMessageToThisClient(" [Servidor] Comandos disponibles:");
        sendMessageToThisClient("   /help - Muestra esta guía");
        sendMessageToThisClient("   /rooms - Muestra las salas disponibles");
        sendMessageToThisClient("   /join [nombre de la sala] - Permite seleccionar y unirse a una sala");
        sendMessageToThisClient("   /exit - Salir del chat");
    }

    private void showRooms() {
        sendMessageToThisClient(" [Servidor] Salas disponibles:");
        sendMessageToThisClient(SocketTCPServer.getAvailableRooms());
    }

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

    private void openStreams() throws IOException {
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
    }

    private void closeStreams() throws IOException {
        in.close();
        out.close();
    }
}
