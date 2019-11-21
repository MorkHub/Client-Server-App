package server;

import shared.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketClient {
    public Player player;
    public Socket socket;

    public PrintWriter out;
    public BufferedReader in;

    public SocketClient(Player player, Socket socket) throws IOException {
        this.player = player;
        this.socket = socket;

        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void sendMessage(String message) {
        System.out.println(String.format(" [%s] <- %s", socket.getInetAddress(), message));
        out.write(message + "\n");
    }
}
