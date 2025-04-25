/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */
package dam.super_chat;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

/**
 *
 * @author Propietario
 */
public class Chat_Server {

    // Objetos para la conexion
    private final ServerSocket serverSocket;
    private Socket socket;
    private InputStream is;
    private OutputStream os;

    // Objetos para envío y recepción de cadenas
    private InputStreamReader isr;
    private BufferedReader br;
    private PrintWriter pw;

    // Constructor
    public Chat_Server(int puerto) throws IOException {
        serverSocket = new ServerSocket(puerto);
    }

    // Espera conexiones y devuelve la dirección del cliente
    public SocketAddress start() throws IOException {
        System.out.println(calcularHoraLocal() + " (Servidor) Esperando conexiones...");
        socket = serverSocket.accept(); // Queda a la espera de una conexion
        is = socket.getInputStream();   // Abre flujos de lectura
        os = socket.getOutputStream();  // Abre flujos de escritura
        abrirCanalesDeTexto();          // Abre los canales de texto
        SocketAddress IPCliente = socket.getRemoteSocketAddress(); // Recibe la dirección IP del cliente
        String userCliente = identificarHost();
        System.out.println(calcularHoraLocal() + " (Servidor) Conexión establecida con cliente " + IPCliente);
        return IPCliente;
    }

    // Cierra todo
    public void stop() throws IOException {
        //System.out.println(" (Servidor) Cerrando conexiones...");
        cerrarCanalesDeTexto();
        is.close();
        os.close();
        socket.close();
        serverSocket.close();
        //System.out.println(" (Servidor) Conexiones cerradas.");
    }

    // Abre los canales de texto
    public void abrirCanalesDeTexto() {
        //System.out.println(" (Servidor) Abriendo canales de texto...");
        // Lectura
        isr = new InputStreamReader(is);
        br = new BufferedReader(isr);
        // Escritura
        pw = new PrintWriter(os, true);
        //System.out.println(" (Servidor) Canales de texto abiertos.");
    }

    // Cierra los canales de texto
    public void cerrarCanalesDeTexto() throws IOException {
        //System.out.println(" (Servidor) Cerrando canales de texto...");
        // Lectura
        br.close();
        isr.close();
        // Escritura
        pw.close();
        //System.out.println(" (Servidor) Canales de texto cerrados.");
    }

    // Devuelve el mensaje enviado por el cliente como String
    public String leerMensajeDeTexto() throws IOException {
        //System.out.println(" (Servidor) Leyendo mensaje...");
        String msg = br.readLine();
        if (msg.isEmpty()) {
            msg = "Cliente desconectado.";
        }
        //System.out.println(" (Servidor) Mensaje leído.");
        return msg;
    }

    // Envía un mensaje al cliente mediante PrintWriter(OutPutStream)
    public void enviarMensajeDeTexto(String msg) {
        //System.out.println(" (Servidor) Eviando mensaje...");
        pw.println(msg);
        //System.out.println(" (Servidor) Mensaje enviado.");
    }

    public void guardarMensajeDeTexto(String msg) {
        try {
            FileWriter fw = new FileWriter("chat.txt", true);
            fw.write("\r\n" + msg);
            fw.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public String identificarHost() throws IOException {
        // Le pedimos al cliente que introduzca su nombre de usuario
        enviarMensajeDeTexto("Introduce tu nombre de usuario: ");
        // Leemos la respuesta del cliente
        String username = leerMensajeDeTexto();
        return username;
    }

    public String calcularHoraLocal() {
        String horaLocal = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy - HH:mm:ss"));
        return horaLocal;
    }

    public static void main(String[] args) {
        String msg;
        try {
            // Inicio
            Chat_Server server = new Chat_Server(50000);
            System.out.println(server.calcularHoraLocal() + " (Servidor) Sala abierta.");

            SocketAddress IPCliente = server.start();

            do {
                // Recepción del mensaje del cliente
                msg = server.leerMensajeDeTexto();
                //server.guardarMensajeDeTexto(salida);

                //  Confirmación de recepción del mensaje al cliente
                server.enviarMensajeDeTexto(server.calcularHoraLocal() + " " + msg);
            } while (!msg.equals("END"));

            // Cierre de todo
            server.stop();
            System.out.println(server.calcularHoraLocal() + " (Servidor) Sala cerrada.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
