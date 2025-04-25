package chat_multihilo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Clase que representa un cliente TCP para el chat multihilo.
 *
 * Establece una conexión con el servidor, envía mensajes introducidos por el
 * usuario y muestra en tiempo real los mensajes que recibe desde el servidor
 * (incluidos los de otros clientes).
 *
 * La comunicación se mantiene activa hasta que el usuario finaliza la ejecución
 * manualmente (Ctrl + C).
 *
 * @author Ciso
 */
public class SocketTCPClient {

    /**
     * Dirección IP del servidor al que se desea conectar.
     */
    private final String serverIP;

    /**
     * Puerto del servidor al que se desea conectar.
     */
    private final int serverPort;

    /**
     * Socket que representa la conexión con el servidor.
     */
    private Socket socket;

    /**
     * Flujo de entrada para recibir datos del servidor.
     */
    private BufferedReader in;

    /**
     * Flujo de salida para enviar datos al servidor.
     */
    private PrintWriter out;

    /**
     * Constructor que inicializa la IP y el puerto del servidor.
     *
     * @param serverIP Dirección IP del servidor.
     * @param serverPort Puerto del servidor.
     */
    public SocketTCPClient(String serverIP, int serverPort) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
    }

    /**
     * Inicia la conexión con el servidor, lanza un hilo que recibe mensajes del
     * servidor y permite enviar mensajes escritos por el usuario.
     *
     * @throws IOException Si ocurre un error durante la conexión o la
     * comunicación.
     */
    public void start() throws IOException {
        System.out.println("(Cliente) Estableciendo Conexión.");
        socket = new Socket(serverIP, serverPort);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        System.out.println("(Cliente) Conexión Establecida.");

        new Thread(() -> {
            try {
                String msgFromServer;
                while ((msgFromServer = in.readLine()) != null) {
                    System.out.println(msgFromServer);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
        String msg;
        while ((msg = keyboard.readLine()) != null) {
            out.println(msg);
        }
    }

    /**
     * Cierra todos los recursos utilizados por el cliente: flujos y socket.
     *
     * @throws IOException Si ocurre un error al cerrar alguno de los recursos.
     */
    public void stop() throws IOException {
        System.out.println("(Cliente) Cerrando Conexión.");
        in.close();
        out.close();
        socket.close();
        System.out.println("(Cliente) Conexión Cerrada.");
    }

    /**
     * Método principal que inicia el cliente y se conecta al servidor
     * especificado.
     *
     * @param args Argumentos de línea de comandos (no se utilizan).
     */
    public static void main(String[] args) {
        try {
            SocketTCPClient client = new SocketTCPClient("10.208.6.1", 50000);
            client.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
