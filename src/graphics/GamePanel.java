package graphics;

import emulator.SuperCC;
import emulator.TickFlags;
import game.*;
import game.button.ConnectionButton;
import tools.SeedSearch;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.List;

import static game.Position.UNCLICKABLE;

public abstract class GamePanel extends JPanel
    implements MouseMotionListener, MouseListener {
    
    static final int CHANNELS = 4, SMALL_NUMERAL_WIDTH = 3, SMALL_NUMERAL_HEIGHT = 5;
    protected int bgBorderSize;
    protected int tileHeight, tileWidth;
    private TileSheet tileSheet;

    protected Position screenTopLeft = new Position(0, 0);
    
    protected static final int[][]
        blackDigits = new int[10][(SMALL_NUMERAL_WIDTH+2)*(SMALL_NUMERAL_HEIGHT+2)*CHANNELS],
        blueDigits = new int[10][(SMALL_NUMERAL_WIDTH+2)*(SMALL_NUMERAL_HEIGHT+2)*CHANNELS];
    static int[][] tileImage, bgTileImage;
    
    protected boolean showBG = true, showMonsterList = true, showSlipList = true; //Makes sure that the BG layer, the slip list, and the monster list show by default
    protected boolean showTrapConnections, showCloneConnections, showHistory;

    // The background image
    protected BufferedImage bg;
    // The foreground image
    protected BufferedImage fg;
    // The image behind the background (32*32 floor tiles)
    protected BufferedImage bbg;
    protected BufferedImage overlay;
    
    protected SuperCC emulator;
    
    public void setEmulator(SuperCC emulator){
        this.emulator = emulator;
    }
    
    public void setTileSheet(TileSheet ts) {
        this.tileSheet = ts;
    }
    
    public TileSheet getTileSheet() {
        return tileSheet;
    }
    
    public int getTileWidth() {
        return tileWidth;
    }
    
    public int getTileHeight() {
        return tileHeight;
    }
    
    @Override
    public void paintComponent(Graphics g){
        g.drawImage(bbg, 0, 0, null);
        g.drawImage(bg, 0, 0, null);
        g.drawImage(fg, 0, 0, null);
        g.drawImage(overlay, 0, 0, null);
    }
    
    public static void drawNumber(int n, int[][] digitRaster, int x, int y, WritableRaster raster){
        char[] chars = (String.valueOf(n)).toCharArray();
        for (int j = 0; j < chars.length; j++){
            int digit = Character.digit(chars[j], 10);
            raster.setPixels(x+4*j, y, SMALL_NUMERAL_WIDTH+2, SMALL_NUMERAL_HEIGHT+2, digitRaster[digit]);
        }
    }
    protected abstract void drawLevel(Level level, boolean fromScratch);
    protected abstract void drawMonsterList(CreatureList monsterList, BufferedImage overlay);
    protected abstract void drawSlipList(SlipList monsterList, BufferedImage overlay);
    protected abstract void drawButtonConnections(ConnectionButton[] connections, BufferedImage overlay);
    public abstract void drawPositionList(List<Position> positionList, Graphics2D g);
    protected abstract void drawChipHistory(Position currentPosition, BufferedImage overlay);
    
    void updateGraphics(boolean fromScratch) {
        Level level = emulator.getLevel();
        drawLevel(level, fromScratch);
        overlay = new BufferedImage(32 * tileWidth, 32 * tileHeight, BufferedImage.TYPE_4BYTE_ABGR);
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
    
    protected static BufferedImage drawDigit(int n, Color colorBG, Color colorFG){
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
    protected abstract void initialiseTileGraphics(BufferedImage tilespng);
    protected abstract void initialiseBGTileGraphics(BufferedImage tilespng);
    protected abstract void initialiseLayers();

    public void initialise(SuperCC emulator, Image tilespng, TileSheet tileSheet, int tileWidth, int tileHeight) {
        this.emulator = emulator;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.tileSheet = tileSheet;
        initialiseDigits();
        initialiseTileGraphics((BufferedImage) tilespng);
        initialiseBGTileGraphics((BufferedImage) tilespng);
        initialiseLayers();
        if (getMouseListeners().length == 0) { //Just so you don't add extra mouse listeners when you update tilesize or something
            addMouseListener(this);
            addMouseMotionListener(this); //I don't check existing motion listeners cause its always the same number of them as the normal mouse listener
        }
    }
    
    class GamePopupMenu extends JPopupMenu {
        
        GamePopupMenu(GameGraphicPosition position) {
            add(new JLabel("Cheats", SwingConstants.CENTER));
            Cheats cheats = emulator.getLevel().cheats;
            
            Tile tileFG = emulator.getLevel().getLayerFG().get(position);
            Tile tileBG = emulator.getLevel().getLayerBG().get(position);
            for (Tile tile : new Tile[] {tileFG, tileBG}) {
                
                if (tile.isButton()) {
                    JMenuItem press = new JMenuItem("Press button");
                    press.addActionListener((e) -> {
                        cheats.pressButton(position);
                        updateGraphics(false);
                        emulator.getMainWindow().repaint();
                    });
                    add(press);
                }
                
                if (tile == Tile.TRAP) {
                    JMenuItem open = new JMenuItem("Open Trap");
                    open.addActionListener((e) -> cheats.setTrap(position, true));
                    add(open);
                    JMenuItem close = new JMenuItem("Close Trap");
                    close.addActionListener((e) -> cheats.setTrap(position, true));
                    add(close);
                }

                Creature c = emulator.getLevel().getMonsterList().creatureAt(position);
                if (c == null) {
                    if (tile.isMonster()) {
                        JMenuItem animate = new JMenuItem("Animate Monster");
                        animate.addActionListener(e -> {
                            cheats.animateMonster(position);
                        });
                        add(animate);
                    }
                }
                
            }
            
            Creature c = emulator.getLevel().getMonsterList().creatureAt(position);
            if (c != null) {
                JMenu setDirection = new JMenu("Change Creature's Direction");
                for (Direction d : Direction.values()) {
                    JMenuItem menuItem = new JMenuItem(d.toString());
                    menuItem.addActionListener((e) -> {
                        cheats.setDirection(c, d);
                        updateGraphics(false);
                        emulator.getMainWindow().repaint();
                    });
                    setDirection.add(menuItem);
                }
                add(setDirection);
                JMenuItem kill = new JMenuItem("Kill Creature");
                kill.addActionListener((e) -> {
                    cheats.kill(c);
                    updateGraphics(false);
                    emulator.getMainWindow().repaint();
                });
                add(kill);
            }
            
            if (position.equals(emulator.getLevel().getChip().getPosition()) && emulator.getLevel().getChip().isDead()) {
                JMenuItem revive = new JMenuItem("Revive Chip");
                revive.addActionListener((e) -> {
                    cheats.reviveChip();
                    updateGraphics(false);
                    emulator.getMainWindow().repaint();
                });
                add(revive);
            }
            
            JMenuItem teleportChip = new JMenuItem("Move Chip Here");
            teleportChip.addActionListener((e) -> {
                cheats.moveChip(position);
                updateGraphics(false);
                emulator.getMainWindow().repaint();
            });
            add(teleportChip);
            
            JMenuItem pop = new JMenuItem("Remove Tile: "+tileFG.toString());
            pop.addActionListener((e) -> {
                cheats.popTile(position);
                updateGraphics(false);
                emulator.getMainWindow().repaint();
            });
            add(pop);
            
            JMenu insert = new JMenu("Insert Tile");
            Tile[] allTiles = Tile.values();
            for (int i = 0; i < 0x70; i+= 0x10) {
                JMenu tileSubsetMenu = new JMenu(i + " - " + (i+0XF));
                for (int j = i; j < i + 0x10; j++) {
                    Tile t = allTiles[j];
                    JMenuItem menuItem = new JMenuItem(t.toString());
                    menuItem.addActionListener((e) -> {
                        cheats.insertTile(position, t);
                        updateGraphics(false);
                        emulator.getMainWindow().repaint();
                    });
                    tileSubsetMenu.add(menuItem);
                }
                insert.add(tileSubsetMenu);
            }
            
            add(insert);
            
        }
        
    }
    
    public void mouseClicked(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {}
    private void leftClick(GameGraphicPosition clickPosition) {
        Creature chip = emulator.getLevel().getChip();
        if (!emulator.getLevel().getChip().isDead() && !SuperCC.areToolsRunning()) {
            byte b = clickPosition.clickByte(chip.getPosition());
            if (b == UNCLICKABLE) return;
            emulator.showAction("Clicked " + clickPosition);
            emulator.getLevel().setClick(clickPosition.getIndex());
            Direction[] directions = chip.seek(clickPosition);
            emulator.tick(b, directions, TickFlags.GAME_PLAY);
        }
    }
    private void rightClick(GameGraphicPosition clickPosition, MouseEvent e) {
        GamePanel.GamePopupMenu popupMenu = new GamePopupMenu(clickPosition);
        popupMenu.show(e.getComponent(), e.getX(), e.getY());
    }
    public void mouseReleased(MouseEvent e) {
        GameGraphicPosition clickPosition = new GameGraphicPosition(e, tileWidth, tileHeight, screenTopLeft);
        if (e.isPopupTrigger()) rightClick(clickPosition, e);
        else leftClick(clickPosition);
    }
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseDragged(MouseEvent e) {}
    public void mouseMoved(MouseEvent e) {
        GameGraphicPosition pos = new GameGraphicPosition(e, tileWidth, tileHeight, screenTopLeft);
        Tile bgTile = emulator.getLevel().getLayerBG().get(pos),
            fgTile = emulator.getLevel().getLayerFG().get(pos);
        String str = pos.toString() + " " + fgTile;
        if (bgTile != Tile.FLOOR) str += " / " + bgTile;
        emulator.showAction(str);
    }
    
}
