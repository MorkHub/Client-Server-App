package server.message;

import shared.Player;

import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

public class MessageHandler {
    private HashMap<String, List<Consumer<String>>> listeners;

    public void register(String message, Consumer<String> action) {
        listeners.compute(message, (String key, List<Consumer<String>> actions) -> {
            if (actions == null)
                actions = new ArrayList<>();
            actions.add(action);

            return actions;
        });
    }

    public void handleMessage(String message, Player player, Socket socket) {
        String[] parts = message.split(" ", 1);
        if (parts.length == 2) {
            String command = parts[0];
            String args = parts[1];

            MessageEvent event = new MessageEvent(command, args, player, socket, LocalDateTime.now());

            List<Consumer<String>> actions = listeners.get(command);
            if (actions !=  null) {
                actions.forEach(a -> a.accept(args));
            }
        }
    }
}
