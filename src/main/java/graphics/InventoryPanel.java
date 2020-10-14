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

public class InventoryPanel extends JPanel {
    
    private SuperCC emulator;
    
    private int[][] tileImage;
    private BufferedImage bg;
    private BufferedImage bbg;
    
    private static final int BORDER = 2;
    
    public void initialise(SuperCC emulator) {
        this.emulator = emulator;
        tileImage = GamePanel.tileImage;
        int tileWidth, tileHeight;
        try {
            tileWidth = emulator.getMainWindow().getGamePanel().getTileWidth();
            tileHeight = emulator.getMainWindow().getGamePanel().getTileHeight();
        }
        catch (NullPointerException npe) {
            tileWidth = Gui.DEFAULT_TILE_WIDTH;
            tileHeight = Gui.DEFAULT_TILE_HEIGHT;
        }
        setSize(4*tileWidth + 2*BORDER, 2*tileHeight + 2*BORDER);
        setPreferredSize(new Dimension(4*tileWidth + 2*BORDER, 2*tileHeight + 2*BORDER));
        setMaximumSize(new Dimension(4*tileWidth + 2*BORDER, 2*tileHeight + 2*BORDER));
        setMinimumSize(new Dimension(4*tileWidth + 2*BORDER, 2*tileHeight + 2*BORDER));

        bbg = new BufferedImage(4*tileWidth, 2*tileHeight, BufferedImage.TYPE_4BYTE_ABGR);
        bg = new BufferedImage(4*tileWidth, 2*tileHeight, BufferedImage.TYPE_4BYTE_ABGR);
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
        int tileWidth = emulator.getMainWindow().getGamePanel().getTileWidth();
        int tileHeight = emulator.getMainWindow().getGamePanel().getTileHeight();

        WritableRaster r1 = bbg.getRaster();
        WritableRaster r2 = bg.getRaster();
        for (int i = 0; i < NUM_BOOTS; i++) {
            r1.setPixels(i * tileWidth, 0, tileWidth, tileHeight, tileImage[Tile.FLOOR.ordinal()]);
            r2.setPixels(i * tileWidth, 0, tileWidth, tileHeight, tileImage[i + Tile.KEY_BLUE.ordinal()]);
            r1.setPixels(i * tileWidth, tileHeight, tileWidth, tileHeight, tileImage[Tile.FLOOR.ordinal()]);
            r2.setPixels(i * tileWidth, tileHeight, tileWidth, tileHeight, tileImage[i + Tile.BOOTS_WATER.ordinal()]);
        }
        bg = FlattenImage(bbg, bg);

        for (int i = 0; emulator.getLevel() != null && i < NUM_KEYS; i++) {
            GamePanel.drawNumber(emulator.getLevel().getKeys()[i], GamePanel.blackDigits,
                                           tileWidth * i, tileHeight - SMALL_NUMERAL_HEIGHT - 2, bg.getRaster());
            GamePanel.drawNumber(emulator.getLevel().getBoots()[i], GamePanel.blackDigits,
                                           tileWidth * i, 2 * tileHeight - SMALL_NUMERAL_HEIGHT - 2, bg.getRaster());
        }
        g.drawImage(bg, BORDER, BORDER, null);
    }
    
}
