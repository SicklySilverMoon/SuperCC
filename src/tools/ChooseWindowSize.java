package tools;

import emulator.SuperCC;
import graphics.Gui;
import graphics.SmallGamePanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;

public class ChooseWindowSize {
    private JPanel panel1;
    private JButton okButton;
    private JSpinner heightSpinner;
    private JSpinner widthSpinner;
    
    private static final int MIN_WINDOW_SIZE = 1, MAX_WINDOW_SIZE = 32;
    
    public ChooseWindowSize(SuperCC emulator){
        SmallGamePanel gamePanel = (SmallGamePanel) emulator.getMainWindow().getGamePanel();
        SpinnerModel sm = new SpinnerNumberModel(gamePanel.getWindowSizeY(), MIN_WINDOW_SIZE, MAX_WINDOW_SIZE, 1);
        heightSpinner.setModel(sm);
        sm = new SpinnerNumberModel(gamePanel.getWindowSizeY(), MIN_WINDOW_SIZE, MAX_WINDOW_SIZE, 1);
        widthSpinner.setModel(sm);
        JFrame frame = new JFrame("Choose Dimensions");
        frame.setContentPane(panel1);
        frame.pack();
        frame.setLocationRelativeTo(emulator.getMainWindow());
        frame.setVisible(true);
        okButton.addActionListener((e) -> {
            int width = ((Number) widthSpinner.getValue()).intValue();
            int height = ((Number) heightSpinner.getValue()).intValue();
            gamePanel.setWindowSize(width, height);
            int tileWidth = gamePanel.getTileWidth(), tileHeight = gamePanel.getTileHeight();
            Gui window = emulator.getMainWindow();
            window.setSize(200+tileWidth*width, 200+tileHeight*height);
            window.getGamePanel().setPreferredSize(new Dimension(tileWidth*width, tileHeight*height));
            window.getGamePanel().setSize(tileWidth*width, tileHeight*height);
            window.pack();
            window.repaint(true);
            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
        });
    }
    
}
