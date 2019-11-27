package shared;

import javax.swing.text.DateFormatter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.server.ExportException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SocketClient {
    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final boolean DEBUG = java.lang.management.ManagementFactory.getRuntimeMXBean().
            getInputArguments().toString().contains("jdwp");

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
            if (message == null) return null;

            Response response = Response.parse(message);
            LocalDateTime dt = LocalDateTime.now();

            if (DEBUG)
                System.out.println(String.format("[%s] [FROM %s] %s", fmt.format(dt), this, message));

            return response;
        } catch (IOException e) {
            return null;
        }
    }

    public boolean send(String c) {
        return send(c, "");
    }

    public boolean send(String command, Object data) {
        String message = command;
        if (data != null) message += " " + data;

        LocalDateTime dt = LocalDateTime.now();
        out.println(message);
        if (DEBUG)
            System.out.println(String.format("[%s] [TO %s] %s", fmt.format(dt), this, message));

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
