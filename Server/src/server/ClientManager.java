package server;

import shared.Player;
import shared.PlayerNotFoundException;

import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ClientManager {
    private ClientManager instance;
    private Map<Player, Socket> sockets;

    private ClientManager() {
        this.sockets = new HashMap<>();
    }

    public ClientManager getInstance() {
        if (instance == null) {
            instance = new ClientManager();
        }

        return instance;
    }

    public void addPlayer(Socket socket, Player player) {
        sockets.put(player, socket);
    }

    public Player getPlayer(String id) throws PlayerNotFoundException {
        Optional<Player> playerById = sockets.keySet().stream().filter((Player p) -> id.equals(p.id)).findFirst();
        if (playerById.isPresent())
            return playerById.get();
        else
            throw new PlayerNotFoundException();
    }

    public Socket getSocketByPlayer (Player p) throws PlayerNotFoundException {
        Socket socketByPlayer = sockets.get(p);
        if (socketByPlayer != null)
            return socketByPlayer;
        else
            throw new PlayerNotFoundException();
    }
}

