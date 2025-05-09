package chat_multihilo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Clase que representa un cliente TCP para conectarse a un servidor de chat
 * multihilo. Establece una conexión con el servidor, permite enviar mensajes y
 * recibe mensajes en un hilo separado. La comunicación se realiza usando
 * codificación UTF-8.
 *
 * El cliente se conecta al servidor indicado por IP y puerto, y mantiene un
 * bucle de entrada para enviar mensajes desde la consola.
 *
 * @author Ciso
 */
public class SocketTCPClient {

    /**
     * Dirección IP del servidor
     */
    private final String serverIP;

    /**
     * Puerto del servidor
     */
    private final int serverPort;

    /**
     * Socket de conexión con el servidor
     */
    private Socket socket;

    /**
     * Flujo de entrada desde el servidor
     */
    private BufferedReader in;

    /**
     * Flujo de salida hacia el servidor
     */
    private PrintWriter out;

    /**
     * Constructor del cliente.
     *
     * @param serverIP Dirección IP del servidor.
     * @param serverPort Puerto del servidor.
     */
    public SocketTCPClient(String serverIP, int serverPort) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
    }

    /**
     * Inicia la conexión con el servidor, escucha mensajes entrantes en un hilo
     * independiente y permite enviar mensajes escritos por teclado.
     *
     * @throws IOException si hay un problema de entrada/salida al conectarse o
     * comunicarse.
     */
    public void start() throws IOException {
        socket = new Socket(serverIP, serverPort);
        System.out.println(" [Cliente] Conexión Establecida.");
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

        // Hilo para recibir mensajes del servidor
        new Thread(() -> {
            try {
                String msgFromServer;
                while ((msgFromServer = in.readLine()) != null) {
                    System.out.println(msgFromServer);
                }
                System.out.println(" [Cliente] Conexión cerrada por el servidor.");
            } catch (IOException e) {
                System.out.println(" [Cliente] ¡Vuelve pronto!");
            }
        }).start();

        // Bucle principal para leer mensajes del usuario
        BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
        String msg;
        boolean salir = false;
        while (!salir && (msg = keyboard.readLine()) != null) {
            sendMessage(msg);
        }
        // Finaliza la conexión
        stop();
    }

    /**
     * Envía un mensaje al servidor.
     *
     * @param msg Mensaje que se quiere enviar.
     */
    private void sendMessage(String msg) {
        out.println(msg);
    }

    /**
     * Cierra la conexión con el servidor y libera los recursos asociados.
     *
     * @throws IOException si hay un problema al cerrar los flujos o el socket.
     */
    public void stop() throws IOException {
        System.out.println(" [Cliente] Cerrando Conexión.");
        socket.close();
        in.close();
        out.close();
        System.out.println(" [Cliente] Conexión Cerrada.");
    }

    /**
     * Método principal para lanzar el cliente y conectarse al servidor
     * localhost en el puerto 50000.
     *
     * @param args Argumentos de línea de comandos (no se utilizan).
     */
    public static void main(String[] args) {
        try {
            SocketTCPClient client = new SocketTCPClient("localhost", 50000);
            client.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
