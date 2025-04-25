package chat_multihilo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Clase que gestiona la comunicación con un cliente específico. Cada instancia
 * se ejecuta en su propio hilo, permitiendo una comunicación simultánea con
 * múltiples clientes.
 *
 * Al iniciar, se le pide al cliente que introduzca un nombre, el cual se
 * utiliza para identificar los mensajes enviados por él.
 *
 * Recibe mensajes desde el cliente y los reenvía al resto de clientes mediante
 * el método estático
 * {@link SocketTCPServer#broadcast(String, Process_Manager)}.
 *
 * También proporciona un método {@link #sendMessage(String)} para enviar
 * mensajes directamente a este cliente.
 *
 * @author Ciso
 */
public class Process_Manager extends Thread {

    /**
     * Socket asociado a este cliente.
     */
    private Socket socket;

    /**
     * Flujo de entrada para leer mensajes del cliente.
     */
    private BufferedReader in;

    /**
     * Flujo de salida para enviar mensajes al cliente.
     */
    private PrintWriter out;

    /**
     * Nombre del cliente, proporcionado al conectarse.
     */
    private String user;

    /**
     * Constructor que recibe el socket del cliente.
     *
     * @param socket Socket conectado al cliente.
     */
    public Process_Manager(Socket socket) {
        this.socket = socket;
    }

    /**
     * Envía un mensaje de texto al cliente asociado a este hilo.
     *
     * @param message Mensaje a enviar.
     */
    public void sendMessage(String msg) {
        if (out != null) {
            out.println(msg);
        }
    }

    /**
     * Método principal del hilo.
     *
     * - Solicita el nombre del cliente. - Anuncia su entrada al chat. - Escucha
     * mensajes del cliente y los difunde. - Maneja la desconexión y notifica al
     * resto.
     */
    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println("Introduce tu nombre: ");
            user = in.readLine();

            out.println("Bienvenido al chat, " + user + ".");

            SocketTCPServer.broadcast("[Servidor]: " + user + " se ha unido al chat.", this);

            String msg;
            while ((msg = in.readLine()) != null) {
                if (msg.equalsIgnoreCase("/salir")) {
                    break;
                }
                System.out.println(user + ": " + msg);
                SocketTCPServer.broadcast(user + ": " + msg, this);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            SocketTCPServer.clients.remove(this);
            SocketTCPServer.broadcast("[Servidor]: " + user + " ha salido del chat.", this);
            try {
                socket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
