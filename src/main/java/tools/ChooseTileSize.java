package tools;

import emulator.SuperCC;
import graphics.Gui;
import graphics.SmallGamePanel;
import graphics.TileSheet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class ChooseTileSize {
    private JPanel panel1;
    private JSpinner heightSpinner;
    private JSpinner widthSpinner;
    private JButton okButton;
    
    public ChooseTileSize(SuperCC emulator, int defaultValue, int min, int max){
        short[] keys = emulator.getLevel().getKeys();
        SpinnerModel sm = new SpinnerNumberModel(defaultValue, min, max, 1);
        heightSpinner.setModel(sm);
        sm = new SpinnerNumberModel(defaultValue, min, max, 1);
        widthSpinner.setModel(sm);
        JFrame frame = new JFrame("Choose Dimensions");
        frame.setContentPane(panel1);
        frame.pack();
        frame.setLocationRelativeTo(emulator.getMainWindow());
        frame.setVisible(true);
        okButton.addActionListener((e) -> {
            try {
                TileSheet ts;
                try {
                    ts = emulator.getMainWindow().getGamePanel().getTileSheet();
                }
                catch (NullPointerException npe) {
                    ts = Gui.DEFAULT_TILESHEET;
                }
                int width = ((Number) widthSpinner.getValue()).intValue();
                int height = ((Number) heightSpinner.getValue()).intValue();
                Gui window = emulator.getMainWindow();
                SmallGamePanel gamePanel = (SmallGamePanel) window.getGamePanel();
                window.getGamePanel().initialise(emulator, ts.getTileSheets(emulator, width, height), ts, width, height);
                window.getInventoryPanel().initialise(emulator);
                window.getInventoryPanel().repaint();
                window.setSize(200+width*gamePanel.getWindowSizeX(), 200+height*gamePanel.getWindowSizeY());
                window.getGamePanel().setPreferredSize(new Dimension(width * gamePanel.getWindowSizeX(), height * gamePanel.getWindowSizeY()));
                window.getGamePanel().setSize(width*gamePanel.getWindowSizeX(), height*gamePanel.getWindowSizeY());
                window.pack();
                window.repaint(true);

                emulator.getPaths().setTileSizes(new int[]{width, height});
            }
            catch (IOException e1) {
                emulator.throwError(e1.getMessage());
            }
            frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
        });
    }
    
}
