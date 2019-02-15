package graphics;

import game.*;
import game.button.ConnectionButton;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.List;

public class FullscreenGamePanel extends GamePanel {

    private byte[] previousFG = new byte[32*32];
    
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
    
        for (int i = 0; i < 32*32; i++){
            if (fromScratch | layerFG[i] != previousFG[i]){
                int x = getTileSize()*(i%32), y = getTileSize()*(i/32);
                rasterBG.setPixels(x, y, getTileSize(), getTileSize(), tileImage[layerBG[i]]);
                rasterFG.setPixels(x, y, getTileSize(), getTileSize(), tileImage[layerFG[i]]);
                if (showBG && !Tile.fromOrdinal(layerFG[i]).isTransparent() && layerBG[i] != 0) {
                    rasterFG.setPixels(x + BG_BORDER, y + BG_BORDER,
                                       getTileSize() - 2 * BG_BORDER, getTileSize() - 2 * BG_BORDER, bgTileImage[layerBG[i]]);
                }
            }
        }
        previousFG = layerFG.clone();
    }
    
    @Override
    protected void drawMonsterList(CreatureList monsterList, BufferedImage overlay){
        int i = 0;
        for (Creature c : monsterList){
            int x = c.getPosition().getX()*getTileSize(), y = c.getPosition().getY()*getTileSize();
            drawNumber(++i, blackDigits, x, y, overlay.getRaster());
        }
    }
    
    @Override
    protected void drawSlipList(SlipList slipList, BufferedImage overlay){
        int yOffset = getTileSize() - SMALL_NUMERAL_HEIGHT - 2;
        for (int i = 0; i < slipList.size(); i++){
            Creature monster = slipList.get(i);
            int x = monster.getPosition().getX()*getTileSize(), y = monster.getPosition().getY()*getTileSize() + yOffset;
            drawNumber(i+1, blueDigits, x, y, overlay.getRaster());
        }
    }
    
    @Override
    protected void drawButtonConnections(ConnectionButton[] connections, BufferedImage overlay){
        Graphics2D g = overlay.createGraphics();
        g.setColor(Color.BLACK);
        for (ConnectionButton connection : connections){
            GameGraphicPosition pos1 = new GameGraphicPosition(connection.getButtonPosition(), getTileSize()), pos2 = new GameGraphicPosition(connection.getTargetPosition(), getTileSize());
            g.drawLine(pos1.getGraphicX(0), pos1.getGraphicY(0), pos2.getGraphicX(0), pos2.getGraphicY(0));
        }
    }
    
    @Override
    public void drawPositionList(List<Position> positionList, Graphics2D g) {
        GameGraphicPosition previousPos = new GameGraphicPosition(positionList.get(0), getTileSize());
        boolean[][] tileEnterCount = new boolean[32*32][21];
        int oldOffset = 0, offset = 0;
        for(Position pos : positionList) {
            GameGraphicPosition gp = new GameGraphicPosition(pos, getTileSize());
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
        tileImage = new int[7*16][getTileSize()*getTileSize()*CHANNELS];
        for (int i = 0; i < 16 * 7; i++) {
            int x = i / 16;
            int y = i % 16;
            allTiles.getRaster().getPixels(x * getTileSize(), y * getTileSize(), getTileSize(), getTileSize(), tileImage[i]);
        }
    }
    
    @Override
    protected void initialiseBGTileGraphics(BufferedImage allTiles) {
        bgTileImage = new int[7*16][getTileSize()*getTileSize()*CHANNELS];
        for (int i = 0; i < 16 * 7; i++) {
            int x = i / 16;
            int y = i % 16;
            allTiles.getRaster().getPixels(x * getTileSize() + BG_BORDER, y * getTileSize() + BG_BORDER,
                                           getTileSize() - 2 * BG_BORDER, getTileSize() - 2 * BG_BORDER, bgTileImage[i]);
        }
    }
    
    @Override
    protected void initialiseLayers() {
        bg = new BufferedImage(32*getTileSize(), 32*getTileSize(), BufferedImage.TYPE_4BYTE_ABGR);
        fg = new BufferedImage(32*getTileSize(), 32*getTileSize(), BufferedImage.TYPE_4BYTE_ABGR);
        bbg = new BufferedImage(32*getTileSize(), 32*getTileSize(), BufferedImage.TYPE_4BYTE_ABGR);
        WritableRaster bbgRaster = bbg.getRaster();
        for (int i = 0; i < 32*32; i++){
            int x = getTileSize() * (i % 32), y = getTileSize() * (i / 32);
            bbgRaster.setPixels(x, y, getTileSize(), getTileSize(), tileImage[0]);
        }
    }

    FullscreenGamePanel() {
        setPreferredSize(new Dimension(32*getTileSize(), 32*getTileSize()));
    }

}
