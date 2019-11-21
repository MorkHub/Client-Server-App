package client.GUI;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    public MainFrame() throws HeadlessException {
        super();

        add(new GamePanel());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        pack();
        setVisible(true);
        setLocationRelativeTo(null);
    }
}
