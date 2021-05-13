package graphics;

import game.*;
import game.MS.*;
import game.button.ConnectionButton;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.List;

public class SmallGamePanel extends GamePanel {
    
    private Position screenBottomRight;                 // not included
    private int windowSizeX, windowSizeY;
    private byte[] previousLayerFG = new byte[32*32];
    private byte[] previousLayerBG = new byte[32*32];
    private Position previousScreenTopLeft = new Position(-1, -1);
    
    private static final double[] offsets = new double[] {
        (double) 7/15 + (double) 1/30,
        (double) 3/15 + (double) 1/30,
        (double) 11/15 + (double) 1/30,
        (double) 5/15 + (double) 1/30,
        (double) 9/15 + (double) 1/30,
        (double) 1/15 + (double) 1/30,
        (double) 13/15 + (double) 1/30,
        (double) 4/15 + (double) 1/30,
        (double) 10/15 + (double) 1/30,
        (double) 2/15 + (double) 1/30,
        (double) 12/15 + (double) 1/30,
        (double) 6/15 + (double) 1/30,
        (double) 8/15 + (double) 1/30,
        (double) 0/15 + (double) 1/30,
        (double) 14/15 + (double) 1/30
    };
    
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
        return screenTopLeft.getX() <= p.getX() && p.getX() < screenBottomRight.getX()
            && screenTopLeft.getY() <= p.getY() && p.getY() < screenBottomRight.getY();
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
        if (level.supportsLayerBG()) drawLevelWithBGSupport(level, fromScratch);
        else drawLevelWithoutBGSupport(level, fromScratch);
    }

    protected void drawLevelWithBGSupport(Level level, boolean fromScratch) {
        byte[] layerBG;
        byte[] layerFG;

        WritableRaster rasterFG;
        WritableRaster rasterBG;

        int screenMotion = screenTopLeft.getIndex() - previousScreenTopLeft.getIndex();

        rasterFG = upperImage.getRaster();
        rasterBG = lowerImage.getRaster();

        try{
            layerFG = level.getLayerFG().getBytes();
            layerBG = level.getLayerBG().getBytes();
        }
        catch (NullPointerException npe){
            return;
        }

        for (int xPos = 0; xPos < windowSizeX; xPos++) {
            for (int yPos = 0; yPos < windowSizeY; yPos++) {
                Position p = new Position(screenTopLeft.getX() + xPos, screenTopLeft.getY() + yPos);
                int i = p.getIndex();
                if (fromScratch || layerFG[i] != previousLayerFG[i - screenMotion] || layerBG[i] != previousLayerBG[i - screenMotion]) {
                    int x = tileWidth * xPos, y = tileHeight * yPos;
                    rasterBG.setPixels(x, y, tileWidth, tileHeight, tileImage[layerBG[i]]);
                    rasterFG.setPixels(x, y, tileWidth, tileHeight, tileImage[layerFG[i]]);
                    if (showBG && !Tile.fromOrdinal(layerFG[i]).isTransparent() && layerBG[i] != 0) {
                        rasterFG.setPixels(x + bgBorderSize, y + bgBorderSize, tileWidth - 2 * bgBorderSize, tileHeight - 2 * bgBorderSize, bgTileImage[layerBG[i]]);
                    }
                }
            }
        }
        previousLayerFG = layerFG.clone();
        previousLayerBG = layerBG.clone();
        previousScreenTopLeft = screenTopLeft.clone();
    }

    protected void drawLevelWithoutBGSupport(Level level, boolean fromScratch) {
        byte[] layerFG;

        Graphics graphicsCreatures;
        WritableRaster rasterTerrain;

        int screenMotion = screenTopLeft.getIndex() - previousScreenTopLeft.getIndex();

        upperImage = new BufferedImage(32 * tileWidth, 32 * tileHeight, BufferedImage.TYPE_4BYTE_ABGR);
        graphicsCreatures = upperImage.getGraphics();
        rasterTerrain = lowerImage.getRaster();
        /* We create a blank/transparent image since we have to redraw the creature list every time we want a
        transparent image/raster to draw overtop of, so we just create a blank image the correct size and go from there */
        try{
            layerFG = level.getLayerFG().getBytes();
        }
        catch (NullPointerException npe){
            return;
        }

        for (int xPos = 0; xPos < windowSizeX; xPos++) {
            for (int yPos = 0; yPos < windowSizeY; yPos++) {
                Position p = new Position(screenTopLeft.getX() + xPos, screenTopLeft.getY() + yPos);
                int i = p.getIndex();
                if (fromScratch || layerFG[i] != previousLayerFG[i - screenMotion]) {
                    int x = tileWidth * xPos, y = tileHeight * yPos;

                    rasterTerrain.setPixels(x, y, tileWidth, tileHeight, tileImage[layerFG[i]]);
                    /* If there's no bottom layer than that means we have to draw the creature list on top of
                    everything else, however it would be extremely wasteful to create an entire other raster for
                    that and leave the BG raster and BBG raster unused and only use the FG raster and the new creature
                    raster so instead I draw the FG layer onto the BG raster and draw the creature list on the FG
                    raster to avoid creating new rasters and having unused ones */
                }
            }
        }
        for (Creature creature : emulator.getLevel().getMonsterList()) { //If we don't support BG (meaning: lynx) it means we have to draw the creature list separately
            if (creature.isDead() && creature.getAnimationTimer() == 0)
                continue;

            int x = tileWidth * creature.getPosition().x;
            int y = tileHeight * creature.getPosition().y;
            final int vPixelsBetweenTiles = tileHeight / 8;//Lynx has values between 0 and 7 for this, and i don't want to extend level for something so trivial, so I just hardcode it here for now
            final int hPixelsBetweenTiles = tileWidth / 8;
            switch (creature.getDirection()) {
                case UP:
                    y += vPixelsBetweenTiles * creature.getTimeTraveled();
                    break;
                case LEFT:
                    x += hPixelsBetweenTiles * creature.getTimeTraveled();
                    break;
                case DOWN:
                    y -= vPixelsBetweenTiles * creature.getTimeTraveled();
                    break;
                case RIGHT:
                    x -= hPixelsBetweenTiles * creature.getTimeTraveled();
                    break;
            }
            Image creatureImage;
            switch (creature.getCreatureType()) { //creatureImages is laid out the same way as the creatures in the tilesheet
                default:
                    creatureImage = creatureImages[creature.getCreatureType().ordinal() + 1][creature.getDirection().ordinal()];
                    break;
                case BLOCK:
                    creatureImage = creatureImages[11][creature.getDirection().ordinal()];
                    break;
                case CHIP:
                    creatureImage = creatureImages[10][creature.getDirection().ordinal()];
                    break;
                case CHIP_SWIMMING:
                    creatureImage = creatureImages[0][creature.getDirection().ordinal()];
                    break;
                case DEAD:
                    creatureImage = creatureImages[12][creature.getDirection().ordinal()];
            }
            graphicsCreatures.drawImage(creatureImage, x, y, tileWidth, tileHeight, null);
        }

        previousLayerFG = layerFG.clone();
        previousScreenTopLeft = screenTopLeft.clone();
    }
    
    @Override
    protected void drawMonsterListNumbers(Level level, CreatureList monsterList, BufferedImage overlay){
        int i = 0;
        for (Creature c : monsterList){
            ++i;
            if (onScreen(c.getPosition()) && level.shouldDrawCreatureNumber(c)) {
                int x = (c.getPosition().getX() - screenTopLeft.getX()) * tileWidth;
                int y = (c.getPosition().getY() - screenTopLeft.getY()) * tileHeight;
                drawNumber(i, blackDigits, x, y, overlay.getRaster());
            }
        }
    }
    
    @Override
    protected void drawSlipListNumbers(SlipList slipList, BufferedImage overlay){
        int yOffset = tileHeight - SMALL_NUMERAL_HEIGHT - 2;
        for (int i = 0; i < slipList.size(); i++){
            Creature monster = slipList.get(i);
            if (onScreen(monster.getPosition())) {
                int x = (monster.getPosition().getX() - screenTopLeft.getX()) * tileWidth;
                int y = (monster.getPosition().getY() - screenTopLeft.getY()) * tileHeight + yOffset;
//                System.out.println(x); //I have no idea why these 2 are here
//                System.out.println(y);
                drawNumber(i + 1, blueDigits, x, y, overlay.getRaster());
            }
        }
    }
    
    @Override
    protected void drawButtonConnections(ConnectionButton[] connections, BufferedImage overlay){
        Graphics2D g = overlay.createGraphics();
        g.setColor(Color.BLACK);
        for (ConnectionButton connection : connections){
            int x1 = (connection.getButtonPosition().getX() - screenTopLeft.getX()) * tileWidth + tileWidth/2;
            int y1 = (connection.getButtonPosition().getY() - screenTopLeft.getY()) * tileHeight + tileHeight/2;
            int x2 = (connection.getTargetPosition().getX() - screenTopLeft.getX()) * tileWidth + tileWidth/2;
            int y2 = (connection.getTargetPosition().getY() - screenTopLeft.getY()) * tileHeight + tileHeight/2;
            g.drawLine(x1, y1, x2, y2);
        }
    }
    
    @Override
    public void drawPositionList(List<Position> positionList, Graphics2D g) {
        Position previousPos = positionList.get(0);
        boolean[][] tileEnterCount = new boolean[32*32][offsets.length];
        tileEnterCount[previousPos.getIndex()][0] = true;
        int oldOffset = 0, offset;
        g.setColor(Color.BLACK);
        for(Position pos : positionList) {
            int tile = pos.getIndex();
            if (tile == previousPos.getIndex()) continue;
            if (tileEnterCount[tile][oldOffset]){
                for (offset = 0; offset < offsets.length; offset++) if (!tileEnterCount[tile][offset]) break;
            }
            else offset = oldOffset;
            if (offset == offsets.length) offset = 0;
            int x1 = (previousPos.getX() - screenTopLeft.getX()) * tileWidth + ((int) (tileWidth * offsets[oldOffset]));
            int y1 = (previousPos.getY() - screenTopLeft.getY()) * tileHeight + ((int) (tileHeight * offsets[oldOffset]));
            int x2 = (pos.getX() - screenTopLeft.getX()) * tileWidth + ((int) (tileWidth * offsets[offset]));
            int y2 = (pos.getY() - screenTopLeft.getY()) * tileHeight + ((int) (tileHeight * offsets[offset]));
            g.drawLine(x1, y1, x2, y2);
            previousPos = pos;
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
        bgBorderSize = tileWidth / 5;
        for (int i = 0; i < 16 * 7; i++) {
            int x = i / 16;
            int y = i % 16;
            allTiles.getRaster().getPixels(x * tileWidth + bgBorderSize, y * tileHeight + bgBorderSize,
                                           tileWidth - 2 * bgBorderSize, tileHeight - 2 * bgBorderSize, bgTileImage[i]);
        }
    }

    @Override
    protected void initialiseCreatureGraphics(BufferedImage allTiles) {
        creatureImages = new Image[13][4]; //11 creatures (plus blocks and death effect), each has 4 direction images
        for (int i = 0; i < 10; i++) {
            int offset = 60 + i*4; //60 is Swimming Chip N's tile
            int x = offset / 16;
            for (int j = 0; j < 4; j++) {
                int y = (offset + j) % 16;
                creatureImages[i][j] = allTiles.getSubimage(x * tileWidth, y * tileHeight, tileWidth, tileHeight);
            }
        }
        for (int k=0; k<4; k++) { //Chip's graphics are separate from the rest of the creatures so we handle them here
            int offset = 108; //Chip's tile
            int x = offset / 16;
            int y = (offset + k) % 16;
            creatureImages[10][k] = allTiles.getSubimage(x * tileWidth, y * tileHeight, tileWidth, tileHeight);

            if (k < 2) offset = 14; //Clone blocks start at 14
            else offset = 16; //And they're split across 2 columns for some reason
            x = offset / 16;
            y = (offset + (k % 2)) % 16;
            creatureImages[11][k] = allTiles.getSubimage(x * tileWidth, y * tileHeight, tileWidth, tileHeight);

            int vOff = 0;
            if (k > 0) { //splash image is at 51 and explode is at 54 followed by chip death explosion
                offset = 54;
                vOff = k-1;
            }
            else
                offset = 51;
            x = offset / 16;
            y = (offset + vOff) % 16;
            creatureImages[12][k] = allTiles.getSubimage(x * tileWidth, y * tileHeight, tileWidth, tileHeight);
        }
    }
    
    @Override
    protected void initialiseLayers() {
        lowerImage = new BufferedImage(windowSizeX * tileWidth, windowSizeY * tileHeight, BufferedImage.TYPE_4BYTE_ABGR);
        upperImage = new BufferedImage(windowSizeX * tileWidth, windowSizeY * tileHeight, BufferedImage.TYPE_4BYTE_ABGR);
        backingImage = new BufferedImage(windowSizeX * tileWidth, windowSizeY * tileHeight, BufferedImage.TYPE_4BYTE_ABGR);
        WritableRaster bbgRaster = backingImage.getRaster();
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
