package tools;

import emulator.SuperCC;

import javax.swing.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class ChangeInventory extends JPanel {
    private JPanel mainPanel;
    private JPanel boots2;
    private JPanel keys;
    private JTextField textBlue;
    private JTextField textRed;
    private JTextField textGreen;
    private JTextField textYellow;
    private JPanel boots;
    private JPanel chips;
    private JTextField textChips;
    private JCheckBox checkFlippers;
    private JCheckBox checkFire;
    private JCheckBox checkIce;
    private JCheckBox checkSuction;
    
    private final JTextField[] allKeyTextFields = new JTextField[] {textBlue, textRed, textGreen, textYellow};
    private final JCheckBox[] allBootCheckboxes = new JCheckBox[] {checkFlippers, checkFire, checkIce, checkSuction};
    
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
        for (int i = 0; i < allKeyTextFields.length; i++) keys[i] = (short) (KEYS_MAX_VALUE & Integer.parseInt(allKeyTextFields[i].getText()));
        byte[] boots = emulator.getLevel().getBoots();
        for (int i = 0; i < allBootCheckboxes.length; i++) {
            if (!allBootCheckboxes[i].isSelected()) boots[i] = 0;
            else boots[i] = 1;
        }
        emulator.getLevel().cheats.setChipsLeft(Integer.parseInt(textChips.getText()));
        emulator.getMainWindow().repaint(false);
    }
    
    public ChangeInventory(SuperCC emulator){
        short[] keys = emulator.getLevel().getKeys();
        for (int i = 0; i < allKeyTextFields.length; i++) allKeyTextFields[i].setText(String.valueOf(keys[i]));
        byte[] boots = emulator.getLevel().getBoots();
        for (int i = 0; i < allBootCheckboxes.length; i++) allBootCheckboxes[i].setSelected(boots[i] == 1);
        textChips.setText(String.valueOf(emulator.getLevel().getChipsLeft()));
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
