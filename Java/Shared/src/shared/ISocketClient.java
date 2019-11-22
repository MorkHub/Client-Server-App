package shared;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public interface ISocketClient {
    Player getOwner();

    Socket getSocket();

    InetAddress getAddress();

    String readMessage() throws IOException;

    void sendMessage(String message);
}
