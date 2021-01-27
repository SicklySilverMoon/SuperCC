package graphics;

import game.Level;

import java.awt.*;

public class LevelPanel extends TextPanel {

    private static boolean twsNotation;

    private static String timerToString(Level level){
        int timePerSecond = level.ticksPerMove()*5;
        int twsMax = timePerSecond == 10 ? 90 : 95;

        int time;
        if (level.isUntimed()) {
            time = level.getTChipTime();
            if (!twsNotation) return String.format("[%d.%d]",
                    time / 100,
                    Math.abs(time % 100));
            else return String.format("[%d (-.%d)]",
                    time / 100,
                    twsMax - Math.abs(time % 100));
        }
        time = level.getTimer();
        if (!twsNotation)
            return String.format("%d.%d",
                time / 100,
                Math.abs(time % 100));
        else return String.format("%d (-.%d)",
                time / 100,
                twsMax - Math.abs(time % 100));
    }

    public void changeNotation(boolean change) {
        twsNotation = change;
        emulator.getPaths().setTWSNotation(change);
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
        if (level.hasCyclicRFF()) drawText(g, "RFF Initial Direction: "+level.getInitialRFFDirection(), 1);
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
