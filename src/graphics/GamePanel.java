package graphics;

import emulator.SuperCC;
import emulator.TickFlags;
import game.*;
import game.button.ConnectionButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.List;

import static game.Position.UNCLICKABLE;
import static graphics.Gui.TILE_SIZE;

public class GamePanel extends JPanel{
    
    public static final int BG_BORDER = 4;

    // The background image
    private BufferedImage bg = new BufferedImage(32*TILE_SIZE, 32*TILE_SIZE, BufferedImage.TYPE_4BYTE_ABGR);
    // The foreground image
    private BufferedImage fg = new BufferedImage(32*TILE_SIZE, 32*TILE_SIZE, BufferedImage.TYPE_4BYTE_ABGR);
    // The image behind the background (32*32 floor tiles)
    private BufferedImage bbg = new BufferedImage(32*TILE_SIZE, 32*TILE_SIZE, BufferedImage.TYPE_4BYTE_ABGR);
    private BufferedImage overlay;

    private byte[] previousFG = new byte[32*32];
    private SuperCC emulator;
    private boolean showBG, showMonsterList, showSlipList, showTrapConnections, showCloneConnections, showHistory;

    // All 7*16 tile types are preloaded and stored here for fast access.
    private static final int CHANNELS = 4;
    public static final int SMALL_NUMERAL_WIDTH = 3, SMALL_NUMERAL_HEIGHT = 5;
    public static final int[][] tileImage = new int[7*16][TILE_SIZE*TILE_SIZE*CHANNELS],
        bgTileImage = new int[7*16][TILE_SIZE*TILE_SIZE*CHANNELS],
        blackDigits = new int[10][(SMALL_NUMERAL_WIDTH+2)*(SMALL_NUMERAL_HEIGHT+2)*CHANNELS],
        blueDigits = new int[10][(SMALL_NUMERAL_WIDTH+2)*(SMALL_NUMERAL_HEIGHT+2)*CHANNELS];
    
    public void setEmulator(SuperCC emulator){
        this.emulator = emulator;
    }
    
    /**
     * Draw the game state. This does not update the graphics - call
     * updateGraphics first.
     * @param g The graphics to draw
     */
    @Override
    public void paintComponent(Graphics g){
        g.drawImage(bbg, 0, 0, null);
        g.drawImage(bg, 0, 0, null);
        g.drawImage(fg, 0, 0, null);
        g.drawImage(overlay, 0, 0, null);
    }
    
    /**
     * Update the graphics using a new Level state. This does not redraw the
     * graphics.
     * @param level The level state to draw
     * @param fromScratch Whether to recreate the entire image (slow) or only
     *                    update where the level changed.
     */
    void updateGraphics(Level level, boolean fromScratch){
    
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
                int x = TILE_SIZE*(i%32), y = TILE_SIZE*(i/32);
                rasterBG.setPixels(x, y, TILE_SIZE, TILE_SIZE, tileImage[layerBG[i]]);
                rasterFG.setPixels(x, y, TILE_SIZE, TILE_SIZE, tileImage[layerFG[i]]);
                if (showBG && !Tile.fromOrdinal(layerFG[i]).isTransparent() && layerBG[i] != 0) {
                    rasterFG.setPixels(x + BG_BORDER, y + BG_BORDER,
                                       TILE_SIZE - 2 * BG_BORDER, TILE_SIZE - 2 * BG_BORDER, bgTileImage[layerBG[i]]);
                }
            }
        }
        previousFG = layerFG.clone();
        
        overlay = new BufferedImage(32 * TILE_SIZE, 32 * TILE_SIZE, BufferedImage.TYPE_4BYTE_ABGR);
        WritableRaster r = overlay.getRaster();
        if (showMonsterList) drawMonsterList(level.getMonsterList(), r);
        if (showSlipList) drawSlipList(level.getSlipList(), r);
        Graphics2D g = overlay.createGraphics();
        if (showCloneConnections) drawButtonConnections(level.getRedButtons(), g);
        if (showTrapConnections) drawButtonConnections(level.getBrownButtons(), g);
        if (showHistory) drawChipHistory(level.getChip().getPosition(), g);
    }
    
    public static void drawNumber(int n, int[][] digitRaster, WritableRaster raster, int x, int y){
        char[] chars = (String.valueOf(n)).toCharArray();
        for (int j = 0; j < chars.length; j++){
            int digit = Character.digit(chars[j], 10);
            raster.setPixels(x+4*j, y, SMALL_NUMERAL_WIDTH+2, SMALL_NUMERAL_HEIGHT+2, digitRaster[digit]);
        }
    }
    
    private void drawMonsterList(CreatureList monsterList, WritableRaster raster){
        int i = 0;
        for (Creature c : monsterList){
            int x = c.getPosition().getX()*TILE_SIZE, y = c.getPosition().getY()*TILE_SIZE;
            drawNumber(++i, blackDigits, raster, x, y);
        }
    }
    
    private void drawSlipList(SlipList slipList, WritableRaster raster){
        int yOffset = TILE_SIZE - SMALL_NUMERAL_HEIGHT - 2;
        for (int i = 0; i < slipList.size(); i++){
            Creature monster = slipList.get(i);
            int x = monster.getPosition().getX()*TILE_SIZE, y = monster.getPosition().getY()*TILE_SIZE + yOffset;
            drawNumber(i+1, blueDigits, raster, x, y);
        }
    }
    
    private void drawButtonConnections(ConnectionButton[] connections, Graphics2D g){
        g.setColor(Color.BLACK);
        for (ConnectionButton connection : connections){
            GameGraphicPosition pos1 = new GameGraphicPosition(connection.getButtonPosition()), pos2 = new GameGraphicPosition(connection.getTargetPosition());
            g.drawLine(pos1.getGraphicX(0), pos1.getGraphicY(0), pos2.getGraphicX(0), pos2.getGraphicY(0));
        }
    }
    
    public void drawPositionList(List<Position> positionList, Graphics2D g) {
        GameGraphicPosition previousPos = new GameGraphicPosition(positionList.get(0));
        boolean[][] tileEnterCount = new boolean[32*32][21];
        int oldOffset = 0, offset = 0;
        for(Position pos : positionList) {
            GameGraphicPosition gp = new GameGraphicPosition(pos);
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
    
    private void drawChipHistory(Position currentPosition, Graphics2D g){
        List<Position> history = emulator.getSavestates().getChipHistory();
        history.add(currentPosition);
        drawPositionList(history, g);
    }
    
    public void setBGVisible(boolean visible) {
        showBG = visible;
    }
    public void setMonsterListVisible(boolean visible){
        showMonsterList = visible;
    }
    public void setSlipListVisible(boolean visible){
        showSlipList = visible;
    }
    public void setTrapsVisible(boolean visible){
        showTrapConnections = visible;
    }
    public void setClonesVisible(boolean visible){
        showCloneConnections = visible;
    }
    public void setHistoryVisible(boolean visible){
        showHistory = visible;
    }
    
    public void initialiseTileGraphics(BufferedImage allTiles) {
        for (int i = 0; i < 16 * 7; i++) {
            int x = i / 16;
            int y = i % 16;
            allTiles.getRaster().getPixels(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE, tileImage[i]);
        }
    }
    
    public void initialiseBGTileGraphics(BufferedImage allTiles) {
        for (int i = 0; i < 16 * 7; i++) {
            int x = i / 16;
            int y = i % 16;
            allTiles.getRaster().getPixels(x * TILE_SIZE + BG_BORDER, y * TILE_SIZE + BG_BORDER,
                                           TILE_SIZE - 2 * BG_BORDER, TILE_SIZE - 2 * BG_BORDER, bgTileImage[i]);
        }
    }
    
    private static BufferedImage drawDigit(int n, Color colorBG, Color colorFG){
        int[] smallNumeralBitmap = new int[] {
            0b111_010_111_111_101_111_111_111_111_111_00,
            0b101_010_001_001_101_100_100_001_101_101_00,
            0b101_010_111_111_111_111_111_001_111_111_00,
            0b101_010_100_001_001_001_101_001_101_001_00,
            0b111_010_111_111_001_111_111_001_111_111_00
        };
        BufferedImage digit = new BufferedImage(SMALL_NUMERAL_WIDTH+2, SMALL_NUMERAL_HEIGHT+2, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D graphics = digit.createGraphics();
        graphics.setPaint(colorBG);
        graphics.fillRect(0, 0, digit.getWidth(), digit.getHeight());
        int rgb = colorFG.getRGB();
        for (int y = 0; y < SMALL_NUMERAL_HEIGHT; y++){
            for (int x = 0; x < SMALL_NUMERAL_WIDTH; x++) {
                if (((smallNumeralBitmap[y] << (x + 3*n)) & 0x80000000) == 0x80000000){
                    digit.setRGB(x+1, y+1, rgb);
                }
            }
        }
        return digit;
    }
    
    private void initialiseDigits(){
        for (int n = 0; n <= 9; n++){
            BufferedImage digitBlue = drawDigit(n, Color.BLUE, Color.WHITE);
            digitBlue.getRaster().getPixels(0, 0, digitBlue.getWidth(), digitBlue.getHeight(), blueDigits[n]);
            BufferedImage digitBlack = drawDigit(n, Color.BLACK, Color.WHITE);
            digitBlack.getRaster().getPixels(0, 0, digitBlue.getWidth(), digitBlue.getHeight(), blackDigits[n]);
        }
    }
    
    private void initialiseBBG(){
        WritableRaster bbgRaster = bbg.getRaster();
        for (int i = 0; i < 32*32; i++){
            int x = TILE_SIZE * (i % 32), y = TILE_SIZE * (i / 32);
            bbgRaster.setPixels(x, y, TILE_SIZE, TILE_SIZE, tileImage[0]);
        }
    }
    
    public void initialise(Image tilespng) throws IOException{
        initialiseTileGraphics((BufferedImage) tilespng);
        initialiseBGTileGraphics((BufferedImage) tilespng);
        initialiseDigits();
        initialiseBBG();
    }

    GamePanel() {
        setPreferredSize(new Dimension(32*TILE_SIZE, 32*TILE_SIZE));
        addMouseListener(new GameMouseListener());
        addMouseMotionListener(new GameMouseSensor());
    }
    
    private class GameMouseSensor implements MouseMotionListener {
    
        @Override
        public void mouseDragged(MouseEvent e) {}
    
        @Override
        public void mouseMoved(MouseEvent e) {
            GameGraphicPosition pos = new GameGraphicPosition(e);
            Tile bgTile = emulator.getLevel().getLayerBG().get(pos),
                fgTile = emulator.getLevel().getLayerFG().get(pos);
            String str = pos.toString() + " " + fgTile;
            if (bgTile != Tile.FLOOR) str += " / " + bgTile;
            emulator.showAction(str);
        }
        
    }
    
    public class GameMouseListener implements MouseListener{
        @Override
        public void mouseClicked(MouseEvent e) {
            GameGraphicPosition clickPosition = new GameGraphicPosition(e);
            Creature chip = emulator.getLevel().getChip();
            byte b = clickPosition.clickByte(chip.getPosition());
            if (b == UNCLICKABLE) return;
            emulator.showAction("Clicked " + clickPosition);
            emulator.getLevel().setClick(clickPosition.getIndex());
            Direction[] directions = chip.seek(clickPosition);
            emulator.tick(b, directions, TickFlags.GAME_PLAY);
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
