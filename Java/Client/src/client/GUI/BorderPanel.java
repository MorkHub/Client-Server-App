package client.GUI;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;

public class BorderPanel extends JPanel {
    public BorderPanel(String title) {
        super();

        TitledBorder border = new TitledBorder(title);
        border.setTitleJustification(TitledBorder.CENTER);
        border.setTitlePosition(TitledBorder.TOP);
        setBackground(new Color(255,255,255));

        setLayout(new BorderLayout());
        setBorder(border);
    }
}
