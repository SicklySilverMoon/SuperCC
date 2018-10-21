package graphics;

import emulator.SuperCC;
import game.Level;
import game.Tile;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import static graphics.MainWindow.TILE_SIZE;

class GamePanel extends JPanel{

    BufferedImage bg = new BufferedImage(32*TILE_SIZE, 32*TILE_SIZE, BufferedImage.TYPE_4BYTE_ABGR);
    BufferedImage fg = new BufferedImage(32*TILE_SIZE, 32*TILE_SIZE, BufferedImage.TYPE_4BYTE_ABGR);
    BufferedImage bbg = new BufferedImage(32*TILE_SIZE, 32*TILE_SIZE, BufferedImage.TYPE_4BYTE_ABGR);

    private Tile[] previousFG = new Tile[32*32];
    private SuperCC emulator;

    // All 7*16 tile types are preloaded and stored here for fast access.
    private final int[][] tileImage = new int[7*16][TILE_SIZE*TILE_SIZE*4];

    @Override
    public void paintComponent(Graphics g){
        g.drawImage(bbg, 0, 0, null);
        g.drawImage(bg, 0, 0, null);
        g.drawImage(fg, 0, 0, null);
    }

    void updateGraphics(Level level, boolean fromScratch){

        Tile[] layerBG;
        Tile[] layerFG;

        try{
            layerFG = level.getLayerFG();
            layerBG = level.getLayerBG();
        }
        catch (NullPointerException npe){
            return;
        }

        WritableRaster rasterFG = fg.getRaster();
        WritableRaster rasterBG = bg.getRaster();

        for (int i = 0; i < 32*32; i++){
            if (fromScratch | layerFG[i] != previousFG[i]){
                int x = TILE_SIZE*(i%32), y = TILE_SIZE*(i/32);
                rasterBG.setPixels(x, y, TILE_SIZE, TILE_SIZE, tileImage[layerBG[i].ordinal()]);
                rasterFG.setPixels(x, y, TILE_SIZE, TILE_SIZE, tileImage[layerFG[i].ordinal()]);
            }
        }
        previousFG = layerFG.clone();
    }

    public GamePanel(SuperCC emulator, String tilespngPath) throws IOException {
        this.emulator = emulator;
        setPreferredSize(new Dimension(32*TILE_SIZE, 32*TILE_SIZE));
        BufferedImage allTiles = ImageIO.read(new File(tilespngPath));
        addMouseListener(new GameMouseListener());
        for (int i = 0; i < 16 * 7; i++) {
            int x = i / 16;
            int y = i % 16;
            allTiles.getRaster().getPixels(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE, tileImage[i]);
        }

        WritableRaster bbgRaster = bbg.getRaster();
        for (int i = 0; i < 32*32; i++){
            int x = TILE_SIZE * (i % 32), y = TILE_SIZE * (i / 32);
            bbgRaster.setPixels(x, y, TILE_SIZE, TILE_SIZE, tileImage[0]);
        }
    }
    
    private class GameMouseListener implements MouseListener{
        @Override
        public void mouseClicked(MouseEvent e) {
            int x = e.getX() / TILE_SIZE;
            int y = e.getY() / TILE_SIZE;
            emulator.tick(x + (y << 5));
        }
        
        @Override
        public void mousePressed(MouseEvent e) {}
    
        @Override
        public void mouseReleased(MouseEvent e) {}
    
        @Override
        public void mouseEntered(MouseEvent e) {}
    
        @Override
        public void mouseExited(MouseEvent e) {}
    }

}
