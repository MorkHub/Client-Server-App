package server;

import server.message.MessageEvent;
import server.message.MessageHandler;
import shared.Player;
import shared.PlayerNotFoundException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ClientManager {
    MessageHandler messageHandler;
    static Map<Integer, SocketClient> clients;
    SocketClient hasBall;

    private static ClientManager instance;
    private static int ID = 0;

    public ClientManager() {
        messageHandler = new MessageHandler();
        clients = new HashMap<>();
        hasBall = null;
    }

    public static ClientManager getInstance() {
        if (instance == null) {
            instance = new ClientManager();
        }

        return instance;
    }

    void onRegister(MessageEvent event) {
        String args = event.args;
        SocketClient client = event.client;
        String name = args.split(" ")[0];
        if (!name.isBlank()) {
            Player player = new Player(++ID, name);
            client.player = player;
            clients.put(player.id, client);
        }
    }

    void passBall(MessageEvent event) {
        String args = event.args;
        SocketClient client = event.client;
        SocketClient recipient = clients.get(Integer.parseInt(args));
        if (recipient == null) {
            client.out.write("ERROR Player not found");
        } else {
            if (!hasBall.equals(client.player)) {
                client.sendMessage("ERROR You do not have the ball");
            }
            hasBall = recipient;
            broadcast("BALLHOLDER " + recipient.player.id);
        }
    }

    void listPlayers(SocketClient client) {
        StringBuilder sb = new StringBuilder();

        sb.append("PLAYERLIST ");
        clients.forEach((Integer i, SocketClient c) -> sb.append(c.player.toString()).append(" "));
        client.sendMessage(sb.toString());
    }

    void onListPlayers(MessageEvent event) {
        listPlayers(event.client);
    }

    public void registerHandlers() {
        messageHandler.register("REGISTER", this::onRegister);
        messageHandler.register("PASS", this::passBall);
    }

    public void onClientConnected(SocketClient client) {
        new Thread(() -> {
            try {
                listPlayers(client);
                String message;
                while ((message = client.in.readLine()) != null) {
                    System.out.println(String.format("[%s] -> %s", client.socket.getInetAddress(), message));
                    messageHandler.handleMessage(client, message);
                }

                System.out.println(String.format("Client disconnected [%s]", client.socket.getInetAddress()));
            } catch (IOException e) {
                System.err.println("Error in socket");
                e.printStackTrace();
            }
        }).start();
    }

    public void broadcast(String message) {
        clients.forEach((Integer id, SocketClient client) -> client.sendMessage(message));
    }

    public SocketClient getClientById(int id) throws PlayerNotFoundException {
        SocketClient client = clients.get(id);
        if (client != null)
            return client;
        else
            throw new PlayerNotFoundException();
    }
}

