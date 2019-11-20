package client.GUI;

import shared.Player;

import javax.swing.*;
import java.awt.*;
import java.io.*;

import static client.GUI.Constants.LAST_ADDRESS_FILENAME;

public class GamePanel extends JPanel {

    public String getAddress() {
        String address;
        if ((address = JOptionPane.showInputDialog(null, "Enter server IP", readLastAddress())) == null) {
            return "";
        } else {
            writeLastAddress(address);
            return address;
        }
    }

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

    public void addPlayer(Player player) {
        model.addElement(player);
    }

    private JPanel left = new BorderPanel("Players");
    private JPanel right = new BorderPanel("Message Log");
    private JPanel bottom = new JPanel();

    private JButton passBtn = new JButton("PASS");
    private JList<Player> playerList = new JList<>();
    private JTextArea log = new JTextArea();

    private JLabel statusBar = new JLabel();
    private Player ballHolder = null;

    public void updateStatus() {
        String ballHolderString;
        ballHolderString = ballHolder != null ? ballHolder.toString() : "Nobody has the ball";
        statusBar.setText(String.format("%s", ballHolderString));
    }

    public GamePanel() {
        super();
        setLayout(new BorderLayout());

        left.setLayout(new BorderLayout());
        left.add(playerList, BorderLayout.CENTER);
        left.add(passBtn, BorderLayout.SOUTH);

        passBtn.setEnabled(false);

        right.add(log);
        log.setEditable(false);

        GridLayout splitLayout = new GridLayout(1, 2);
        JPanel split = new JPanel();
        split.setLayout(splitLayout);
        split.add(left);
        split.add(right);

        playerList.setModel(model);

        statusBar.setHorizontalAlignment(SwingConstants.LEFT);
        updateStatus();
        bottom.add(statusBar);

        add(split);
        add(bottom, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(700, 400));

        addPlayer(new Player("9147", "Gamer"));

        String host = getAddress();
        info("Connecting to:" + host);
    }

    public void info(String message) {
        write("INFO", message);
    }
    private void write(String level, String message) {
        log.append(String.format("[%s] %s", level, message));
    }
}
