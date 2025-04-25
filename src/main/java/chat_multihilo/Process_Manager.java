package chat_multihilo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Ciso
 */
public class Process_Manager extends Thread{
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String user;

    public Process_Manager(Socket socket, String user) {
        this.socket = socket;
        this.user = user;
    }
    
    public void sendMessage(String msg) {
        if (out != null) {
            out.println(msg);
        }
    }
    
    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            
            out.println("Bienvenido al chat.");
            
            String msg;
            while((msg = in.readLine()) != null) {
                System.out.println("Mensaje recibido: " + msg);
                SocketTCPServer.broadcast(msg, this);
            } 
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                SocketTCPServer.clients.remove(this);
                socket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
