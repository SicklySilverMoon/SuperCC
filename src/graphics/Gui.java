package graphics;

import emulator.SuperCC;
import game.Level;

import javax.imageio.ImageIO;
import javax.sound.midi.Soundbank;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;

public class Gui extends JFrame{
    private JPanel mainPanel;
    private JPanel rightContainer;
    private JPanel gamePanel;
    private JPanel levelPanel;
    private JPanel inventoryPanel;
    private JPanel movePanel;
    private JPanel lastActionPanel;
    
    private final SuperCC emulator;
    
    public static final int TILE_SIZE = 20;
    
    public GamePanel getGamePanel() {
        return (GamePanel) gamePanel;
    }
    
    public LevelPanel getLevelPanel() {
        return (LevelPanel) levelPanel;
    }
    
    public MovePanel getMovePanel() {
        return (MovePanel) movePanel;
    }
    
    public LastActionPanel getLastActionPanel() {
        return (LastActionPanel) lastActionPanel;
    }
    
    public InventoryPanel getInventoryPanel() {
        return (InventoryPanel) inventoryPanel;
    }
    
    public JPanel getRightContainer() {
        return rightContainer;
    }
    
    private void createUIComponents() {
        levelPanel = new LevelPanel();
        inventoryPanel = new InventoryPanel();
        movePanel = new MovePanel();
        gamePanel = new GamePanel();
        lastActionPanel = new LastActionPanel();
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
        try{
            setIconImage(ImageIO.read(getClass().getResource("/resources/icons/windowIcon.png")));
        }
        catch (IOException e){}
        this.emulator = emulator;
        getMovePanel().setEmulator(emulator);
        getLevelPanel().setEmulator(emulator);
        getInventoryPanel().initialise(emulator);
        getInventoryPanel().setOpaque(true);
        getGamePanel().setEmulator(emulator);
        getContentPane().addKeyListener(emulator);
        setContentPane(mainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setJMenuBar(new MenuBar(this, emulator));
        pack();
        setVisible(true);
        addKeyListener(emulator);
        rightContainer.setBackground(Color.DARK_GRAY);
        levelPanel.setBackground(Color.DARK_GRAY);
        inventoryPanel.setBackground(Color.DARK_GRAY);
        movePanel.setBackground(Color.DARK_GRAY);
        lastActionPanel.setBackground(Color.DARK_GRAY);
    }
    
    public void repaintRightContainer(){
        levelPanel.repaint();
        inventoryPanel.repaint();
        lastActionPanel.repaint();
        movePanel.repaint();
    }
    
    public void repaint(Level level, boolean fromSratch){
        getGamePanel().updateGraphics(level, fromSratch);
        gamePanel.repaint();
        repaintRightContainer();
    }
    
}
