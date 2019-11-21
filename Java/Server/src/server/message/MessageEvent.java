package server.message;

import server.SocketClient;
import shared.Player;

import java.net.Socket;
import java.time.LocalDateTime;

public class MessageEvent {
    public String command;
    public String args;
    public SocketClient client;
    public LocalDateTime time;

    public MessageEvent(String command, String args, SocketClient client, LocalDateTime time) {
        this.command = command;
        this.args = args;
        this.client = client;
        this.time = time;
    }
}
