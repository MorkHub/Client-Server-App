package server;

import shared.Response;
import shared.SocketClient;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import static shared.Message.*;


public class Server {
    private ServerSocket server;
    private final Map<Integer, SocketClient> clients = new HashMap<>();
    private int hasBall = -1;

    boolean fixBall() {
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

    void passBall(SocketClient from, int id) {

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
        } else {
            from.send(ERROR, "Player not found.");
        }
    }

    void broadcast(String cmd, Object d) {
        clients.forEach((id, client) -> client.send(cmd, d));
    }

    Thread heartbeatThread;

    void heartbeat() {
        if (heartbeatThread == null) {
            heartbeatThread = new Thread(() -> {
//                while (!server.isClosed()) {
//                    clients.forEach((i, c) -> {
//                        c.send("HEARTBEAT", null);
//                    });
//                    Thread.sleep(500);
//                }
            });
            heartbeatThread.start();
        }
    }

    String listPlayers() {
        StringBuilder sb = new StringBuilder();
        clients.values().forEach(c -> sb.append(c.id).append(","));
        return sb.toString();
    }

    Server() {
        try {
            server = new ServerSocket(6969);
            heartbeat();

            System.out.println("Server running on port 6969");

            Socket socket;
            while ((socket = server.accept()) != null) {
                SocketClient client = new SocketClient(socket);
                clients.put(client.id, client);

                client.send(ID_ASSIGNED, client.id);
                broadcast("PLAYERS", listPlayers());
                client.send(BALL_MOVED, hasBall);

                System.out.println(String.format(" - %s connected %s", client, clients.values()));
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

                    System.out.println(String.format(" - %s disconnected %s", client, clients.values()));
                    fixBall();
                }).start();
            }
            server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Server();
    }
}
