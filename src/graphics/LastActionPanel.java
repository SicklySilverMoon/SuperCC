package graphics;

import game.Level;

import javax.sound.midi.Soundbank;
import java.awt.*;
import java.nio.charset.StandardCharsets;

public class LastActionPanel extends TextPanel {
    
    private String lastAction = "";
    
    public void update(String s){
        lastAction = s;
    }

    @Override
    public void paintComponent(Graphics g){
    
        textHeight = 28;
    
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 16));
        drawText(g, lastAction, 1);
    }

}
