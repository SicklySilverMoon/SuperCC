package graphics;

import emulator.SuperCC;
import game.CreatureList;
import game.Level;
import game.Position;
import game.SlipList;
import game.button.ConnectionButton;

import java.awt.*;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.List;

import static graphics.Gui.TILE_SIZE;

public abstract class AbstractGamePanel implements MouseMotionListener {
    
    public static final int BG_BORDER = 4;
    private static final int CHANNELS = 4;
    private static final int SMALL_NUMERAL_WIDTH = 3, SMALL_NUMERAL_HEIGHT = 5;
    
    protected static final int[][] tileImage = new int[7*16][TILE_SIZE*TILE_SIZE*CHANNELS],
        bgTileImage = new int[7*16][TILE_SIZE*TILE_SIZE*CHANNELS],
        blackDigits = new int[10][(SMALL_NUMERAL_WIDTH+2)*(SMALL_NUMERAL_HEIGHT+2)*CHANNELS],
        blueDigits = new int[10][(SMALL_NUMERAL_WIDTH+2)*(SMALL_NUMERAL_HEIGHT+2)*CHANNELS];
    
    private SuperCC emulator;
    
    protected boolean showBG, showMonsterList, showSlipList, showTrapConnections, showCloneConnections, showHistory;
    
    // The background image
    private BufferedImage bg;
    // The foreground image
    private BufferedImage fg;
    // The image behind the background (32*32 floor tiles)
    private BufferedImage bbg;
    private BufferedImage overlay;
    
    public static void drawNumber(int n, int[][] digitRaster, WritableRaster raster, int x, int y){
        char[] chars = (String.valueOf(n)).toCharArray();
        for (int j = 0; j < chars.length; j++){
            int digit = Character.digit(chars[j], 10);
            raster.setPixels(x+4*j, y, SMALL_NUMERAL_WIDTH+2, SMALL_NUMERAL_HEIGHT+2, digitRaster[digit]);
        }
    }
    protected abstract void drawLevel(Level level);
    protected abstract void drawMonsterList(CreatureList monsterList, BufferedImage overlay);
    protected abstract void drawSlipList(SlipList monsterList, BufferedImage overlay);
    protected abstract void drawButtonConnections(ConnectionButton[] connections, BufferedImage overlay);
    protected abstract void drawPositionList(List<Position> positionList, Graphics2D g);
    protected abstract void drawChipHistory(Position currentPosition, BufferedImage overlay);
    
    void updateGraphics(boolean fromScratch) {
        overlay = new BufferedImage(32 * TILE_SIZE, 32 * TILE_SIZE, BufferedImage.TYPE_4BYTE_ABGR);
        if (showMonsterList) drawMonsterList(level.getMonsterList(), overlay);
        if (showSlipList) drawSlipList(level.getSlipList(), overlay);
        if (showCloneConnections) drawButtonConnections(level.getRedButtons(), overlay);
        if (showTrapConnections) drawButtonConnections(level.getBrownButtons(), overlay);
        if (showHistory) drawChipHistory(level.getChip().getPosition(), overlay);
    
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
    
    protected void initialiseDigits(){
        for (int n = 0; n <= 9; n++){
            BufferedImage digitBlue = drawDigit(n, Color.BLUE, Color.WHITE);
            digitBlue.getRaster().getPixels(0, 0, digitBlue.getWidth(), digitBlue.getHeight(), blueDigits[n]);
            BufferedImage digitBlack = drawDigit(n, Color.BLACK, Color.WHITE);
            digitBlack.getRaster().getPixels(0, 0, digitBlue.getWidth(), digitBlue.getHeight(), blackDigits[n]);
        }
    }
    
}
