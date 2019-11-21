package server.message;

import shared.Player;

import java.net.Socket;
import java.time.LocalDateTime;

public class MessageEvent {
    String command;
    String args;
    Player player;
    Socket socket;
    LocalDateTime time;

    public MessageEvent(String command, String args, Player player, Socket socket, LocalDateTime time) {
        this.command = command;
        this.args = args;
        this.player = player;
        this.socket = socket;
        this.time = time;
    }
}
