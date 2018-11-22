package graphics;

import emulator.SuperCC;
import game.Tile;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

import static game.Tile.NUM_BOOTS;
import static game.Tile.NUM_KEYS;
import static graphics.GamePanel.SMALL_NUMERAL_HEIGHT;
import static graphics.Gui.TILE_SIZE;

public class InventoryPanel extends JPanel {
    
    private SuperCC emulator;
    
    private int[][] tileImage;
    private BufferedImage bg;
    
    private static final int BORDER = 2;
    
    public void initialise(SuperCC emulator){
        this.emulator = emulator;
        tileImage = GamePanel.tileImage;
        BufferedImage bbg = new BufferedImage(4*TILE_SIZE, 2*TILE_SIZE, BufferedImage.TYPE_4BYTE_ABGR);
        bg = new BufferedImage(4*TILE_SIZE, 2*TILE_SIZE, BufferedImage.TYPE_4BYTE_ABGR);
        WritableRaster r1 = bbg.getRaster();
        WritableRaster r2 = bg.getRaster();
        for (int i = 0; i < NUM_BOOTS; i++){
            r1.setPixels(i * TILE_SIZE, 0, TILE_SIZE, TILE_SIZE, tileImage[Tile.FLOOR.ordinal()]);
            r2.setPixels(i * TILE_SIZE, 0, TILE_SIZE, TILE_SIZE, tileImage[i + Tile.KEY_BLUE.ordinal()]);
            r1.setPixels(i * TILE_SIZE, TILE_SIZE, TILE_SIZE, TILE_SIZE, tileImage[Tile.FLOOR.ordinal()]);
            r2.setPixels(i * TILE_SIZE, TILE_SIZE, TILE_SIZE, TILE_SIZE, tileImage[i + Tile.BOOTS_WATER.ordinal()]);
        }
        bg = FlattenImage(bbg, bg);
    }
    
    private BufferedImage FlattenImage(Image... images){
        BufferedImage out = new BufferedImage(32*TILE_SIZE, 32*TILE_SIZE, BufferedImage.TYPE_4BYTE_ABGR_PRE);
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
        
        WritableRaster r = bg.getRaster();
        for (int i = 0; emulator.getLevel() != null && i < NUM_KEYS; i++){
            GamePanel.drawNumber(emulator.getLevel().getKeys()[i], GamePanel.blackDigits, r, TILE_SIZE * i,  TILE_SIZE - SMALL_NUMERAL_HEIGHT - 2);
            GamePanel.drawNumber(emulator.getLevel().getBoots()[i], GamePanel.blackDigits, r, TILE_SIZE * i, 2 * TILE_SIZE - SMALL_NUMERAL_HEIGHT - 2);
        }
        g.drawImage(bg, BORDER, BORDER, null);
    }
    
}
