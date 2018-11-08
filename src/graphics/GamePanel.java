package graphics;

import emulator.SuperCC;
import emulator.TickFlags;
import game.*;

import javax.imageio.ImageIO;
import javax.sound.midi.Soundbank;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import static graphics.Gui.TILE_SIZE;
import static game.Position.UNCLICKABLE;

class GamePanel extends JPanel{

    // The background image
    private BufferedImage bg = new BufferedImage(32*TILE_SIZE, 32*TILE_SIZE, BufferedImage.TYPE_4BYTE_ABGR);
    // The foreground image
    private BufferedImage fg = new BufferedImage(32*TILE_SIZE, 32*TILE_SIZE, BufferedImage.TYPE_4BYTE_ABGR);
    // The image behind the background (32*32 floor tiles)
    private BufferedImage bbg = new BufferedImage(32*TILE_SIZE, 32*TILE_SIZE, BufferedImage.TYPE_4BYTE_ABGR);
    private BufferedImage overlay;

    private byte[] previousFG = new byte[32*32];
    private SuperCC emulator;
    private boolean showMonsterList, showSlipList, showTrapConnections, showCloneConnections, showHistory;

    // All 7*16 tile types are preloaded and stored here for fast access.
    private static final int CHANNELS = 4;
    public static final int SMALL_NUMERAL_WIDTH = 3, SMALL_NUMERAL_HEIGHT = 5;
    public static final int[][] tileImage = new int[7*16][TILE_SIZE*TILE_SIZE*CHANNELS],
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
            layerFG = level.getLayerFG().getLayer();
            layerBG = level.getLayerBG().getLayer();
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
            }
        }
        previousFG = layerFG.clone();
        
        overlay = new BufferedImage(32 * TILE_SIZE, 32 * TILE_SIZE, BufferedImage.TYPE_4BYTE_ABGR);
        WritableRaster r = overlay.getRaster();
        if (showMonsterList) drawMonsterList(level.getMonsterList().list, r);
        if (showSlipList) drawSlipList(level.getSlipList(), r);
        Graphics2D g = overlay.createGraphics();
        if (showCloneConnections) drawButtonConnections(level.getCloneConnections(), g);
        if (showTrapConnections) drawButtonConnections(level.getTrapConnections(), g);
        if (showHistory) drawChipHistory(level.getChip().getPosition(), g);
    }
    
    public static void drawNumber(int n, int[][] digitRaster, WritableRaster raster, int x, int y){
        char[] chars = (String.valueOf(n)).toCharArray();
        for (int j = 0; j < chars.length; j++){
            int digit = Character.digit(chars[j], 10);
            raster.setPixels(x+4*j, y, SMALL_NUMERAL_WIDTH+2, SMALL_NUMERAL_HEIGHT+2, digitRaster[digit]);
        }
    }
    
    private void drawMonsterList(Creature[] monsterList, WritableRaster raster){
        for (int i = 0; i < monsterList.length; i++){
            Creature monster = monsterList[i];
            int x = monster.getX()*TILE_SIZE, y = monster.getY()*TILE_SIZE;
            drawNumber(i+1, blackDigits, raster, x, y);
        }
    }
    
    private void drawSlipList(SlipList slipList, WritableRaster raster){
        int yOffset = TILE_SIZE - SMALL_NUMERAL_HEIGHT - 2;
        for (int i = 0; i < slipList.size(); i++){
            Creature monster = slipList.get(i);
            int x = monster.getX()*TILE_SIZE, y = monster.getY()*TILE_SIZE + yOffset;
            drawNumber(i+1, blueDigits, raster, x, y);
        }
    }
    
    private void drawButtonConnections(int[][] connections, Graphics2D g){
        g.setColor(Color.BLACK);
        for (int[] connection : connections){
            Position pos1 = new Position(connection[0]), pos2 = new Position(connection[1]);
            g.drawLine(pos1.getGraphicX(), pos1.getGraphicY(), pos2.getGraphicX(), pos2.getGraphicY());
        }
    }
    
    private void drawChipHistory(Position currentPosition, Graphics2D g){
        LinkedList<Position> history = emulator.getSavestates().getChipHistory();
        history.addFirst(currentPosition);
        float length = history.size();
        int i = 0;
        Position previousPos = history.getLast();
        boolean[][] tileEnterCount = new boolean[32*32][21];
        int oldOffset = 0, offset = 0;
        Iterator iter = history.descendingIterator();
        while(iter.hasNext()){
            Position pos = (Position) iter.next();
            int tile = pos.getIndex();
            if (tile == previousPos.getIndex()) continue;
            if (tileEnterCount[tile][oldOffset]){
                for (offset = 0; offset < 21; offset++) if (!tileEnterCount[tile][offset]) break;
            }
            else offset = oldOffset;
            if (offset == 21) offset = 0;
            float hue = (float) (0.5 + i++ / length / 1);
            g.setColor(Color.getHSBColor(hue, (float) 0.9, (float) 0.8));
            //g.setColor(Color.WHITE);
            g.drawLine(previousPos.getGraphicX(oldOffset), previousPos.getGraphicY(oldOffset), pos.getGraphicX(offset), pos.getGraphicY(offset));
            previousPos = pos;
            oldOffset = offset;
            tileEnterCount[tile][offset] = true;
        }
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
    
    public void initialiseTileGraphics(BufferedImage allTiles) throws IOException{
        for (int i = 0; i < 16 * 7; i++) {
            int x = i / 16;
            int y = i % 16;
            allTiles.getRaster().getPixels(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE, tileImage[i]);
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
        initialiseDigits();
        initialiseBBG();
    }

    GamePanel() {
        setPreferredSize(new Dimension(32*TILE_SIZE, 32*TILE_SIZE));
        addMouseListener(new GameMouseListener());
    }
    
    private class GameMouseListener implements MouseListener{
        @Override
        public void mouseClicked(MouseEvent e) {
            Position clickPosition = new Position(e);
            Creature chip = emulator.getLevel().getChip();
            byte b = clickPosition.clickByte(chip.getPosition());
            if (b == UNCLICKABLE) return;
            emulator.showAction("Clicked " + clickPosition);
            emulator.getLevel().setClick(clickPosition.getIndex());
            int[] directions = chip.seek(clickPosition);
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
