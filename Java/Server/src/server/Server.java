package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static shared.SharedConstants.PORT_NUM;

public class Server {
    public static void main(String[] args) {
        new Server();
    }

    ServerSocket serverSocket;
    boolean running = true;

    public void stop() {
        running = false;
    }

    public Server() {
        ClientManager cm = ClientManager.getInstance();
        try (ServerSocket serverSocket = new ServerSocket(PORT_NUM);) {

            System.out.println("Server running on port " + PORT_NUM);

            Socket newSocket;
            while (running && (newSocket = serverSocket.accept()) != null) {
                cm.processSocket(newSocket);
            }

            System.out.println("Server closed.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
