package shared;

public class Response {

    String msg;
    String command;
    String data;

    public static Response parse(String msg) {
        String[] parts = msg.split(" ", 2);
        return new Response(parts[0], parts.length == 2 ? parts[1] : "");
    }

    Response(String command, String data) {
        this.command = command;
        this.data = data;
        this.msg = command + " " + data;
    }

    public String getMessage() {
        return msg;
    }

    public String getCommand() {
        return command;
    }

    public String getData() {
        return data;
    }
}
