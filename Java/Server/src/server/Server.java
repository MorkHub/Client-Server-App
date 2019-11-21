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
    public void stop (){
        try {
            serverSocket.close();
        } catch (IOException ignored) {

        }
    }

    public Server() {
        ClientManager cm = ClientManager.getInstance();
        try {
            serverSocket = new ServerSocket(PORT_NUM);
            System.out.println("Server running on port " + PORT_NUM);

            Socket newSocket;
            while ((newSocket = serverSocket.accept()) != null) {
                System.out.println(String.format("New socket! [%s]", newSocket.getInetAddress()));

                SocketClient client = new SocketClient(null, newSocket);
                cm.onClientConnected(client);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
