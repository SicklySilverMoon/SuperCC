package graphics;

import emulator.SuperCC;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

import static emulator.SuperCC.*;

public abstract class TextPanel extends JPanel{

    public static final int BORDER_HORIZONTAL = 16;
    
    public void setEmulator(SuperCC emulator) {
        this.emulator = emulator;
    }
    
    protected SuperCC emulator;

    protected int textHeight;
    
    private static String[] addLineBreaks(String str, int lineWidth, FontMetrics m) {
        ArrayList<String> lines = new ArrayList<>();

        StringBuilder builder = new StringBuilder();
        int len = 0;
        for (char c : str.toCharArray()) {
            if (len + m.charWidth(c) > lineWidth) {
                lines.add(builder.toString());
                builder = new StringBuilder();
                len = 0;
            }
            len += m.charWidth(c);
            builder.append(c);
        }
        lines.add(builder.toString());

        return lines.toArray(new String[0]);
    }

    protected void drawText(Graphics g, String text, int maxLines){
        String[] lines = addLineBreaks(text, getWidth() - 2 * BORDER_HORIZONTAL, g.getFontMetrics());
        for (int i = 0; i < maxLines; i++){
            if (lines.length == i) break;
            g.drawString(lines[i], BORDER_HORIZONTAL, textHeight);
            textHeight += g.getFont().getSize();
        }
    }

}
