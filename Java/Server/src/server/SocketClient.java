package server;

import shared.ISocketClient;
import shared.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class SocketClient implements ISocketClient {
    private Player player;
    private Socket socket;

    private PrintWriter out;
    private BufferedReader in;

    public SocketClient(Player player, Socket socket) throws IOException {
        this.player = player;
        this.socket = socket;

        this.out = new PrintWriter(socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    public void sendMessage(String message) {
        System.out.println(String.format("TO [%s]: %s", getAddress(), message));
        out.println(message);
    }

    @Override
    public String readMessage() throws IOException {
        return in.readLine();
    }

    @Override
    public String toString() {
        return String.format("%s:%s", getOwner(), getAddress());
    }

    @Override
    public Player getOwner() {
        return player;
    }

    @Override
    public Socket getSocket() {
        return socket;
    }

    @Override
    public InetAddress getAddress() {
        return socket.getInetAddress();
    }
}
