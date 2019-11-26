package gaming;

import shared.Response;
import shared.SocketClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static shared.Message.*;

public class Client {

    private int myID = -1;
    private int hasBall = -1;
    private Set<Integer> players = new HashSet<>();

    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

    int readInt(String prompt) {
        try {
            boolean valid = false;
            while (!valid) {
                System.out.print(prompt);
                String input = reader.readLine();
                return Integer.parseInt(input);
            }
        } catch (IOException e) {}

        return -1;
    }

    Client() {
        try {
            SocketClient client = new SocketClient("localhost", 6969);
            System.out.println("Connected");
//            client.send(PING, null);

            Response res;

            while ((res = client.read()) != null) {
                System.out.println("\n\n===== Update ====");
                int ID = -1;
                try {
                    ID = Integer.parseInt(res.getData());
                } catch (NumberFormatException ignored) {}

                switch (res.getCommand()) {
                    case "PLAYERS":
                        players.clear();
                        for (String player : res.getData().split(",")) {
                            try {
                                players.add(Integer.parseInt(player));
                            } catch(Exception ignored) {}
                        }

                    case BALL_MOVED:
                        if (ID >= 0) {
                            hasBall = ID;
                            if (hasBall == myID) {
                                System.out.println("Ball was passed to me! Who do I pass it to?");

                                int passTo = -1;
                                while (passTo < 0 || !players.contains(passTo)) {
                                    passTo = readInt("Enter Player ID: ");
                                }

                                client.send(PASS_BALL, passTo);
                            } else {
                                System.out.println("Ball was passed to player " + res.getData());
                            }
                        }
                        break;

                    case ID_ASSIGNED:
                        try {
                            myID = Integer.parseInt(res.getData());
                            System.out.println("I am Player #" + myID);
                        } catch (NumberFormatException e) {
                            System.err.println("Invalid user ID from server");
                        }
                        break;

                    case ERROR:
                        System.err.println(res.getData());
                }

                System.out.println("Players: " + players);
                System.out.println("Ball held by: " + hasBall);
            }
            System.out.println("Disconnected.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new Client();
    }
}
