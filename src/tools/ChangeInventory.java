package tools;

import emulator.SuperCC;

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class ChangeInventory extends JPanel {
    private JPanel mainPanel;
    private JPanel boots2;
    private JPanel keys;
    private JSpinner spinnerBlue;
    private JSpinner spinnerRed;
    private JSpinner spinnerGreen;
    private JSpinner spinnerYellow;
    private JPanel boots;
    private JPanel chips;
    private JSpinner spinnerChips;
    private JSpinner spinnerFlippers;
    private JSpinner spinnerFire;
    private JSpinner spinnerIce;
    private JSpinner spinnerSuction;
    
    private final JSpinner[] allKeySpinners = new JSpinner[] {spinnerBlue, spinnerRed, spinnerGreen, spinnerYellow};
    private final JSpinner[] allBootSpinners = new JSpinner[] {spinnerFlippers, spinnerFire, spinnerIce, spinnerSuction};
    
    public static final int KEYS_MAX_VALUE = 2 * 32 * 32 - 1;
    
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setContentPane(new ChangeInventory().mainPanel);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
    
    private ChangeInventory(){}
    
    public void updateChanges(SuperCC emulator){
        short[] keys = emulator.getLevel().getKeys();
        for (int i = 0; i < allKeySpinners.length; i++) keys[i] = (short) (KEYS_MAX_VALUE & ((java.lang.Number) allKeySpinners[i].getValue()).intValue());
        byte[] boots = emulator.getLevel().getBoots();
        for (int i = 0; i < allKeySpinners.length; i++) {
            if (((java.lang.Number) allBootSpinners[i].getValue()).intValue() == 0) boots[i] = 0;
            else boots[i] = 1;
        }
        emulator.getLevel().cheats.setChipsLeft((short) (KEYS_MAX_VALUE & ((java.lang.Number) spinnerChips.getValue()).intValue()));
        emulator.getMainWindow().repaint(emulator.getLevel(), false);
    }
    
    public ChangeInventory(SuperCC emulator){
        short[] keys = emulator.getLevel().getKeys();
        for (int i = 0; i < allKeySpinners.length; i++) allKeySpinners[i].setValue(keys[i]);
        byte[] boots = emulator.getLevel().getBoots();
        for (int i = 0; i < allBootSpinners.length; i++) allBootSpinners[i].setValue(boots[i]);
        spinnerChips.setValue(emulator.getLevel().getChipsLeft());
        JFrame frame = new JFrame("Inventory");
        frame.setContentPane(mainPanel);
        frame.pack();
        frame.setLocationRelativeTo(emulator.getMainWindow());
        frame.setVisible(true);
        frame.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
    
            @Override
            public void windowClosing(WindowEvent e) {
                updateChanges(emulator);
            }
    
            @Override
            public void windowClosed(WindowEvent e) {}
    
            @Override
            public void windowIconified(WindowEvent e) {}
    
            @Override
            public void windowDeiconified(WindowEvent e) {}
    
            @Override
            public void windowActivated(WindowEvent e) {}
    
            @Override
            public void windowDeactivated(WindowEvent e) {}
        });
    }
}
