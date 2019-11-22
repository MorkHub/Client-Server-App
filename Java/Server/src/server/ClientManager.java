package server;

import shared.event.MessageEvent;
import shared.event.MessageHandler;
import shared.ISocketClient;
import shared.Player;
import shared.PlayerNotFoundException;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class ClientManager {
    private MessageHandler messageHandler;
    private static Map<Integer, SocketClient> clients;
    private Player hasBall;

    private static ClientManager instance;
    private static int ID = 0;

    private ClientManager() {
        messageHandler = new MessageHandler();
        clients = new HashMap<>();
        hasBall = null;

        registerHandlers();
    }

    static ClientManager getInstance() {
        if (instance == null) {
            instance = new ClientManager();
        }

        return instance;
    }

    private String passBall(MessageEvent event) {
        ISocketClient client = event.getClient();
        try {
            Player recipient = getClientById(event.getData());
            if (!hasBall.equals(event.getOwner()))
                client.sendMessage("ERROR You do not have the ball");
            hasBall = recipient;

//            broadcast("BALLHOLDER " + recipient.id);
            return "SUCCESS";
        } catch (PlayerNotFoundException e) {
            return "ERROR Player not found";
        }
    }

    private String listPlayers(MessageEvent event) {
        StringBuilder sb = new StringBuilder();

        sb.append("PLAYERLIST ");
        clients.forEach((Integer i, SocketClient c) -> sb.append(c.getOwner().toString()).append(" "));
        return sb.toString();
    }

    public void registerHandlers() {
        messageHandler.register("PASS", this::passBall);
        messageHandler.register("LISTPLAYERS", this::listPlayers);
    }

    public void processSocket(Socket socket) {
        try {
            ISocketClient initialClient = new SocketClient(null, socket);
            System.out.println(String.format("New connection from [%s]", initialClient.getAddress()));
            int attemptsRemaining = 3;

            String fromClient;
            while (attemptsRemaining-- > 0 && (fromClient = initialClient.readMessage()) != null) {
                System.out.println(fromClient);
                MessageEvent registerEvent = new MessageEvent(fromClient, initialClient, null);

                System.out.println(String.format("%s / %s", registerEvent.getCommand(), registerEvent.getData()));
                if (registerEvent.getCommand().equals("IDENTIFY")) {
                    Player player = new Player(++ID, registerEvent.getData());
                    ISocketClient client = new SocketClient(player, socket);
                    System.out.println(String.format("[%s] Registered as %s", client.getAddress(), player));

                    client.sendMessage("SUCCESS");
                    onClientRegistered(client);

                    break;
                } else {
                    initialClient.sendMessage("ERROR You must identify first.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onClientRegistered(ISocketClient client) {
        new Thread(() -> {
            try {
                String fromClient, toClient;
                while ((fromClient = client.readMessage()) != null) {
                    System.out.println(String.format("FROM [%s]: %s", client.getAddress(), fromClient));
                    toClient = messageHandler.processInput(client, fromClient);

                    if (toClient != null) {
                        client.sendMessage(toClient);
                    }
                }
                System.out.println(String.format("Client disconnected [%s]", client.getAddress()));
            } catch (IOException e) {
                System.err.println("Error in socket");
                e.printStackTrace();
            }
        }).start();
    }

    public void broadcast(String message) {
        clients.forEach((Integer id, SocketClient client) -> client.sendMessage(message));
    }

    public Player getClientById(String id) throws PlayerNotFoundException {
        return getClientById(Integer.parseInt(id));
    }
    public Player getClientById(int id) throws PlayerNotFoundException {
        SocketClient client = clients.get(id);
        if (client != null)
            return client.getOwner();
        else
            throw new PlayerNotFoundException();
    }
}

