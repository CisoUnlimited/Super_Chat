/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dam.super_chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author Propietario
 */
public class Chat_Client_Test {

    private String serverIP;
    private int serverPort;
    private Socket socket;
    private InputStream is;
    private OutputStream os;

    // Objetos para envío de cadenas
    private InputStreamReader isr;
    private BufferedReader br;
    private PrintWriter pw;

    public Chat_Client_Test(String serverIP, int serverPort) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
    }

    public void start() throws IOException {
        //System.out.println(" (Cliente) Estableciendo conexión...");
        socket = new Socket(serverIP, serverPort);
        is = socket.getInputStream();
        os = socket.getOutputStream();
        abrirCanalesDeTexto();
        //System.out.println(" (Cliente) Conexión establecida.");
    }

    public void stop() throws IOException {
        //System.out.println(" (Cliente) Cerrando conexiones...");
        cerrarCanalesDeTexto();
        is.close();
        os.close();
        socket.close();
        //System.out.println(" (Cliente) Conexiones cerradas.");
    }

    public void abrirCanalesDeTexto() {
        //System.out.println(" (Cliente) Abriendo canales de texto...");
        // Canales de lectura
        isr = new InputStreamReader(is);
        br = new BufferedReader(isr);
        // Canales de escritura
        pw = new PrintWriter(os, true);
        //System.out.println(" (Cliente) Canales de texto abiertos.");
    }

    public void cerrarCanalesDeTexto() throws IOException {
        //System.out.println(" (Cliente) Cerrando canales de texto...");
        // Canales de lectura
        br.close();
        isr.close();
        // Canales de escritura
        pw.close();
        //System.out.println(" (Cliente) Canales de texto cerrados.");
    }

    public String leerMensajeTexto() throws IOException {
        //System.out.println(" (Cliente) Leyendo mensaje...");
        String msg = br.readLine();
        //System.out.println(" (Cliente) Mensaje leído.");
        return msg;
    }

    public void enviarMensajeTexto(String msg) {
        //System.out.println(" (Cliente) Enviando mensaje...");
        pw.println(msg);
        //System.out.println(" (Cliente) Mensaje enviado.");
    }

    public static void main(String[] args) {
        String msg;

        try {
            // Creamos el socket
            Chat_Client_Test client = new Chat_Client_Test("10.208.6.1", 50000);
            // Abrimos la comunicación
            client.start();
            do {

                // Enviar mensajes al servidor
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

                System.out.println("Mensaje a enviar (END para terminar): ");
                msg = br.readLine();
                client.enviarMensajeTexto(msg);

                // Recepción de la confirmación
                String msgRecibido = client.leerMensajeTexto();
                System.out.println(" (Servidor) " + msgRecibido);

            } while (!msg.equals("END"));
            client.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
