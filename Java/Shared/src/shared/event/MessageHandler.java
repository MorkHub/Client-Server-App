package shared.event;

import shared.ISocketClient;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class MessageHandler {
    private Map<String, SocketAction> listeners;

    public MessageHandler() {
        listeners = new HashMap<>();
    }

    public void register(String command, SocketAction action) {
        listeners.put(command, action);
    }

    public String processInput(ISocketClient client, String message) {
        MessageEvent event = new MessageEvent(message, client, LocalDateTime.now());
        SocketAction action = listeners.get(event.getCommand());

        if (action != null) {
            return action.performAction(event);
        } else {
            return "ERROR Invalid command";
        }
    }
}
