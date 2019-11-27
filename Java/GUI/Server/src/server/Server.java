package server;

import shared.Response;
import shared.SocketClient;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;

import static shared.Message.*;
import static shared.SocketClient.DEBUG;

public class Server {
    private ServerSocket server;
    private final Map<Integer, SocketClient> clients = new HashMap<>();
    private int hasBall = -1;

    private boolean fixBall() {
        synchronized (clients) {
            if (hasBall == -1 || !clients.containsKey(hasBall)) {
                if (clients.isEmpty()) {
                    hasBall = -1;
                    System.out.println("  Ball returned to server");
                } else {
                    hasBall = clients.values().iterator().next().id;
                    System.out.println("  Ball given to " + hasBall);
                }
                broadcast(BALL_MOVED, hasBall);
                return true;
            }
            return false;
        }
    }

    private void passBall(SocketClient from, int id) {
        if (fixBall()) {
            System.out.println("Fixed ball");
        }

        if (hasBall != -1 && hasBall != from.id) {
            from.send(ERROR, "You do not have the ball!");
            return;
        }

        if (clients.containsKey(id)) {
            hasBall = id;
            broadcast(BALL_MOVED, hasBall);
            System.out.println(String.format("%s passed the ball to %s.", from, id));
        } else {
            from.send(ERROR, "Player not found.");
            System.out.println(String.format("%s tried to give the ball to %s, but did not have it.", from, id));
        }
    }

    private void broadcast(String cmd, Object d) {
        clients.forEach((id, client) -> client.send(cmd, d));
    }

    private Thread heartbeatThread;

    private void heartbeat() {
        if (heartbeatThread == null) {
            heartbeatThread = new Thread(() -> {
                try {
                    while (!server.isClosed()) {
                        clients.forEach((i, c) -> {
                            c.send(PING, null);
                        });
                        Thread.sleep(100);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            heartbeatThread.start();
        }
    }

    private String listPlayers() {
        StringBuilder sb = new StringBuilder();
        clients.values().forEach(c -> sb.append(c.id).append(","));
        return sb.toString();
    }

    private Server() {
        try {
            server = new ServerSocket(6969);
            heartbeat();

            System.out.println("Server running on port 6969");

            Socket socket;
            while ((socket = server.accept()) != null) {
                SocketClient client = new SocketClient(socket);
                clients.put(client.id, client);

                client.send(ID_ASSIGNED, client.id);
                client.send(PLAYER_LIST, listPlayers());
                broadcast(PLAYER_JOIN, client.id);
                client.send(BALL_MOVED, hasBall);

                System.out.println(String.format("%s connected. Players: [%s]", client, clients.values()));
                fixBall();

                new Thread(() -> {
                    Response res;
                    while ((res = client.read()) != null) {
                        String cmd = res.getCommand();

                        if (cmd.equals(PASS_BALL)) {
                            try {
                                int id = Integer.parseInt(res.getData());
                                passBall(client, id);
                            } catch (NumberFormatException e) {
                                client.send(ERROR, "Invalid Player");
                            }
                        } else {
                            client.send(ERROR, res.getMessage() + " is not a command");
                        }
                    }

                    clients.remove(client.id);
                    broadcast(PLAYER_LEAVE, client.id);

                    System.out.println(String.format("%s disconnected. Players: [%s]", client, clients.values()));
                    fixBall();
                }).start();
            }
            server.close();
        } catch (IOException e) {
            if (DEBUG)
                e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Server();
    }
}
