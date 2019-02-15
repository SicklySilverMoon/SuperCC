package graphics;

import game.*;
import game.button.ConnectionButton;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.List;

public class SmallGamePanel extends GamePanel {
    
    private Position screenTopLeft;                     // included
    private Position screenBottomRight;                 // not included
    private final int WINDOW_SIZE_X, WINDOW_SIZE_Y;
    
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
    
        screenX = chipX - (WINDOW_SIZE_X)/2;
        if (screenX < 0) screenX = 0;
        if (screenX + WINDOW_SIZE_X > 32) screenX = 32-WINDOW_SIZE_X;
        
        screenY = chipY - (WINDOW_SIZE_Y)/2;
        if (screenY < 0) screenY = 0;
        if (screenY + WINDOW_SIZE_Y > 32) screenY = 32-WINDOW_SIZE_Y;
    
        screenTopLeft = new Position(screenX, screenY);
        screenBottomRight = screenTopLeft.add(WINDOW_SIZE_X, WINDOW_SIZE_Y);
        System.out.println(screenTopLeft);
        System.out.println(screenBottomRight);
        System.out.println("");
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
    
        for (int xPos = 0; xPos < WINDOW_SIZE_X ; xPos++){
            for (int yPos = 0; yPos < WINDOW_SIZE_Y ; yPos++) {
                Position p = new Position(screenTopLeft.getX() + xPos, screenTopLeft.getY() + yPos);
                int x = tileWidth * xPos, y = tileHeight * yPos;
                rasterBG.setPixels(x, y, tileWidth, tileHeight, tileImage[layerBG[p.getIndex()]]);
                rasterFG.setPixels(x, y, tileWidth, tileHeight, tileImage[layerFG[p.getIndex()]]);
                if (showBG && !Tile.fromOrdinal(layerFG[p.getIndex()]).isTransparent() && layerBG[p.getIndex()] != 0) {
                    rasterFG.setPixels(x + BG_BORDER, y + BG_BORDER, tileWidth - 2 * BG_BORDER, tileHeight - 2 * BG_BORDER, bgTileImage[layerBG[p.getIndex()]]);
                }
            }
        }
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
            GameGraphicPosition pos1 = new GameGraphicPosition(connection.getButtonPosition(), tileWidth, tileHeight),
                pos2 = new GameGraphicPosition(connection.getTargetPosition(), tileWidth, tileHeight);
            g.drawLine(pos1.getGraphicX(0), pos1.getGraphicY(0), pos2.getGraphicX(0), pos2.getGraphicY(0));
        }
    }
    
    @Override
    public void drawPositionList(List<Position> positionList, Graphics2D g) {
        GameGraphicPosition previousPos = new GameGraphicPosition(positionList.get(0), tileWidth, tileHeight);
        boolean[][] tileEnterCount = new boolean[32*32][21];
        int oldOffset = 0, offset = 0;
        for(Position pos : positionList) {
            GameGraphicPosition gp = new GameGraphicPosition(pos, tileWidth, tileHeight);
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
        bg = new BufferedImage(WINDOW_SIZE_X*tileWidth, WINDOW_SIZE_Y*tileHeight, BufferedImage.TYPE_4BYTE_ABGR);
        fg = new BufferedImage(WINDOW_SIZE_X*tileWidth, WINDOW_SIZE_Y*tileHeight, BufferedImage.TYPE_4BYTE_ABGR);
        bbg = new BufferedImage(WINDOW_SIZE_X*tileWidth, WINDOW_SIZE_Y*tileHeight, BufferedImage.TYPE_4BYTE_ABGR);
        WritableRaster bbgRaster = bbg.getRaster();
        for (int i = 0; i < WINDOW_SIZE_X*WINDOW_SIZE_Y; i++){
            int x = tileWidth * (i % WINDOW_SIZE_X), y = tileHeight * (i / WINDOW_SIZE_X);
            bbgRaster.setPixels(x, y, tileWidth, tileHeight, tileImage[0]);
        }
    }
    
    SmallGamePanel(int windowSizeX, int windowSizeY) {
        this.WINDOW_SIZE_X = windowSizeX;
        this.WINDOW_SIZE_Y = windowSizeY;
    }
    
}
