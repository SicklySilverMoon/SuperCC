package tools;

import emulator.SuperCC;

import javax.swing.*;

public class TSPHelpWindow {
    private JPanel mainPanel;
    private JTextPane mainText;

    public TSPHelpWindow(SuperCC emulator) {
        JFrame frame = new JFrame("Help");
        frame.setContentPane(mainPanel);
        frame.pack();
        frame.setLocationRelativeTo(emulator.getMainWindow());
        frame.setVisible(true);
    }
}
