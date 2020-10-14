package tools;

import emulator.SuperCC;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class ChangeTimer {
    private JPanel mainPanel;
    private JSpinner spinner;
    
    public ChangeTimer(SuperCC emulator){
        short[] keys = emulator.getLevel().getKeys();
        SpinnerModel sm = new SpinnerNumberModel(((double) emulator.getLevel().getTimer()) / 100, 0.1, Short.MAX_VALUE, 0.1);
        spinner.setModel(sm);
        JFrame frame = new JFrame("Timer");
        frame.setContentPane(mainPanel);
        frame.pack();
        frame.setLocationRelativeTo(emulator.getMainWindow());
        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                emulator.getLevel().getCheats().setTimer((int) Math.round(10 * ((java.lang.Number) spinner.getValue()).doubleValue()));
                emulator.getMainWindow().getLevelPanel().repaint();
            }
        });
    }
}
