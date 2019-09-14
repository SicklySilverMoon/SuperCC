package tools;

import emulator.SuperCC;

import javax.swing.*;

public class HelpWindow {
    private JPanel mainPanel;
    private JTextPane mainText;

    public HelpWindow(SuperCC emulator) {
        JFrame frame = new JFrame("Help");
        frame.setContentPane(mainPanel);
        frame.pack();
        frame.setLocationRelativeTo(emulator.getMainWindow());
        frame.setVisible(true);
    }
}
