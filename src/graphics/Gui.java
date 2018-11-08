package graphics;

import com.sun.java.swing.plaf.windows.WindowsSliderUI;
import emulator.SavestateManager;
import emulator.SuperCC;
import game.Level;
import emulator.Solution;
import game.SaveState;
import util.TreeNode;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.io.IOException;
import java.util.List;

public class Gui extends JFrame{
    private JPanel mainPanel;
    private JPanel rightContainer;
    private JPanel gamePanel;
    private JPanel levelPanel;
    private JPanel inventoryPanel;
    private JPanel movePanel;
    private JPanel lastActionPanel;
    private JPanel leftPanel;
    private JSlider timeSlider;
    private JSlider speedSlider;
    private JPanel sliderPanel;
    
    public JSlider getTimeSlider(){
        return timeSlider;
    }
    
    private final SuperCC emulator;
    
    public static final int TILE_SIZE = 20;
    
    public static final Color DARK_GREY = new Color(0x3C3F41);
    
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
        speedSlider = new JSlider(0, Solution.NUM_SPEEDS - 1);
        speedSlider.setBackground(DARK_GREY);
        speedSlider.setUI(new WindowsSliderUI(speedSlider));
        timeSlider = new JSlider(0, 0);
        timeSlider.setBackground(DARK_GREY);
        timeSlider.setUI(new WindowsSliderUI(timeSlider));
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
        speedSlider.addChangeListener((e) -> emulator.getSolution().setPlaybackSpeed(speedSlider.getValue()));
        timeSlider.addChangeListener((e) -> {
            if (timeSlider.getValueIsAdjusting()) {
                emulator.getSavestates().playbackRewind(timeSlider.getValue());
                emulator.getLevel().load(emulator.getSavestates().getSavestate());
                emulator.showAction("Solution Rewind");
                emulator.repaint(false);
                emulator.getMainWindow().requestFocus();
            }
        });
        this.emulator = emulator;
        leftPanel.setBackground(DARK_GREY);
        sliderPanel.setBackground(DARK_GREY);
        getMovePanel().setEmulator(emulator);
        getLevelPanel().setEmulator(emulator);
        getInventoryPanel().initialise(emulator);
        getInventoryPanel().setOpaque(true);
        getGamePanel().setEmulator(emulator);
        setContentPane(mainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setJMenuBar(new MenuBar(this, emulator));
        pack();
        setVisible(true);
        rightContainer.setBackground(Color.DARK_GRAY);
        levelPanel.setBackground(Color.DARK_GRAY);
        inventoryPanel.setBackground(Color.DARK_GRAY);
        movePanel.setBackground(Color.DARK_GRAY);
        lastActionPanel.setBackground(Color.DARK_GRAY);
        setFocusable(true);
        gamePanel.setFocusable(true);
        addKeyListener(emulator);
    }
    
    public void updateTimeSlider(SavestateManager manager) {
        List<TreeNode<byte[]>> playbackNodes = manager.getPlaybackNodes();
        timeSlider.setMaximum(playbackNodes.size() - 1);
        timeSlider.setValue(manager.getPlaybackIndex());
    }
    
    public void repaintRightContainer(){
        levelPanel.repaint();
        inventoryPanel.repaint();
        lastActionPanel.repaint();
        movePanel.repaint();
    }
    
    public void repaint(Level level, boolean fromSratch){
        updateTimeSlider(emulator.getSavestates());
        getGamePanel().updateGraphics(level, fromSratch);
        gamePanel.repaint();
        repaintRightContainer();
    }
    
}
