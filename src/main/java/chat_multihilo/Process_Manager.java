package chat_multihilo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

/**
 * Clase que gestiona la comunicación con un cliente específico en el chat.
 * Cada instancia de esta clase se ejecuta en su propio hilo, permitiendo una
 * comunicación simultánea con múltiples clientes.
 * 
 * Al iniciar, se solicita al cliente que ingrese un nombre, el cual será utilizado
 * para identificar los mensajes enviados por ese cliente.
 * 
 * Los mensajes que recibe el cliente se reenvían a todos los demás clientes
 * conectados mediante el método estático {@link SocketTCPServer#broadcast(String, Process_Manager)}.
 * 
 * Además, proporciona el método {@link #sendMessageToThisClient(String)} para enviar
 * mensajes directos a este cliente en particular.
 * 
 * El ciclo de vida del hilo de este proceso es el siguiente:
 * - Solicita el nombre del cliente al conectarse.
 * - Anuncia su entrada al chat.
 * - Escucha los mensajes del cliente y los difunde a los demás.
 * - Maneja la desconexión y notifica al resto de los usuarios.
 * 
 * @author Ciso
 */
public class Process_Manager extends Thread {

    /**
     * Socket asociado a este cliente.
     */
    private final Socket socket;

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
    * Constructor que recibe el socket del cliente y asigna un nombre temporal
    * basado en el puerto del socket. Este nombre es utilizado hasta que el cliente
    * ingrese su propio nombre.
    *
    * @param socket Socket conectado al cliente.
    */
    public Process_Manager(Socket socket) {
        this.socket = socket;
        this.user = "Anónimo#" + this.socket.getPort();
    }

    /**
     * Envía un mensaje de texto al cliente asociado a este hilo.
     *
     * @param msg Mensaje a enviar.
     */
    public void sendMessageToThisClient(String msg) {
        if (out != null) {
            out.println(msg);
        }
    }

    /**
     * Envía un mensaje de texto a todos los clientes conectados, excepto al
     * cliente que lo envió.
     *
     * @param msg El mensaje a enviar a todos los clientes.
     * @param pm El cliente que envió el mensaje (no recibirá el mensaje).
     */
    public void sendMessageToAllClients(String msg, Process_Manager pm) {
        SocketTCPServer.broadcast(msg, pm);
    }

    /**
     * Método principal del hilo que gestiona el flujo de la comunicación con el cliente.
     * Este método realiza lo siguiente:
     * - Solicita el nombre del cliente.
     * - Anuncia su entrada al chat.
     * - Escucha los mensajes del cliente y los reenvía a los demás clientes.
     * - Maneja la desconexión del cliente y notifica al resto.
     */
    @Override
    public void run() {
        try {
            openStreams();

            identifyUser();

            // Notifica la bienvenida al cliente
            sendMessageToThisClient("Bienvenido al chat, " + user + ". \nPuedes salir en cualquier momento escribiendo '/salir'.");
            sendMessageToAllClients(" [Servidor]: " + user + " se ha unido al chat.", this);

            // Escucha los mensajes enviados por el cliente y los reenvía a todos
            String msg;
            while ((msg = in.readLine()) != null && !msg.equalsIgnoreCase("/salir")) {
                sendMessageToThisClient(user + ": " + msg);
                sendMessageToAllClients(user + ": " + msg, this);
            }
        } catch (SocketException e) {
            System.err.println(" [Servidor] Un cliente se desconectó de forma inesperada: " + user);
        } catch (IOException e) {
            System.err.println("Error con cliente " + user + ": " + e.getMessage());
        } finally {
            try {
                SocketTCPServer.clients.remove(this);
                sendMessageToAllClients(" [Servidor]: " + user + " ha salido del chat.", this);
                closeStreams();
                socket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Solicita al cliente que ingrese su nombre. Si el cliente no ingresa un
     * nombre o se desconecta, se asigna un nombre por defecto.
     *
     * @throws IOException Si ocurre un error al leer el nombre del cliente.
     */
    private void identifyUser() throws IOException {
        sendMessageToThisClient("Introduce tu nombre: ");
        user = in.readLine();
    }

    /**
     * Cierra los flujos de entrada y salida utilizados para la comunicación
     * con el cliente.
     *
     * @throws IOException Si ocurre un error al cerrar los flujos.
     */
    private void closeStreams() throws IOException {
        in.close();
        out.close();
    }

    /**
     * Abre los flujos de entrada y salida para la comunicación con el cliente.
     *
     * @throws IOException Si ocurre un error al abrir los flujos.
     */
    private void openStreams() throws IOException {
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
    }
}
