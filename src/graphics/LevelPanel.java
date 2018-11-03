package graphics;

import game.Level;

import java.awt.*;
import java.nio.charset.StandardCharsets;

public class LevelPanel extends TextPanel {
    
    private static String timerToString(int time){
        if (time < 0) return "---";
        return Integer.toString(time/10)+"."+Integer.toString(time%10);
    }
    
    @Override
    public void paintComponent(Graphics g){
        
        if (emulator == null) return;
        Level level = emulator.getLevel();
        if (level == null) return;
        
        //setSize(emulator.getMainWindow().getRightContainer().getWidth(), getHeight());
        
        textHeight = 40;
    
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 32));
        drawText(g, new String(level.title), 3);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        drawText(g, level.getStep().toString()+" step, seed: "+level.getRngSeed(), 1);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 24));
        drawText(g, "", 1);
        drawText(g, "Time: "+ timerToString(level.getTimer()), 1);
        drawText(g, "Chips left: "+level.getChipsLeft(), 1);
        
        if (textHeight != getHeight()){
            setPreferredSize(new Dimension(getWidth(), textHeight));
            setSize(getPreferredSize());
            emulator.getMainWindow().repaintRightContainer();
        }
    }
    
}
