package graphics;

import emulator.SuperCC;
import game.Tile;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import static game.Tile.NUM_BOOTS;
import static game.Tile.NUM_KEYS;
import static graphics.FullscreenGamePanel.SMALL_NUMERAL_HEIGHT;

public class InventoryPanel extends JPanel {
    
    private SuperCC emulator;
    
    private int[][] tileImage;
    private BufferedImage bg;
    
    private static final int BORDER = 2;
    
    public void initialise(SuperCC emulator){
        this.emulator = emulator;
        tileImage = FullscreenGamePanel.tileImage;
        int tileSize = 20;
        BufferedImage bbg = new BufferedImage(4*tileSize, 2*tileSize, BufferedImage.TYPE_4BYTE_ABGR);
        bg = new BufferedImage(4*tileSize, 2*tileSize, BufferedImage.TYPE_4BYTE_ABGR);
        WritableRaster r1 = bbg.getRaster();
        WritableRaster r2 = bg.getRaster();
        for (int i = 0; i < NUM_BOOTS; i++){
            r1.setPixels(i * tileSize, 0, tileSize, tileSize, tileImage[Tile.FLOOR.ordinal()]);
            r2.setPixels(i * tileSize, 0, tileSize, tileSize, tileImage[i + Tile.KEY_BLUE.ordinal()]);
            r1.setPixels(i * tileSize, tileSize, tileSize, tileSize, tileImage[Tile.FLOOR.ordinal()]);
            r2.setPixels(i * tileSize, tileSize, tileSize, tileSize, tileImage[i + Tile.BOOTS_WATER.ordinal()]);
        }
        bg = FlattenImage(bbg, bg);
    }
    
    private BufferedImage FlattenImage(Image... images){
        int tileSize = 20;
        BufferedImage out = new BufferedImage(32*tileSize, 32*tileSize, BufferedImage.TYPE_4BYTE_ABGR_PRE);
        Graphics2D g = out.createGraphics();
        for (Image img : images){
            g.drawImage(img, 0, 0, null);
        }
        g.dispose();
        return out;
    }
    
    @Override
    public void paintComponent(Graphics g) {
        
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 0, getWidth(), getHeight());
        int tileSize = emulator.getMainWindow().getGamePanel().getTileSize();
        
        for (int i = 0; emulator.getLevel() != null && i < NUM_KEYS; i++){
            FullscreenGamePanel.drawNumber(emulator.getLevel().getKeys()[i], FullscreenGamePanel.blackDigits,
                                           tileSize * i, tileSize - SMALL_NUMERAL_HEIGHT - 2, bg.getRaster());
            FullscreenGamePanel.drawNumber(emulator.getLevel().getBoots()[i], FullscreenGamePanel.blackDigits,
                                           tileSize * i, 2 * tileSize - SMALL_NUMERAL_HEIGHT - 2, bg.getRaster());
        }
        g.drawImage(bg, BORDER, BORDER, null);
    }
    
}
