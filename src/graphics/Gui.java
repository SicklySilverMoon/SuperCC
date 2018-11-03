package graphics;

import emulator.SuperCC;
import game.Level;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;

public class Gui extends JFrame{
    private JPanel mainPanel;
    private JPanel textPanel;
    private JPanel gamePanel;
    
    private final SuperCC emulator;
    
    public static final int TILE_SIZE = 20;
    
    public GamePanel getGamePanel() {
        return (GamePanel) gamePanel;
    }
    
    public TextPanel getTextPanel() {
        return (TextPanel) textPanel;
    }
    
    private void createUIComponents() {
        textPanel = new TextPanel();
        gamePanel = new GamePanel();
        try {
            ((GamePanel) gamePanel).initialise(ImageIO.read(getClass().getResource("/resources/tw-editor.png")));
        }
        catch (IOException e){
            emulator.throwError("Error loading tileset: "+e.getMessage());
            try {
                ((GamePanel) gamePanel).initialise(ImageIO.read(getClass().getResource("/resources/tw-editor.png")));
            }
            catch (IOException e2){ }
        }
    }
    
    public Gui(SuperCC emulator) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (Exception e){
            e.printStackTrace();
        }
        setTitle("SuperCC");
        try{
            setIconImage(ImageIO.read(getClass().getResource("/resources/icon.png")));
        }
        catch (IOException e){}
        this.emulator = emulator;
        getTextPanel().setEmulator(emulator);
        getGamePanel().setEmulator(emulator);
        getContentPane().addKeyListener(emulator);
        setContentPane(mainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setJMenuBar(new MenuBar(this, emulator));
        pack();
        setVisible(true);
        addKeyListener(emulator);
    }
    
    public void repaint(Level level, boolean fromSratch){
        getGamePanel().updateGraphics(level, fromSratch);
        gamePanel.repaint();
        textPanel.repaint();
    }
    
}
