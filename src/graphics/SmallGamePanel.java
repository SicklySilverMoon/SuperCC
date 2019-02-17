package graphics;

import game.*;
import game.button.ConnectionButton;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.List;

public class SmallGamePanel extends GamePanel {
    
    private Position screenBottomRight;                 // not included
    private int windowSizeX, windowSizeY;
    private byte[] previousFG = new byte[32*32];
    private byte[] previousBG = new byte[32*32];
    private Position previousScreenTopLeft = new Position(-1, -1);
    
    public void setWindowSize(int windowSizeX, int windowSizeY) {
        this.windowSizeX = windowSizeX;
        this.windowSizeY = windowSizeY;
        try {
            super.initialise(emulator, getTileSheet().getTileSheet(tileWidth, tileHeight), getTileSheet(), tileWidth, tileHeight);
        }
        catch (IOException e) {}
    }
    
    public int getWindowSizeX() {
        return windowSizeX;
    }
    public int getWindowSizeY() {
        return windowSizeY;
    }
    
    private boolean onScreen(Position p) {
        return screenTopLeft.getX() <= p.getX() && p.getX() <= screenBottomRight.getX()
            && screenTopLeft.getY() <= p.getY() && p.getY() <= screenBottomRight.getY();
    }
    
    @Override
    void updateGraphics(boolean fromScratch) {
        Position chipPosition = emulator.getLevel().getChip().getPosition();
        int screenX, screenY;
        int chipX = chipPosition.getX();
        int chipY = chipPosition.getY();
    
        screenX = chipX - (windowSizeX) / 2;
        if (screenX < 0) screenX = 0;
        if (screenX + windowSizeX > 32) screenX = 32 - windowSizeX;
        
        screenY = chipY - (windowSizeY) / 2;
        if (screenY < 0) screenY = 0;
        if (screenY + windowSizeY > 32) screenY = 32 - windowSizeY;
    
        screenTopLeft = new Position(screenX, screenY);
        screenBottomRight = screenTopLeft.add(windowSizeX, windowSizeY);
        super.updateGraphics(fromScratch);
    }
    
    @Override
    protected void drawLevel(Level level, boolean fromScratch) {
        byte[] layerBG;
        byte[] layerFG;
    
        try{
            layerFG = level.getLayerFG().getBytes();
            layerBG = level.getLayerBG().getBytes();
        }
        catch (NullPointerException npe){
            return;
        }
    
        WritableRaster rasterFG = fg.getRaster();
        WritableRaster rasterBG = bg.getRaster();
    
        int screenMotion = screenTopLeft.getIndex() - previousScreenTopLeft.getIndex();
    
        for (int xPos = 0; xPos < windowSizeX; xPos++){
            for (int yPos = 0; yPos < windowSizeY; yPos++) {
                Position p = new Position(screenTopLeft.getX() + xPos, screenTopLeft.getY() + yPos);
                int i = p.getIndex();
                if (fromScratch || layerFG[i] != previousFG[i-screenMotion] || layerBG[i] != previousBG[i-screenMotion]) {
                    int x = tileWidth * xPos, y = tileHeight * yPos;
                    rasterBG.setPixels(x, y, tileWidth, tileHeight, tileImage[layerBG[i]]);
                    rasterFG.setPixels(x, y, tileWidth, tileHeight, tileImage[layerFG[i]]);
                    if (showBG && !Tile.fromOrdinal(layerFG[i]).isTransparent() && layerBG[i] != 0) {
                        rasterFG.setPixels(x + BG_BORDER, y + BG_BORDER, tileWidth - 2 * BG_BORDER, tileHeight - 2 * BG_BORDER, bgTileImage[layerBG[i]]);
                    }
                }
            }
        }
        previousBG = layerBG.clone();
        previousFG = layerFG.clone();
        previousScreenTopLeft = screenTopLeft.clone();
    }
    
    @Override
    protected void drawMonsterList(CreatureList monsterList, BufferedImage overlay){
        int i = 0;
        for (Creature c : monsterList){
            if (onScreen(c.getPosition())) {
                int x = c.getPosition().getX() * tileWidth, y = c.getPosition().getY() * tileHeight;
                drawNumber(++i, blackDigits, x, y, overlay.getRaster());
            }
        }
    }
    
    @Override
    protected void drawSlipList(SlipList slipList, BufferedImage overlay){
        int yOffset = tileHeight - SMALL_NUMERAL_HEIGHT - 2;
        for (int i = 0; i < slipList.size(); i++){
            Creature monster = slipList.get(i);
            if (onScreen(monster.getPosition())) {
                int x = monster.getPosition().getX() * tileWidth, y = monster.getPosition().getY() * tileHeight + yOffset;
                drawNumber(i + 1, blueDigits, x, y, overlay.getRaster());
            }
        }
    }
    
    @Override
    protected void drawButtonConnections(ConnectionButton[] connections, BufferedImage overlay){
        Graphics2D g = overlay.createGraphics();
        g.setColor(Color.BLACK);
        for (ConnectionButton connection : connections){
            GameGraphicPosition pos1 = new GameGraphicPosition(connection.getButtonPosition(), tileWidth, tileHeight, new Position(0, 0)),
                pos2 = new GameGraphicPosition(connection.getTargetPosition(), tileWidth, tileHeight, new Position(0, 0));
            g.drawLine(pos1.getGraphicX(0), pos1.getGraphicY(0), pos2.getGraphicX(0), pos2.getGraphicY(0));
        }
    }
    
    @Override
    public void drawPositionList(List<Position> positionList, Graphics2D g) {
        GameGraphicPosition previousPos = new GameGraphicPosition(positionList.get(0), tileWidth, tileHeight, new Position(0, 0));
        boolean[][] tileEnterCount = new boolean[32*32][21];
        int oldOffset = 0, offset = 0;
        for(Position pos : positionList) {
            GameGraphicPosition gp = new GameGraphicPosition(pos, tileWidth, tileHeight, new Position(0, 0));
            int tile = gp.getIndex();
            if (tile == previousPos.getIndex()) continue;
            if (tileEnterCount[tile][oldOffset]){
                for (offset = 0; offset < 21; offset++) if (!tileEnterCount[tile][offset]) break;
            }
            else offset = oldOffset;
            if (offset == 21) offset = 0;
            g.setColor(Color.BLACK);
            g.drawLine(previousPos.getGraphicX(oldOffset), previousPos.getGraphicY(oldOffset), gp.getGraphicX(offset), gp.getGraphicY(offset));
            previousPos = gp;
            oldOffset = offset;
            tileEnterCount[tile][offset] = true;
        }
    }
    
    @Override
    protected void drawChipHistory(Position currentPosition, BufferedImage overlay){
        List<Position> history = emulator.getSavestates().getChipHistory();
        history.add(currentPosition);
        drawPositionList(history, overlay.createGraphics());
    }
    
    @Override
    protected void initialiseTileGraphics(BufferedImage allTiles) {
        tileImage = new int[7*16][tileWidth*tileHeight*CHANNELS];
        for (int i = 0; i < 16 * 7; i++) {
            int x = i / 16;
            int y = i % 16;
            allTiles.getRaster().getPixels(x * tileWidth, y * tileHeight, tileWidth, tileHeight, tileImage[i]);
        }
    }
    
    @Override
    protected void initialiseBGTileGraphics(BufferedImage allTiles) {
        bgTileImage = new int[7*16][tileWidth*tileHeight*CHANNELS];
        for (int i = 0; i < 16 * 7; i++) {
            int x = i / 16;
            int y = i % 16;
            allTiles.getRaster().getPixels(x * tileWidth + BG_BORDER, y * tileHeight + BG_BORDER,
                                           tileWidth - 2 * BG_BORDER, tileHeight - 2 * BG_BORDER, bgTileImage[i]);
        }
    }
    
    @Override
    protected void initialiseLayers() {
        bg = new BufferedImage(windowSizeX * tileWidth, windowSizeY * tileHeight, BufferedImage.TYPE_4BYTE_ABGR);
        fg = new BufferedImage(windowSizeX * tileWidth, windowSizeY * tileHeight, BufferedImage.TYPE_4BYTE_ABGR);
        bbg = new BufferedImage(windowSizeX * tileWidth, windowSizeY * tileHeight, BufferedImage.TYPE_4BYTE_ABGR);
        WritableRaster bbgRaster = bbg.getRaster();
        for (int i = 0; i < windowSizeX * windowSizeY; i++){
            int x = tileWidth * (i % windowSizeX), y = tileHeight * (i / windowSizeX);
            bbgRaster.setPixels(x, y, tileWidth, tileHeight, tileImage[0]);
        }
    }
    
    SmallGamePanel(int windowSizeX, int windowSizeY) {
        this.windowSizeX = windowSizeX;
        this.windowSizeY = windowSizeY;
    }
    
}
