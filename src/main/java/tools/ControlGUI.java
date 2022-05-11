package tools;

import emulator.EmulatorKeyListener;
import emulator.SuperCC;
import io.SuccPaths;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ControlGUI {
    
    private JPanel mainPanel;
    private JPanel MovementPanel;
    private JButton upButton;
    private JButton downButton;
    private JButton leftButton;
    private JButton rightButton;
    private JButton halfWaitButton;
    private JButton fullWaitButton;
    private JTabbedPane tabbedPane1;
    private JButton rewindButton;
    private JButton forwardButton;
    private JPanel toolPanel;
    private JButton upLeftButton;
    private JButton downLeftButton;
    private JButton downRightButton;
    private JButton upRightButton;
    private JCheckBox autoDiagonals;

    private SuperCC emulator;
    
    public ControlGUI(SuperCC emulator) {
        this.emulator = emulator;
        JButton[] buttons = new JButton[] {upButton, leftButton, downButton, rightButton, upLeftButton, downLeftButton,
                downRightButton, upRightButton, halfWaitButton, fullWaitButton, rewindButton, forwardButton};
        
        for (JButton button : buttons) {
            KeyRemapButton krb = (KeyRemapButton) button;
            krb.setText(KeyEvent.getKeyText(krb.key.getKeyCode()));
            if (krb.key.getKeyCode() == KeyEvent.VK_ESCAPE) {
                krb.setText("Disabled");
            }
            else {
                krb.setText(KeyEvent.getKeyText(krb.key.getKeyCode()));
            }
        }
        autoDiagonals.setSelected(emulator.getPaths().getControls().autoDiagonals);
        updateDiagonalButtons();

        JFrame frame = new JFrame("Controls");
        frame.setContentPane(mainPanel);
        frame.pack();
        frame.setLocationRelativeTo(emulator.getMainWindow());
        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                SuccPaths.Controls controls = new SuccPaths.Controls();
                for (int i = 0; i < buttons.length; i++) {
                    controls.keys[i] = ((KeyRemapButton) buttons[i]).key.getKeyCode();
                }
                controls.autoDiagonals = autoDiagonals.isSelected();
                emulator.getPaths().setControls(controls);
            }
        });
        autoDiagonals.addChangeListener((e) -> {
            updateDiagonalButtons();
        });
    }
    private void updateDiagonalButtons() {
        boolean diagonalsEnabled = !autoDiagonals.isSelected();
        upRightButton.setEnabled(diagonalsEnabled);
        upLeftButton.setEnabled(diagonalsEnabled);
        downRightButton.setEnabled(diagonalsEnabled);
        downLeftButton.setEnabled(diagonalsEnabled);
    }
    private void createUIComponents() {
        upButton = new KeyRemapButton(EmulatorKeyListener.Key.UP);
        leftButton = new KeyRemapButton(EmulatorKeyListener.Key.LEFT);
        downButton = new KeyRemapButton(EmulatorKeyListener.Key.DOWN);
        rightButton = new KeyRemapButton(EmulatorKeyListener.Key.RIGHT);
        upLeftButton = new KeyRemapButton(EmulatorKeyListener.Key.UP_LEFT);
        downLeftButton = new KeyRemapButton(EmulatorKeyListener.Key.DOWN_LEFT);
        downRightButton = new KeyRemapButton(EmulatorKeyListener.Key.DOWN_RIGHT);
        upRightButton = new KeyRemapButton(EmulatorKeyListener.Key.UP_RIGHT);
        halfWaitButton = new KeyRemapButton(EmulatorKeyListener.Key.HALF_WAIT);
        fullWaitButton = new KeyRemapButton(EmulatorKeyListener.Key.FULL_WAIT);
        rewindButton = new KeyRemapButton(EmulatorKeyListener.Key.REWIND);
        forwardButton = new KeyRemapButton(EmulatorKeyListener.Key.FORWARD);
    }
    
    private class KeyRemapButton extends JButton {
        
        private EmulatorKeyListener.Key key;
        
        KeyRemapButton(EmulatorKeyListener.Key key) {
            this.key = key;
            addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {

                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        KeyRemapButton.super.setText("Disabled");
                    }
                    else {
                        KeyRemapButton.super.setText(KeyEvent.getKeyText(e.getKeyCode()));
                    }
                    emulator.getControls().setKeyCode(key, e.getKeyCode());
                }
            });
        }
    
    }
    
}
