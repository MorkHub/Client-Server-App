package gaming;

import shared.Response;
import shared.SocketClient;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static shared.Message.*;

public class Client {

    private int myID = -1;
    private int hasBall = -1;
    private Set<Integer> players = new HashSet<>();

    SocketClient client;

    void loop() {
        try {
            client = new SocketClient("localhost", 6969);
            success("Connected");

            Response res;

            while ((res = client.read()) != null) {
                int ID = -1;
                try { ID = Integer.parseInt(res.getData()); } catch (NumberFormatException ignored) {}

                switch (res.getCommand()) {
                    case PLAYER_JOIN:
                        if (ID >= 0) {
                            players.add(ID);
                            info(String.format("Player #%s connected.", ID));
                        }
                        break;

                    case PLAYER_LEAVE:
                        if (ID >= 0) {
                            players.remove(ID);
                            error(String.format("Player #%s disconnected.", ID));
                        }
                        break;

                    case PLAYER_LIST:
                        players.clear();
                        for (String player : res.getData().split(",")) {
                            try {
                                players.add(Integer.parseInt(player));
                            } catch (Exception ignored) {}
                        }
                        break;

                    case BALL_MOVED:
                        if (ID >= 0) {
                            hasBall = ID;
                            if (hasBall == myID) {
                                success("Ball was passed to me!");
                            } else {
                                info("Ball was passed to Player #" + res.getData());
                            }
                        }
                        break;

                    case ID_ASSIGNED:
                        try {
                            myID = Integer.parseInt(res.getData());
                            info("I am Player #" + myID);
                            window.setTitle("Player #" + myID);
                        } catch (NumberFormatException e) {
                            error("Invalid user ID from server");
                        }
                        break;

                    case ERROR:
                        System.err.println(res.getData());
                        break;

                    case PING:
                        continue;
                }

                updateStatus();
            }
            info("Disconnected.");
        } catch (IOException e) {
            error(e.getMessage());
        }
    }

    private void success(String msg) {
        write(msg, Color.BLUE);
    }

    private void error(String msg) {
        write(msg, Color.RED);
    }

    private void info(String msg) {
        write(msg, Color.BLACK);
    }

    private void write(String msg, Color c) {
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet attrs = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);

        attrs = sc.addAttribute(attrs, StyleConstants.FontFamily, "Lucida Console");
        attrs = sc.addAttribute(attrs, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

        int len = log.getDocument().getLength();
        log.setCaretPosition(len);
        log.setCharacterAttributes(attrs, false);
        log.replaceSelection(msg + "\n");
    }

    JPanel border(String title) {
        JPanel panel = new JPanel();
        TitledBorder border = new TitledBorder(title);
        panel.setBackground(new Color(255, 255, 255));
        border.setTitleJustification(TitledBorder.CENTER);
        border.setTitlePosition(TitledBorder.TOP);
        panel.setBorder(border);

        return panel;
    }

    int playerPre = 1, playerPost;

    void updateStatus() {
        List<String> strings = new ArrayList<>();

        if (hasBall == -1) {
            strings.add("Nobody has the ball");
        } else if (myID != -1 && myID == hasBall) {
            strings.add("I have the ball!");
        } else {
            strings.add(String.format("Player #%s has the ball", hasBall));
        }

        if (myID == -1) {
            strings.add("I am nobody");
        } else {
            strings.add(String.format("I am Player #%s", myID));
        }

        passBtn.setEnabled(myID != -1 && hasBall == myID);
        statusBar.setText(String.join(" | ", strings));

        playerPost = players.hashCode();
        if (playerPre != playerPost) {
            model.clear();
            players.forEach(model::addElement);
            playerPre = playerPost;
        }
    }

    private JLabel statusBar = new JLabel();
    private DefaultListModel<Integer> model = new DefaultListModel<>();
    private JButton passBtn = new JButton("Pass Ball");
    private JTextPane log = new JTextPane();
    private JFrame window = new JFrame();

    Client() {
        window.setLayout(new BorderLayout());

        JPanel split = new JPanel();
        JPanel left = border("Players");
        JPanel right = border("Message Log");

        JPanel bottom = new JPanel();
        JList<Integer> playerList = new JList<>();
        playerList.setModel(model);

        split.setLayout(new GridLayout(1, 2));

        passBtn.setEnabled(false);
        passBtn.addActionListener((e) -> {
            Integer playerID = playerList.getSelectedValue();
            if (playerID != null) {
                client.send(PASS_BALL, playerID);
            }
        });

        playerList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    Integer playerID = playerList.getSelectedValue();
                    if (playerID != null) {
                        client.send(PASS_BALL, playerID);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(log);
        left.setLayout(new BorderLayout());


        log = new JTextPane();
        log.setEditable(false);

        right.setLayout(new GridLayout(1,1));


        playerList.setModel(model);

        statusBar.setHorizontalAlignment(SwingConstants.LEFT);

        left.add(playerList, BorderLayout.CENTER);
        left.add(passBtn, BorderLayout.SOUTH);

        right.add(scrollPane,  BorderLayout.CENTER);

        split.add(left);
        split.add(right);
        bottom.add(statusBar);

        window.add(split);
        window.add(bottom, BorderLayout.SOUTH);

        window.setPreferredSize(new Dimension(700, 400));
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setVisible(true);
        window.pack();

        updateStatus();
        new Thread(this::loop).start();
    }

    public static void main(String[] args) {
        new Client();
    }
}
