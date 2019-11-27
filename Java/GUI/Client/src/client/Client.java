package client;

import shared.Response;
import shared.SocketClient;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static shared.Message.*;

public class Client {
    private JFrame window = new JFrame();
    private JPanel split;

    // Player list
    private JPanel left;
    private DefaultListModel<Integer> model;
    private JList<Integer> playerList;
    private JButton passBtn;

    // Message log
    private JPanel right;
    private JScrollPane scrollPane;
    private JTextPane log;

    // Info panel
    private JPanel bottom;
    private JLabel statusBar;
    private SocketClient client;

    // Game data
    private int myID = -1;
    private int hasBall = -1;
    private Set<Integer> players = new HashSet<>();
    private int playerPre = 1;

    private void loop() {
        try {
            client = new SocketClient("localhost", 6969);
            success("Connected");

            Response res;

            while ((res = client.read()) != null) {
                int ID = -1;
                try {
                    ID = Integer.parseInt(res.getData());
                } catch (NumberFormatException ignored) {
                }

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
                            } catch (Exception ignored) {
                            }
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

    private void success(String msg) { write(msg, Color.BLUE); }
    private void info(String msg) { write(msg, Color.BLACK); }
    private void error(String msg) { write(msg, Color.RED); }
    private void write(String msg, Color c) {
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet attrs = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);

        attrs = sc.addAttribute(attrs, StyleConstants.FontFamily, "Lucida Console");
        attrs = sc.addAttribute(attrs, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

        StyledDocument doc = log.getStyledDocument();
        try {
            doc.insertString(doc.getLength(), msg + "\n", attrs);
        } catch (BadLocationException ignored) {
        }
    }

    private JPanel border(String title) {
        JPanel panel = new JPanel();
        TitledBorder border = new TitledBorder(title);
        panel.setBackground(new Color(255, 255, 255));
        border.setTitleJustification(TitledBorder.CENTER);
        border.setTitlePosition(TitledBorder.TOP);
        panel.setBorder(border);

        return panel;
    }

    private void updateStatus() {
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

        int playerPost = players.hashCode();
        if (playerPre != playerPost) {
            model.clear();
            players.forEach(model::addElement);
            playerPre = playerPost;
        }
    }

    private void create() {
        split = new JPanel();
        left = border("Players");
        right = border("Message Log");

        model = new DefaultListModel<>();
        playerList = new JList<>();
        playerList.setModel(model);

        passBtn = new JButton("Pass Ball");

        log = new JTextPane();
        log.setEditable(false);
        scrollPane = new JScrollPane(log);

        bottom = new JPanel();
        statusBar = new JLabel();
        statusBar.setHorizontalAlignment(SwingConstants.LEFT);

        passBtn.setEnabled(false);
    }

    private void layout() {
        window.setLayout(new BorderLayout());
        left.setLayout(new BorderLayout());
        right.setLayout(new GridLayout(1, 1));
        split.setLayout(new GridLayout(1, 2));
    }

    private void populate() {
        left.add(playerList, BorderLayout.CENTER);
        left.add(passBtn, BorderLayout.SOUTH);

        right.add(scrollPane, BorderLayout.CENTER);

        split.add(left);
        split.add(right);
        bottom.add(statusBar);

        window.add(split);
        window.add(bottom, BorderLayout.SOUTH);
    }

    private void listeners() {
        ActionListener clickButton = (e) -> {
            Integer playerID = playerList.getSelectedValue();
            if (playerID != null) {
                client.send(PASS_BALL, playerID);
            }
        };
        MouseListener doubleClickUser = new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    Integer playerID = playerList.getSelectedValue();
                    if (playerID != null) {
                        client.send(PASS_BALL, playerID);
                    }
                }
            }
        };

        passBtn.addActionListener(clickButton);
        playerList.addMouseListener(doubleClickUser);
    }

    void finalise() {
        window.setPreferredSize(new Dimension(700, 400));
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setVisible(true);
        window.pack();
    }

    private Client() {
        create();
        layout();
        populate();
        listeners();
        finalise();
        updateStatus();

        new Thread(this::loop).start();
    }

    public static void main(String[] args) {
        new Client();
    }
}
