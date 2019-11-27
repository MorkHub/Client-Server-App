package client;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class TitledPanel extends JPanel {
    TitledPanel(String title) {
        super();
        TitledBorder border = new TitledBorder(title);
        Color color = new Color(255, 255, 255);

        border.setTitlePosition(TitledBorder.TOP);
        border.setTitleJustification(TitledBorder.CENTER);

        setBackground(color);
        setBorder(border);
    }
}
