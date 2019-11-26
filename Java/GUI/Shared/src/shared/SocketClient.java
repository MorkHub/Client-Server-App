package shared;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SocketClient {

    public static final boolean DEBUG = true;

    private static int IDs = 0;
    private BufferedReader in;
    private PrintWriter out;
    public int id;

    public SocketClient(String address, int port) throws IOException {
        this(new Socket(address, port));
        id = -1;
    }

    public SocketClient(Socket socket) throws IOException {
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        id = ++IDs;
    }

    public Response read() {
        try {
            String message = in.readLine();
            Response response = Response.parse(message);
            if (DEBUG)
                System.out.println(String.format("[FROM %s] %s", this, message));

            return response;
        } catch (IOException e) {
//            e.printStackTrace();
            return null;
        }
    }

    public boolean send(String c) {
        return send(c, "");
    }

    public boolean send(String command, Object data) {
        String message = command;
        if (data != null) message += " " + data;

        out.println(message);
        if (DEBUG)
            System.out.println(String.format("[TO %s] %s", this, message));

        return true;
    }

    @Override
    public String toString() {
        if (id == -1)
            return "SERVER";
        else
            return String.format("Player #%s", id);
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().equals(obj.getClass()))
            return false;

        return id == ((SocketClient) obj).id;
    }
}
