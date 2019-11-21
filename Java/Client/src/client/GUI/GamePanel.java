package client.GUI;

import shared.Player;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static client.GUI.Constants.LAST_ADDRESS_FILENAME;
import static shared.SharedConstants.PORT_NUM;

public class GamePanel extends JPanel {
    public void writeLastAddress(String address) {
        if (address != null && !address.isEmpty()) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("lastserver.dat"))) {
                writer.write(address);
                writer.newLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String readLastAddress() {
        if (new File(LAST_ADDRESS_FILENAME).exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader("lastserver.dat"))) {
                String address = reader.readLine();
                return address != null ? address : "";
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return "";
    }

    private DefaultListModel<Player> model = new DefaultListModel<>();

    public void setPlayers(Iterable<Player> players) {
        model.clear();
        players.forEach(model::addElement);
    }

    Player me;

    public void addPlayer(Player player) {
        model.addElement(player);
    }

    private JPanel bottom = new JPanel();

    private JButton passBtn = new JButton("PASS");
    private JList<Player> playerList = new JList<>();
    private JTextArea log = new JTextArea();

    private JLabel statusBar = new JLabel();
    private Player ballHolder = null;

    public void updateStatus() {
        List<String> statusItems = new ArrayList<>();

        StringBuilder sb = new StringBuilder();
        statusItems.add(ballHolder != null ? ballHolder.toString() : "Nobody has the ball");
        statusItems.add(connected ? ("Server: " + serverAddress) : "Not connected");

        statusBar.setText(String.format("%s", String.join(" | ", statusItems)));
    }

    public void passBall() {

        System.out.println(String.format("Connected: %s\nClosed: %s", activeSocket.isConnected(), activeSocket.isClosed()));
        sendMessage("PASSBALL 1");
    }

    JButton reconnectBtn;

    public GamePanel() {
        super();
        setLayout(new BorderLayout());

        JPanel left = new BorderPanel("Players");
        JPanel right = new BorderPanel("Message Log");
        JPanel split = new JPanel();

        GridLayout splitLayout = new GridLayout(1, 2);
        split.setLayout(splitLayout);

        left.setLayout(new BorderLayout());
        left.add(playerList, BorderLayout.CENTER);
        left.add(passBtn, BorderLayout.SOUTH);
//        passBtn.setEnabled(false);

        passBtn.addActionListener((ActionEvent e) -> {
            passBall();
        });

        log.setEditable(false);
        right.add(log);

        split.add(left);
        split.add(right);

        playerList.setModel(model);

        statusBar.setHorizontalAlignment(SwingConstants.LEFT);
        updateStatus();

        reconnectBtn = new JButton("Reconnect");
        reconnectBtn.addActionListener((ActionEvent e) -> {
            disconnect();
            connect();
        });

        bottom.add(statusBar);
        bottom.add(reconnectBtn);

        add(split);
        add(bottom, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(700, 400));


        String name = getPlayerName();
        me = new Player(1, name);
        info("Set player name as " + name);

        serverAddress = getAddress();

        connect();
    }

    private PrintWriter out;
    private BufferedReader in;

    private boolean connected = false;
    private boolean connecting = false;

    public synchronized void sendMessage(String message) {
        if (connected) {
            System.out.println(String.format(" -> %s", message));
            info(String.format(" -> %s", message));
            out.write(message + "\n");
        }
    }

    public void onDisconnected() {
        connected = false;
        updateStatus();
    }

    String serverAddress;

    public void onConnected() {
        reconnectBtn.setEnabled(true);
        connecting = false;
        connected = true;
        new Thread(() -> {
            String message;
            try {
                while (connected) {
                    message = in.readLine();
                    System.out.println(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
                onDisconnected();
            }
        }).start();

        updateStatus();
    }

    public void disconnect() {
        if (connected) {
            try {
                activeSocket.close();
                connected = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    Socket activeSocket;

    public void connect() {
        if (!connected && !connecting) {
            connecting = true;
            reconnectBtn.setEnabled(false);
            new Thread(() -> {
                try {
                    info("Connecting to:" + serverAddress);
                    activeSocket = new Socket(serverAddress, PORT_NUM);

                    out = new PrintWriter(activeSocket.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(activeSocket.getInputStream()));

                    System.out.println(String.format("Connected: %s\nClosed: %s", activeSocket.isConnected(), activeSocket.isClosed()));
                    onConnected();

                    out.write("HELLO\n");
                    System.out.println(in.readLine());
                    sendMessage("REGISTER " + me.name);
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(
                            null,
                            "Could not connect to the server. Is it running?",
                            "Connection Failed", JOptionPane.ERROR_MESSAGE);
                }
            }).start();
        }
    }

    public String getAddress() {
        String address;
        if ((address = JOptionPane.showInputDialog(null, "Enter server IP", readLastAddress())) == null) {
            return "";
        } else {
            writeLastAddress(address);
            return address;
        }
    }

    public String getPlayerName() {
        String localUser = System.getProperty("user.name");
        String name;
        if ((name = JOptionPane.showInputDialog(null, "What is your name?", localUser)) != null) {
            return name;
        } else {
            return localUser;
        }
    }

    public void info(String message) {
        write("INFO", message);
    }

    private void write(String level, String message) {
        log.append(String.format("[%s] %s\n", level, message));
    }
}
