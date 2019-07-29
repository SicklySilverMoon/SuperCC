package graphics;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public enum TileSheet {
    
    CCEDIT_TW("/resources/32-ccedit-tw.png", "Tile World (Editor)"),
    TW("/resources/48-tw.png", "Tile World"),
    CCEDIT_MSCC("/resources/32-ccedit-mscc.png", "MSCC (Editor)"),
    MSCC("/resources/32-mscc.png", "MSCC");

    private static final int TILESHEET_WIDTH = 7, TILESHEET_HEIGHT = 16;
    
    private final String url;
    private final String name;
    
    private static String[] names;
    
    public static String[] getNames() {
        if (names == null) {
            TileSheet[] allTileSheets = TileSheet.values();
            names = new String[allTileSheets.length];
            for (int i = 0; i < allTileSheets.length; i++) {
                names[i] = allTileSheets[i].name;
            }
        }
        return names;
    }
    
    TileSheet(String url, String name) {
        this.url = url;
        this.name = name;
    }
    
    public BufferedImage getTileSheet(int tileWidth, int tileHeight) throws IOException {
        Image tilesPNG = ImageIO.read(getClass().getResource(url));
        Image tilesPNGResized = tilesPNG.getScaledInstance(tileWidth * TILESHEET_WIDTH, tileHeight * TILESHEET_HEIGHT, Image.SCALE_SMOOTH);
        BufferedImage out = new BufferedImage(tileWidth*7, tileHeight*16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D bGr = out.createGraphics();
        bGr.drawImage(tilesPNGResized, 0, 0, null);
        bGr.dispose();
        return out;
    }
    
}
