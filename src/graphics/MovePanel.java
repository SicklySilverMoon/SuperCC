package graphics;

import game.Level;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.nio.charset.StandardCharsets;

public class MovePanel extends TextPanel implements MouseWheelListener {
    
    private static final int SCROLL_WHEEL_MULTIPLIER = 8,
                             MIN_SCROLL = 32;
    
    private int heightOffset = MIN_SCROLL;
    
    @Override
    public void paintComponent(Graphics g){
        
        if (emulator == null) return;
        Level level = emulator.getLevel();
        if (level == null) return;
        
        textHeight = heightOffset;
    
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 24));
        drawText(g, level.getMoves().toString(StandardCharsets.ISO_8859_1), Integer.MAX_VALUE);
    }
    
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        int i = e.getUnitsToScroll();
        heightOffset -= i * SCROLL_WHEEL_MULTIPLIER;
        if (heightOffset > MIN_SCROLL) heightOffset = MIN_SCROLL;
        repaint();
    }
    
    public MovePanel(){
        addMouseWheelListener(this);
    }
    
}
