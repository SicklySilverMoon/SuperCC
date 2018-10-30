package graphics;

import emulator.SuperCC;
import game.Level;

import javax.swing.*;
import java.awt.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class TextPanel extends JPanel{

    private final int BORDER_HORIZONTAL = 20;

    private final SuperCC emulator;

    private int textHeight;

    public TextPanel(SuperCC emulator){
        this.emulator = emulator;
        setPreferredSize(new Dimension(320, 32*20));
        setBackground(Color.BLACK);
    }

    private static String addLineBreaks(String str, int lineWidth, FontMetrics m){
        int width = 0, i, cutoff = 0;
        for (i = 0; i < str.length(); i++){
            char ch = str.charAt(i);
            width += m.charWidth(ch);
            if (width > lineWidth){
                //i--;
                break;
            }
            if (ch == ' ') cutoff = i;
        }
        if (width <= lineWidth) return str;
        if (cutoff == 0) cutoff = i;
        return str.substring(0, cutoff) + "\n" + addLineBreaks(str.substring(cutoff), lineWidth, m);
    }

    public static String timerToString(int time){
        if (time < 0) return "---";
        return Integer.toString(time/10)+"."+Integer.toString(time%10);
    }

    private void drawText(Graphics g, String text, int maxLines){
        text = addLineBreaks(text, getWidth() - 2 * BORDER_HORIZONTAL, g.getFontMetrics());
        String[] textLines = text.split("\n");
        for (int i = 0; i < maxLines; i++){
            if (textLines.length == i) break;
            g.drawString(textLines[i], BORDER_HORIZONTAL, textHeight);
            textHeight += g.getFont().getSize();
        }
    }

    private void drawColoredShorts(Graphics g, short[] a, Color[] colors){
        Color lastColor = g.getColor();
        for (int i = 0; i < a.length; i++){
            g.setColor(colors[i]);
            g.drawString(Short.toString(a[i]), BORDER_HORIZONTAL+20*i, textHeight);
        }
        textHeight += g.getFont().getSize();
        g.setColor(lastColor);
    }

    @Override
    public void paintComponent(Graphics g){

        Level level = emulator.getLevel();
        if (level == null) return;

        textHeight = 40;

        setBackground(Color.BLACK);
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
        drawText(g, "Keys:", 1);
        drawColoredShorts(g, level.getKeys(), new Color[] {Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW});
        drawText(g, "Boots:",  1);
        drawColoredShorts(g, level.getBoots(), new Color[] {Color.BLUE, Color.RED, Color.WHITE, Color.GREEN});
        drawText(g, "", 1);
        g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 24));
        drawText(g, level.getMoves().toString(StandardCharsets.ISO_8859_1), Integer.MAX_VALUE);
    }

}
