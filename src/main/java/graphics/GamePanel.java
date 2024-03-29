package graphics;

import emulator.SuperCC;
import emulator.TickFlags;
import game.*;
import game.MS.*;
import game.button.ConnectionButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Collection;
import java.util.List;

import static game.Position.UNCLICKABLE;

public abstract class GamePanel extends JPanel
    implements MouseMotionListener, MouseListener {
    
    static final int CHANNELS = 4, SMALL_NUMERAL_WIDTH = 3, SMALL_NUMERAL_HEIGHT = 5;
    protected int tileHeight, tileWidth;
    protected double hBetweenTiles, vBetweenTiles;
    private TileSheet tileSheet;

    protected Position screenTopLeft = new Position(0, 0);
    
    protected static final int[][]
        blackDigits = new int[10][(SMALL_NUMERAL_WIDTH+2)*(SMALL_NUMERAL_HEIGHT+2)*CHANNELS],
        blueDigits = new int[10][(SMALL_NUMERAL_WIDTH+2)*(SMALL_NUMERAL_HEIGHT+2)*CHANNELS];
    static int[][] tileImage, overlayTileImage;
    static Image[][] creatureImages;
    
    protected boolean showMonsterListNumbers = true, showSlipListNumbers = true; //Makes sure that the BG layer, the slip list, and the monster list show by default
    protected boolean showTrapConnections, showCloneConnections, showHistory;

    // The background image
    protected BufferedImage lowerImage;
    // The foreground image
    protected BufferedImage upperImage;
    // The image behind the background (32*32 floor tiles)
    protected BufferedImage backingImage;
    //Where extras are drawn (slip/monster list #s, move history, etc.)
    protected BufferedImage overlayImage;
    
    protected SuperCC emulator;

    protected ConnectionButton hoveredButton;
    
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
        g.drawImage(backingImage, 0, 0, null);
        g.drawImage(lowerImage, 0, 0, null);
        g.drawImage(upperImage, 0, 0, null);
        g.drawImage(overlayImage, 0, 0, null);
    }
    
    public static void drawNumber(int n, int[][] digitRaster, int x, int y, WritableRaster raster){
        char[] chars = (String.valueOf(n)).toCharArray();
        for (int j = 0; j < chars.length; j++){
            int digit = Character.digit(chars[j], 10);
            raster.setPixels(x+4*j, y, SMALL_NUMERAL_WIDTH+2, SMALL_NUMERAL_HEIGHT+2, digitRaster[digit]);
        }
    }
    protected abstract void drawLevel(Level level, boolean fromScratch);
    protected abstract void drawMonsterListNumbers(Level level, CreatureList monsterList, BufferedImage overlay);
    protected abstract void drawSlipListNumbers(SlipList monsterList, BufferedImage overlay);
    protected abstract void drawButtonConnections(Collection<? extends ConnectionButton> connections, BufferedImage overlay, Color color);
    public abstract void drawPositionList(List<Position> positionList, Graphics2D g);
    protected abstract void drawChipHistory(Position currentPosition, BufferedImage overlay);
    
    void updateGraphics(boolean fromScratch) {
        Level level = emulator.getLevel();
        drawLevel(level, fromScratch);
        overlayImage = new BufferedImage(32 * tileWidth, 32 * tileHeight, BufferedImage.TYPE_4BYTE_ABGR);
        if (showMonsterListNumbers) drawMonsterListNumbers(level, level.getMonsterList(), overlayImage);
        if (showSlipListNumbers && level.supportsSliplist()) drawSlipListNumbers(level.getSlipList(), overlayImage);
        if (showCloneConnections) drawButtonConnections(level.getRedButtons().allValues(), overlayImage, Color.BLACK);
        if (showTrapConnections) drawButtonConnections(level.getBrownButtons().allValues(), overlayImage, Color.BLACK);
        if (hoveredButton != null) drawButtonConnections(List.of(hoveredButton), overlayImage, Color.RED);
        if (showHistory) drawChipHistory(level.getChip().getPosition(), overlayImage);
    }

    public void setMonsterListVisible(boolean visible){
        showMonsterListNumbers = visible;
    }
    public void setSlipListVisible(boolean visible){
        showSlipListNumbers = visible;
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
    protected abstract void initialiseOverlayGraphics(BufferedImage tilespng);
    protected abstract void initialiseCreatureGraphics(BufferedImage overlayImage, BufferedImage tilesImage);
    protected abstract void initialiseLayers();

    public void initialise(SuperCC emulator, BufferedImage[] tilespng, TileSheet tileSheet, int tileWidth, int tileHeight) {
        this.emulator = emulator;
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.hBetweenTiles = tileWidth / 8.0;
        this.vBetweenTiles = tileHeight / 8.0;
        this.tileSheet = tileSheet;
        initialiseDigits();
        initialiseTileGraphics(tilespng[0]);
        initialiseOverlayGraphics(tilespng[1]);
        initialiseCreatureGraphics(tilespng[1], tilespng[0]);
        initialiseLayers();
        if (getMouseListeners().length == 0) { //Just so you don't add extra mouse listeners when you update tilesize or something
            addMouseListener(this);
            addMouseMotionListener(this); //I don't check existing motion listeners cause its always the same number of them as the normal mouse listener
        }
    }
    
    class GamePopupMenu extends JPopupMenu {
        
        GamePopupMenu(Position position) {
            add(new JLabel("Cheats", SwingConstants.CENTER));
            Level level = emulator.getLevel();
            Cheats cheats = level.getCheats();

            Tile tileFG = level.getLayerFG().get(position);
            Tile[] tiles;
            if(level.supportsLayerBG()) {
                Tile tileBG = level.getLayerBG().get(position);
                tiles = new Tile[] {tileFG, tileBG};
            } else {
                tiles = new Tile[] {tileFG};
            }
            for (Tile tile : tiles) {
                
                if (tile.isButton()) {
                    JMenuItem press = new JMenuItem("Press button");
                    press.addActionListener((e) -> {
                        cheats.pressButton(position);
                        updateGraphics(false);
                        emulator.getMainWindow().repaint();
                    });
                    add(press);
                }
                
                if (tile == Tile.TRAP && !level.trapRequiresHeldButton()) {
                    JMenuItem open = new JMenuItem("Open Trap");
                    open.addActionListener((e) -> cheats.setTrap(position, true));
                    add(open);
                    JMenuItem close = new JMenuItem("Close Trap");
                    close.addActionListener((e) -> cheats.setTrap(position, false));
                    add(close);
                }

                Creature c = emulator.getLevel().getMonsterList().creatureAt(position, false);
                if (c == null) {
                    if (tile.isMonster()) {
                        JMenuItem animate = new JMenuItem("Animate Monster");
                        animate.addActionListener(e -> cheats.animateMonster(position));
                        add(animate);
                    }
                }
                
            }
            
            Creature c = emulator.getLevel().getMonsterList().creatureAt(position, level.chipInMonsterList());
            if (c != null) {
                JMenu setDirection = new JMenu("Change Creature's Direction");
                for (Direction d : Direction.CARDINALS) {
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
            if (c == null) {
                JMenuItem pop = new JMenuItem("Remove Tile: " + tileFG.toString());
                pop.addActionListener((e) -> {
                    if(emulator.getLevel().supportsSliplist()) {
                        emulator.getLevel().getSlipList().removeIf(creature -> creature.getPosition().equals(position)); //In practice this only affects sliding blocks
                    }
                    cheats.popTile(position);
                    updateGraphics(false);
                    emulator.getMainWindow().repaint();
                });
                add(pop);
            }
            
            JMenu insertTile = new JMenu("Insert Tile");
            Tile[] allTiles = Tile.values();
            for (int i = 0; i < allTiles.length; i+= 0x10) {
                if (!level.creaturesAreTiles() && (Tile.BUG_UP.ordinal() <= i && i <= Tile.BLOB_RIGHT.ordinal()))
                    continue;
                JMenu tileSubsetMenu = new JMenu(String.format("0x%02X - 0x%02X", i, i+0XF));
                for (int j = i; j < i + 0x10; j++) {
                    Tile t = allTiles[j];
                    if (!level.creaturesAreTiles() && (t.isCreature() || t.isChip() || t.isBlock() || t.isSwimmingChip()))
                        continue;
                    JMenuItem menuItem = new JMenuItem(String.format("0x%02X - %s", j, t.toString()));
                    menuItem.addActionListener((e) -> {
                        cheats.insertTile(position, t);
                        updateGraphics(false);
                        emulator.getMainWindow().repaint();
                    });
                    tileSubsetMenu.add(menuItem);
                }
                insertTile.add(tileSubsetMenu);
            }
            add(insertTile);

            JMenuItem insertCreature = new JMenu("Insert Creature");
            CreatureID[] allIDs = CreatureID.values();
            for (CreatureID id : allIDs) {
                //ah, the mess that is different rulesets
                if (!level.hasStillTanks() && id == CreatureID.TANK_STATIONARY)
                    continue;
                if (!level.blocksInMonsterList() && id.isBlock() || id == CreatureID.ICE_BLOCK)
                    continue;
                if (!level.swimmingChipIsCreature() && id == CreatureID.CHIP_SWIMMING)
                    continue;
                if (id.isChip() || id == CreatureID.DEAD)
                    continue;

                JMenu dirSubMenu = new JMenu(id.prettyPrint());
                Direction[] cardinals = Direction.CARDINALS;
                for (Direction d : cardinals) {
                    JMenuItem menuItem = new JMenuItem(d.toString());
                    menuItem.addActionListener(e -> {
                        cheats.insertCreature(d, id, position);
                        emulator.getMainWindow().repaint(false);
                    });
                    dirSubMenu.add(menuItem);
                }
                insertCreature.add(dirSubMenu);
            }
            add(insertCreature);
        }
        
    }
    
    public void mouseClicked(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {
        mousePressedOrReleased(e, true);
    }
    public void mouseReleased(MouseEvent e) {
        mousePressedOrReleased(e, false);
    }
    private void mousePressedOrReleased(MouseEvent e, boolean checkLeft) {
        if(emulator.isLevelLoaded()) {
            Position clickPosition = new Position(e.getX() / tileWidth + screenTopLeft.getX(), e.getY() / tileHeight + screenTopLeft.getY());
            if (e.isPopupTrigger()) //docs for this say that it should be checked on both press and release
                rightClick(clickPosition, e);
            else if (checkLeft && SwingUtilities.isLeftMouseButton(e))
                leftClick(clickPosition);
        }
    }
    private void leftClick(Position clickPosition) {
        if(emulator.isLevelLoaded()) {
            if (!emulator.getLevel().supportsClick()) return;
            MSCreature chip = (MSCreature) emulator.getLevel().getChip(); //Relies on MS code, might want to refactor that
            if (!emulator.getLevel().getChip().isDead() && !SuperCC.areToolsRunning()) {
                char c = clickPosition.clickChar(chip.getPosition());
                if (c == UNCLICKABLE) return;
                emulator.showAction("Clicked " + clickPosition);
                emulator.getLevel().setClick(clickPosition.getIndex());
                Direction[] directions = chip.seek(clickPosition);
                emulator.tick(c, directions, TickFlags.GAME_PLAY);
            }
        }
    }
    private void rightClick(Position clickPosition, MouseEvent e) {
        if(emulator.isLevelLoaded()) {
            GamePanel.GamePopupMenu popupMenu = new GamePopupMenu(clickPosition);
            popupMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mouseDragged(MouseEvent e) {}
    public void mouseMoved(MouseEvent e) {
        if (emulator.isLevelLoaded()) {
            Level level = emulator.getLevel();
            Position pos = new Position(e.getX() / tileWidth + screenTopLeft.getX(), e.getY() / tileHeight + screenTopLeft.getY());
            Tile bgTile = null;
            Tile fgTile;
            if (level.supportsLayerBG())
                bgTile = level.getLayerBG().get(pos);
            fgTile = level.getLayerFG().get(pos);
            String str = pos + " " + fgTile;
            if (level.supportsLayerBG()) {
                if (bgTile != Tile.FLOOR) str += " / " + bgTile;
            }
            emulator.showAction(str);

            hoveredButton = null;
            boolean fgButton = fgTile == Tile.BUTTON_BROWN || fgTile == Tile.BUTTON_RED;
            boolean bgButton = level.supportsLayerBG() && (bgTile == Tile.BUTTON_BROWN || bgTile == Tile.BUTTON_RED);
            if (fgButton || bgButton) {
                hoveredButton = (ConnectionButton) emulator.getLevel().getButton(pos); //red and brown buttons SHOULD always be safe to downcast like this
            }
            emulator.getMainWindow().repaint(false);
        }
    }
}
