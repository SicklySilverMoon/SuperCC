package graphics;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

enum TileSheet {
    
    CCEDIT_TW("/resources/32-ccedit-tw.png"),
    CCEDIT_MSCC("/resources/32-ccedit-mscc.png"),
    TW("/resources/48-tw.png"),
    MSCC("/resources/32-mscc.png");
    
    private static final int TILESHEET_WIDTH = 7, TILESHEET_HEIGHT = 16;
    
    private final String url;
    
    TileSheet(String url) {
        this.url = url;
    }
    
    public BufferedImage getTileSheet(int tileWidth, int tileHeight) throws IOException {
        Image tilesPNG = ImageIO.read(getClass().getResource(url));
        Image tilesPNGResized = tilesPNG.getScaledInstance(tileWidth * TILESHEET_WIDTH, tileHeight * TILESHEET_HEIGHT, Image.SCALE_SMOOTH);
        BufferedImage out = new BufferedImage(37*7, 37*16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D bGr = out.createGraphics();
        bGr.drawImage(tilesPNGResized, 0, 0, null);
        bGr.dispose();
        return out;
    }
    
}
