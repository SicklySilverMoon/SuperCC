package graphics;

import emulator.EmulatorKeyListener;
import emulator.SavestateManager;
import emulator.SuperCC;
import util.TreeNode;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.plaf.basic.BasicSliderUI;
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
    private JButton playButton;
    
    static final int DEFAULT_TILE_WIDTH = 20;
    static final int DEFAULT_TILE_HEIGHT = 20;
    public static final TileSheet DEFAULT_TILESHEET = TileSheet.CCEDIT_TW;
    
    public JSlider getTimeSlider(){
        return timeSlider;
    }
    
    private final SuperCC emulator;

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
    
    public JButton getPlayButton() {
        return playButton;
    }
    
    private void createUIComponents() {
        playButton = new JButton();
        playButton.setOpaque(false);
        playButton.setBorder(null);
        levelPanel = new LevelPanel();
        inventoryPanel = new InventoryPanel();
        movePanel = new MovePanel();
        gamePanel = new SmallGamePanel(32, 32);
        gamePanel.setPreferredSize(new Dimension(32*DEFAULT_TILE_WIDTH, 32*DEFAULT_TILE_HEIGHT));
        lastActionPanel = new LastActionPanel();
        speedSlider = new JSlider(0, SavestateManager.NUM_SPEEDS - 1);
        speedSlider.setBackground(DARK_GREY);
        speedSlider.setUI(new BasicSliderUI(speedSlider));
        timeSlider = new JSlider(0, 0);
        timeSlider.setBackground(DARK_GREY);
        timeSlider.setUI(new BasicSliderUI(timeSlider));
        try {
            ((GamePanel) gamePanel).initialise(emulator, DEFAULT_TILESHEET.getTileSheet(DEFAULT_TILE_WIDTH, DEFAULT_TILE_HEIGHT), DEFAULT_TILESHEET, DEFAULT_TILE_WIDTH, DEFAULT_TILE_HEIGHT);
            playButton.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/resources/icons/play.gif"))));
        }
        catch (IOException e){
            emulator.throwError("Error loading tileset: "+e.getMessage());
            try {
                ((GamePanel) gamePanel).initialise(emulator, ImageIO.read(getClass().getResource("/resources/tw-editor.png")), TileSheet.CCEDIT_TW, DEFAULT_TILE_WIDTH, DEFAULT_TILE_HEIGHT);
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
        playButton.addActionListener((e) -> {
            emulator.getMainWindow().requestFocus();
            emulator.getSavestates().togglePause();
            try {
                if (emulator.getSavestates().isPaused()) {
                    emulator.showAction("Pausing solution playback");
                    playButton.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/resources/icons/play.gif"))));
                }
                else {
                    emulator.showAction("Playing solution");
                    playButton.setIcon(new ImageIcon(ImageIO.read(getClass().getResource("/resources/icons/pause.gif"))));
                    new Thread(() -> emulator.getSavestates().play(emulator)).start();
                }
            }
            catch (IOException exc) {
                exc.printStackTrace();
            }
        });
        speedSlider.addChangeListener((e) -> {
            emulator.getSavestates().setPlaybackSpeed(speedSlider.getValue());
            emulator.getMainWindow().requestFocus();
        });
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
        EmulatorKeyListener keyListener = new EmulatorKeyListener(emulator);
        addKeyListener(keyListener);
        emulator.setControls(keyListener);
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
    
    public void repaint(boolean fromScratch){
        updateTimeSlider(emulator.getSavestates());
        getGamePanel().updateGraphics(fromScratch);
        leftPanel.repaint();
        gamePanel.repaint();
        repaintRightContainer();
    }
    
}
