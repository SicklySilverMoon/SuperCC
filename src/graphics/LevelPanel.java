package graphics;

import game.Level;

import java.awt.*;

public class LevelPanel extends TextPanel {
    
    private static String timerToString(Level level){
        int time = level.getTimer();
        if (time < 0) {
            time = level.getTChipTime();
            return "("+Integer.toString(time/10)+"."+Integer.toString(Math.abs(time%10))+")";
        }
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
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        drawText(g, "Level "+level.getLevelNumber()+": ", 1);
        drawText(g, "", 1);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 32));
        drawText(g, new String(level.getTitle()), 3);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        drawText(g, level.getStep().toString()+" step, seed: "+level.getRngSeed(), 1);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 24));
        drawText(g, "", 1);
        drawText(g, "Time: "+ timerToString(level), 1);
        drawText(g, "Chips left: "+level.getChipsLeft(), 1);
        
        if (textHeight != getHeight()){
            setPreferredSize(new Dimension(getWidth(), textHeight));
            setSize(getPreferredSize());
            emulator.getMainWindow().pack();
        }
    }
    
}
