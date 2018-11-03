package graphics;

import game.Level;

import javax.swing.*;
import java.awt.*;
import java.nio.charset.StandardCharsets;

public class MovePanel extends TextPanel {
    
    public void resize(JPanel container, JPanel inventory, JPanel levelPanel){
        setMaximumSize(new Dimension(container.getWidth(), container.getHeight() - inventory.getHeight() - levelPanel.getHeight()));
        setLocation(0, levelPanel.getY()+levelPanel.getHeight());
    }
    
    @Override
    public void paintComponent(Graphics g){
        
        if (emulator == null) return;
        Level level = emulator.getLevel();
        if (level == null) return;
        
        textHeight = 40;
    
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 24));
        drawText(g, level.getMoves().toString(StandardCharsets.ISO_8859_1), Integer.MAX_VALUE);
    }
    
}
