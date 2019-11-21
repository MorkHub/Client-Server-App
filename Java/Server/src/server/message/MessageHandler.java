package server.message;

import server.SocketClient;
import shared.Player;

import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class MessageHandler {
    private HashMap<String, List<SocketAction>> listeners;

    public void register(String message, SocketAction action) {
        listeners.compute(message, (String key, List<SocketAction> actions) -> {
            if (actions == null)
                actions = new ArrayList<>();
            actions.add(action);

            return actions;
        });
    }

    public void handleMessage(SocketClient client, String message) {
        String[] parts = message.split(" ", 1);
        if (parts.length == 2) {
            String command = parts[0];
            String args = parts[1];

            MessageEvent event = new MessageEvent(command, args, client, LocalDateTime.now());

            List<SocketAction> actions = listeners.get(command);
            if (actions !=  null) {
                actions.forEach(a -> a.performAction(event));
            }
        }
    }
}
