/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package dam.super_chat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author Propietario
 */
public class Chat_Server {

    private ServerSocket serverSocket;
    private Socket socket;
    private InputStream is;
    private OutputStream os;

    public Chat_Server(int puerto) throws IOException {
        serverSocket = new ServerSocket(puerto);
    }

    public void start() throws IOException {
        System.out.println(" (Servidor) Esperando conexiones...");
        socket = serverSocket.accept(); // Queda a la espera de una conexion
        is = socket.getInputStream();   // Abre flujos de lectura
        os = socket.getOutputStream();  // Abre flujos de escritura
        System.out.println(" (Servidor) Conexión establecida con cliente " + socket.getRemoteSocketAddress());
    }

    public void stop() throws IOException {
        System.out.println(" (Servidor) Cerrando conexiones...");
        is.close();             // Cierra flujos de lectura
        os.close();             // Cierra flujos de escritura
        socket.close();         // Cierra el Socket
        serverSocket.close();   // Cierra el ServerSocket
        System.out.println(" (Servidor) Conexiones cerradas.");
    }

    public static void main(String[] args) {

        try {
            Chat_Server server = new Chat_Server(50000);
            server.start();
            System.out.println("Mensaje del cliente: " + server.is.read());
            server.os.write(200);
            server.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
