package graphics;

import emulator.SuperCC;
import game.Level;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

public class MainWindow extends JFrame {

    public static final int TILE_SIZE = 20;

    private JPanel container;
    GamePanel gamePanel;
    private TextPanel textPanel;
    private MenuBar menuBar;

    public MainWindow(SuperCC emulator) throws IOException{
        addKeyListener(emulator);

        menuBar = new MenuBar(this, emulator);
        setJMenuBar(menuBar);

        container = new JPanel();
        container.setLayout(new BoxLayout(container, BoxLayout.X_AXIS));

        gamePanel = new GamePanel(emulator, ImageIO.read(getClass().getResource("/resources/tw-editor.png")));
        container.add(gamePanel);

        textPanel = new TextPanel(emulator);
        container.add(textPanel);

        getContentPane().add(container);

        pack();
        setResizable(false);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public void repaint(Level level, boolean fromSratch){
        gamePanel.updateGraphics(level, fromSratch);
        gamePanel.repaint();
        textPanel.repaint();
    }

}
