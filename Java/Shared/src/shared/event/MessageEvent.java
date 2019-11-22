package shared.event;

import shared.ISocketClient;
import shared.Player;

import java.time.LocalDateTime;

public class MessageEvent {
    private String command;
    private String data;
    private ISocketClient client;
    private LocalDateTime time;

    public MessageEvent(String message, ISocketClient client, LocalDateTime time) {
        String[] split = message.split(" ", 2);
        this.command = split[0];
        this.data = split.length > 1 ? split[1] : "";

        this.client = client;
        this.time = time;
    }

    public String getCommand() {
        return command;
    }
    public static String getCommand(String message) {
        String[] split = message.split(" ", 2);
        return split[0];
    }

    public String getData() {
        return data;
    }
    public static String getData(String message) {
        String[] split = message.split(" ", 2);
        return split.length > 1 ? split[1] : "";
    }

    public ISocketClient getClient() {
        return client;
    }
    public Player getOwner() {
        return getClient().getOwner();
    }
    public LocalDateTime getTime() {
        return time;
    }
}
