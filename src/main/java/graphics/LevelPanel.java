package graphics;

import game.Level;

import java.awt.*;

public class LevelPanel extends TextPanel {

    private static boolean twsNotation;

    private static String timerToString(Level level){
        boolean untimed = level.isUntimed();
        int timePerSecond = level.ticksPerMove()*5;
        int twsMax = timePerSecond == 10 ? 90 : 95;
        String decimalPoints = ".%0" + (timePerSecond == 10 ? 1 : 2) + "d"; //%02d in lynx so that .05 renders correctly

        int time;
        if (untimed)
            time = level.getTChipTime();
        else
            time = level.getTimer();
        int integerPart = time / 100;
        int fractionalPart = Math.abs(time % 100);

        if (twsNotation)
            fractionalPart = twsMax - fractionalPart;
        if (timePerSecond == 10)
            fractionalPart /= 10; //correction for MS so that it won't display with a trailing 0

        String formatString = decimalPoints;
        if (twsNotation)
            formatString = " (-" + formatString + ")";
        formatString = "%d" + formatString;
        if (untimed)
            formatString = "[" + formatString + "]";
        return String.format(formatString, integerPart, fractionalPart);
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
        drawText(g, level.getTitle(), 3);

        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        drawText(g, "Password: " + level.getPassword(), 1);
        drawText(g, "Ruleset: " + level.getRuleset().prettyPrint(), 1);
        if (level.getAuthor() != null)
            drawText(g, "Author: " + level.getAuthor(), 1);
        drawText(g, level.getStep().toString()+" step, seed: " + level.getRngSeed(), 1);
        if (level.hasCyclicRFF())
            drawText(g, "RFF Initial Direction: "+level.getInitialRFFDirection(), 1);

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
