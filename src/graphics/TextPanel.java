package graphics;

import emulator.SuperCC;
import game.Level;

import javax.swing.*;
import java.awt.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.EmptyStackException;

public abstract class TextPanel extends JPanel{

    public static final int BORDER_HORIZONTAL = 20;
    
    public void setEmulator(SuperCC emulator) {
        this.emulator = emulator;
    }
    
    protected SuperCC emulator;

    protected int textHeight;
    
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
        if (i <= 0) return str;
        if (width <= lineWidth) return str;
        if (cutoff == 0) cutoff = i;
        return str.substring(0, cutoff) + "\n" + addLineBreaks(str.substring(cutoff), lineWidth, m);
    }

    protected void drawText(Graphics g, String text, int maxLines){
        text = addLineBreaks(text, getWidth() - 2 * BORDER_HORIZONTAL, g.getFontMetrics());
        String[] textLines = text.split("\n");
        for (int i = 0; i < maxLines; i++){
            if (textLines.length == i) break;
            g.drawString(textLines[i], BORDER_HORIZONTAL, textHeight);
            textHeight += g.getFont().getSize();
        }
    }

}
