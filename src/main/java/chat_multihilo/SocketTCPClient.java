package chat_multihilo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * Clase que representa un cliente TCP para un sistema de chat multicliente.
 *
 * Este cliente se conecta a un servidor mediante un {@link Socket}, permite
 * enviar mensajes introducidos por el usuario desde consola, y recibe mensajes
 * del servidor en tiempo real, incluyendo los enviados por otros clientes.
 *
 * La comunicación permanece activa hasta que el usuario introduce el comando
 * {@code /salir} o finaliza la ejecución manualmente (por ejemplo, con Ctrl +
 * C).
 *
 * Este cliente utiliza codificación UTF-8 para garantizar la compatibilidad con
 * caracteres especiales.
 *
 * @author Ciso
 */
public class SocketTCPClient {

    /**
     * Dirección IP del servidor al que se conectará el cliente.
     */
    private final String serverIP;

    /**
     * Puerto del servidor al que se conectará el cliente.
     */
    private final int serverPort;

    /**
     * Socket que representa la conexión activa con el servidor.
     */
    private Socket socket;

    /**
     * Flujo de entrada para recibir mensajes del servidor.
     */
    private BufferedReader in;

    /**
     * Flujo de salida para enviar datos al servidor.
     */
    private PrintWriter out;

    /**
     * Crea una instancia del cliente TCP especificando la dirección IP y el
     * puerto del servidor.
     *
     * @param serverIP Dirección IP del servidor.
     * @param serverPort Puerto en el que el servidor está escuchando.
     */
    public SocketTCPClient(String serverIP, int serverPort) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
    }

    /**
     * Inicia la conexión con el servidor. Crea un hilo para escuchar mensajes
     * entrantes del servidor de forma concurrente y gestiona el envío de
     * mensajes desde la entrada estándar (teclado).
     *
     * @throws IOException Si ocurre un error al establecer la conexión o
     * durante la comunicación.
     */
    public void start() throws IOException {
        //tryConnectionWithServer(); Esto no funciona correctamente y no se por qué
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
                System.out.println("Conexión cerrada por el servidor.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        // Bucle principal para leer mensajes del usuario
        BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
        String msg;
        while ((msg = keyboard.readLine()) != null && !msg.equalsIgnoreCase("/salir")) {
            sendMessage(msg);
        }

        // Finaliza la conexión
        stop();
    }

    private void tryConnectionWithServer() throws IOException { // No funciona y no se por qué
        long startTime = System.currentTimeMillis();
        boolean connected = false;
        int c = 0;

        // Intentar conexión ininterrumpidamente durante 10 segundos
        System.out.println(" [Cliente] Estableciendo Conexión...");
        while (System.currentTimeMillis() - startTime < 10000 && !connected) {
            try {
                socket = new Socket(serverIP, serverPort);
                connected = true; // Conexión exitosa, salir del bucle
                System.out.println(" [Cliente] Conexión Establecida.");
            } catch (IOException e) {
                // Si la conexión falla, intentamos nuevamente
                System.out.println("Intento fallido..." + c++);
            }
        }

        // Si no se ha logrado conectar después de 10 segundos
        if (!connected) {
            System.out.println("No se pudo establecer conexión después de 10 segundos.");
            // Preguntar al usuario si quiere intentar nuevamente
            System.out.println("¿Deseas intentar nuevamente? (s/n): ");
            BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
            String userInput;
            try {
                userInput = keyboard.readLine();
            } catch (IOException ex) {
                userInput = "no";
            }

            if (userInput.equalsIgnoreCase("n")) {
                System.out.println(" [Cliente] Terminando intento de conexión.");
                throw new IOException("Conexión no establecida por el usuario.");
            } else {
                System.out.println("Reintentando conexión...");
                tryConnectionWithServer(); // Volver a intentar la conexión
            }
        }
    }

    /**
     * Envía un mensaje al servidor.
     *
     * @param msg Mensaje a enviar.
     */
    private void sendMessage(String msg) {
        out.println(msg);
    }

    /**
     * Cierra todos los recursos asociados a la conexión del cliente: socket y
     * flujos.
     *
     * @throws IOException Si ocurre un error al cerrar alguno de los recursos.
     */
    public void stop() throws IOException {
        System.out.println(" [Cliente] Cerrando Conexión.");
        in.close();
        out.close();
        socket.close();
        System.out.println(" [Cliente] Conexión Cerrada.");
    }

    /**
     * Método principal que crea una instancia del cliente y la conecta al
     * servidor especificado por IP y puerto.
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
