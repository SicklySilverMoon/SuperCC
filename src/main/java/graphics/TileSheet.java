package graphics;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public enum TileSheet {
    
    CCEDIT_TW("/resources/tw-edit-overlay.png", "/resources/tw-edit-tiles.png", "Tile World (CCEdit)"),
    TW("/resources/tw-overlay.png", "/resources/tw-tiles.png", "Tile World"),
    CCEDIT_MSCC("/resources/ms-edit-overlay.png", "/resources/ms-edit-tiles.png", "MSCC (CCEdit)"),
    MSCC("/resources/ms-overlay.png", "/resources/ms-tiles.png", "MSCC"),
//    CCEDIT_CC2("/resources/cc2-edit-overlay.png", "/resources/cc2-edit-tiles.png", "CC2 (CCEdit)"), //removed on account of not having permission to use and trying to avoid issues
//    CC2("/resources/cc2-overlay.png", "/resources/cc2-tiles.png", "CC2"),
    CCEDIT_BLACK_AND_WHITE("/resources/bw-overlay.png", "/resources/bw-tiles.png", "MSCC (Black and White - Editor)"); //should probably do a non-editor version

    private static final int TILESHEET_WIDTH = 7, TILESHEET_HEIGHT = 16;
    
    private final String urlOverlay;
    private final String urlTiles;
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
    
    TileSheet(String urlOverlay, String urlTiles, String name) {
        this.urlOverlay = urlOverlay;
        this.urlTiles = urlTiles;
        this.name = name;
    }
    
    public BufferedImage[] getTileSheets(int tileWidth, int tileHeight) throws IOException {
        Image tilesPNG = ImageIO.read(Objects.requireNonNull(getClass().getResource(urlTiles)));
        Image tilesPNGResized = tilesPNG.getScaledInstance(tileWidth * TILESHEET_WIDTH, tileHeight * TILESHEET_HEIGHT, Image.SCALE_SMOOTH);
        BufferedImage tilesOut = new BufferedImage(tileWidth * TILESHEET_WIDTH, tileHeight * TILESHEET_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D tGr = tilesOut.createGraphics();
        tGr.drawImage(tilesPNGResized, 0, 0, null);
        tGr.dispose();

        Image overlayPNG = ImageIO.read(Objects.requireNonNull(getClass().getResource(urlOverlay)));
        Image overlayPNGResized = overlayPNG.getScaledInstance(tileWidth * TILESHEET_WIDTH, tileHeight * TILESHEET_HEIGHT, Image.SCALE_SMOOTH);
        BufferedImage overlayOut = new BufferedImage(tileWidth * TILESHEET_WIDTH, tileHeight * TILESHEET_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D oGr = overlayOut.createGraphics();
        oGr.drawImage(overlayPNGResized, 0, 0 ,null);
        oGr.dispose();

        return new BufferedImage[] {tilesOut, overlayOut};
    }
    
}
