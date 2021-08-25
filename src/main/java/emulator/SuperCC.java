package emulator;

import game.*;
import graphics.Gui;
import graphics.SmallGamePanel;
import graphics.TileSheet;
import io.DatParser;
import io.SuccPaths;
import io.TWSReader;
import tools.SeedSearch;
import tools.TSPGUI;
import tools.VariationTesting;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

public class SuperCC {

    public static final char UP = 'u', LEFT = 'l', DOWN = 'd', RIGHT = 'r', WAIT = '-', UP_LEFT = '↖', DOWN_LEFT = '↙',
            DOWN_RIGHT = '↘', UP_RIGHT = '↗',  MIN_CLICK_LOWERCASE = '¯', MAX_CLICK_LOWERCASE = 'ÿ',
    MIN_CLICK_UPPERCASE = 'Ā', MAX_CLICK_UPPERCASE = 'Ő';
    private static final char[] CHAR_MOVEMENT_KEYS = {UP, LEFT, DOWN, RIGHT, UP_LEFT, DOWN_LEFT, DOWN_RIGHT, UP_RIGHT, WAIT};
    private static final Map<Character, Direction> DIRECTIONS = Map.of(UP, Direction.UP, LEFT, Direction.LEFT,
            DOWN, Direction.DOWN, RIGHT, Direction.RIGHT, UP_LEFT, Direction.UP_LEFT, DOWN_LEFT, Direction.DOWN_LEFT,
            DOWN_RIGHT, Direction.DOWN_RIGHT, UP_RIGHT, Direction.UP_RIGHT, WAIT, Direction.NONE);
    public static final byte CHIP_RELATIVE_CLICK = 1;

    private SavestateManager savestates;
//    SavestateCompressor savestateCompressor = new SavestateCompressor();
    private Level level;
    private Gui window;
    private DatParser dat;
    private Solution solution;
    public TWSReader twsReader;
    private SuccPaths paths;
    private EmulatorKeyListener controls;
    public boolean hasGui = true;

    public void setControls(EmulatorKeyListener l) {
        controls = l;
    }
    public EmulatorKeyListener getControls() {
        return controls;
    }
    
    public SuccPaths getPaths() {
        return paths;
    }
    
    public String getJSONPath() {
        String levelName = level.getTitle().replaceAll("[^a-zA-Z0-9 ]",""); //Delete everything except letters, numbers, and spaces so you won't get issues with illegal filenames
        //levelName = levelName.substring(0, levelName.length()-1).replaceAll("\\s","_"); //No longer needed as the previous line now takes care of this but kept commented in case its needed in future
        return paths.getJSONPath(dat.getLevelsetName(), level.getLevelNumber(), levelName, level.getRuleset().name());
    }
    
    public String getSerPath() {
        return getJSONPath().replace(".json", ".ser");
    }

    public String getLevelsetPath() {
        return dat.getLevelsetPath();
    }

    public void repaint(boolean fromScratch) {
        window.repaint(fromScratch);
    }
    
    public static char capital(char c){
        if (isClick(c) && isLowercase(c))
            return (char) (c + (MAX_CLICK_UPPERCASE - MAX_CLICK_LOWERCASE)); //puts it into the uppercase click range
        return switch (c) {
            case WAIT -> '_';
            case UP_LEFT -> '⇖';
            case DOWN_LEFT -> '⇙';
            case DOWN_RIGHT -> '⇘';
            case UP_RIGHT -> '⇗';
            default -> Character.toUpperCase(c);
        };
    }
    
    public static char lowerCase(char c) {
        if (isClick(c) && isUppercase(c))
            return (char) (c - (MAX_CLICK_UPPERCASE - MAX_CLICK_LOWERCASE)); //puts it into the lowercase click range
        return switch (c) {
            case 'U' -> UP;
            case 'L' -> LEFT;
            case 'D' -> DOWN;
            case 'R' -> RIGHT;
            case '_' -> WAIT;
            case '⇖' -> UP_LEFT;
            case '⇙' -> DOWN_LEFT;
            case '⇘' -> DOWN_RIGHT;
            case '⇗' -> UP_RIGHT;
            default -> Character.toLowerCase(c);
        };
    }
    
    public int lastLevelNumber() {
        return dat.lastLevel();
    }

    public void setTWSFile(File twsFile){
        try{
            this.twsReader = new TWSReader(twsFile);
        }
        catch (IOException e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Could not read file:\n"+e.getLocalizedMessage());
        }
    }

    public Level getLevel(){
        return level;
    }
    public Solution getSolution() {
        return solution;
    }
    public SavestateManager getSavestates(){
        return savestates;
    }
    public void setSavestates(SavestateManager sm) {
        this.savestates = sm;
    }
    public Gui getMainWindow(){
        return window;
    }

    public SuperCC() {
        try {
            File f = new File("settings.ini");
            paths = new SuccPaths(f);
        }
        catch (IOException e){
            throwMessage("Could not find settings.ini file, creating"); //If it can't find the settings file make it with some defaults
                SuccPaths.createSettingsFile();
                //Now that the settings file exists we can call this again safely
                File f = new File("settings.ini");
            try {
                paths = new SuccPaths(f);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        window = new Gui(this);
    }

    // GUI-less emulator - used for tests
    public SuperCC(boolean hasGui) {
        this.hasGui = hasGui;
    }

    public void openLevelset(File levelset){
        try{
            dat = new DatParser(levelset);
        }
        catch (IOException e){
            throwError("Could not read file:\n"+e.getLocalizedMessage());
        }
        loadLevel(1);
    }

    public synchronized void loadLevel(int levelNumber, int rngSeed, Step step, boolean keepMoves, Ruleset rules, Direction initialSlide){
        if (levelNumber == 0) levelNumber = lastLevelNumber()-1; //If the level number is 0 (player goes back from level 1, load the last level)
        if (levelNumber == lastLevelNumber()) levelNumber = 1; //And vice versa
        try{
            if (keepMoves && level != null && levelNumber == level.getLevelNumber()) {
                solution = new Solution(getSavestates().getMoveList(), rngSeed, step, level.getRuleset(), initialSlide);
                solution.load(this);
            }
            else {
                level = dat.parseLevel(levelNumber, rngSeed, step, rules, initialSlide);
                savestates = new SavestateManager(this, level);
                solution = new Solution(new char[] {}, 0, Step.EVEN, Solution.BASIC_MOVES, level.getRuleset(), Direction.UP);
                if(hasGui) {
                    window.repaint(true);
                    window.setTitle("SuperCC - " + level.getTitle());
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
            throwError("Could not load level: "+e.getMessage());
        }
    }

    public synchronized void loadLevel(int levelNumber){
        loadLevel(levelNumber, 0, Step.EVEN, true, Ruleset.CURRENT, Direction.UP);
    }

    public boolean tick(char c, Direction[] directions, TickFlags flags){
        if (level == null)
            return false;
        if (directions[0].isDiagonal() && !level.supportsDiagonal()) {
            directions[0] = directions[0].decompose()[0]; //take vertical
            for (char c1 : DIRECTIONS.keySet()) { //switch the char part to vertical
                if (directions[0] == DIRECTIONS.get(c1))
                    c = c1;
            }
        }
        boolean tickMulti = level.tick(c, directions);
        if (flags.multiTick && tickMulti) {
            for (int i=0; i < level.ticksPerMove() - 1; i++) {
                c = capital(c);
                level.tick(c, new Direction[] {Direction.NONE});
            }
        }
        if (flags.save) {
            savestates.addRewindState(level, c);
        }
        if (flags.repaint) window.repaint(false);

        return tickMulti;
    }
    
    public boolean tick(char c, TickFlags flags){
        if (level == null) return false;
        Direction[] directions;
        if (isClick(c)){
            Position screenPosition = Position.screenPosition(level.getChip().getPosition());
            Position clickedPosition = Position.clickPosition(screenPosition, c);
            directions = level.getChip().getPosition().seek(clickedPosition);
            level.setClick(clickedPosition.getIndex());
            return tick(c, directions, flags);
        }
        else{
            for (char charMovementKey : CHAR_MOVEMENT_KEYS) {
                if (charMovementKey == c) {
                    directions = new Direction[] {DIRECTIONS.get(c)};
                    return tick(c, directions, flags);
                }
            }
        }
        return false;
    }

    public boolean isLevelLoaded() {
        return level != null;
    }

    public static boolean isClick(char c){
        return c <= MAX_CLICK_UPPERCASE && c >= MIN_CLICK_LOWERCASE;
    }

    public static boolean isUppercase(char c) {
        return c == 'U' || c == 'L' || c == 'D' || c == 'R' || c == '_' || c == '⇖' || c == '⇙' || c == '⇘' || c == '⇗'
                || (c <= MAX_CLICK_UPPERCASE && c >= MIN_CLICK_UPPERCASE);
    }

    public static boolean isLowercase(char c) {
        return c == UP || c == LEFT || c == DOWN || c == RIGHT || c == UP_LEFT || c == DOWN_LEFT || c == WAIT
                || c == DOWN_RIGHT || c == UP_RIGHT || (c <= MAX_CLICK_LOWERCASE && c >= MIN_CLICK_LOWERCASE);
    }

    public void showAction(String s){
        getMainWindow().getLastActionPanel().update(s);
        getMainWindow().getLastActionPanel().repaint();
    }

    void testTWS() {
        System.out.println(dat.getLevelsetName());

        for (int j = 1; j <= level.getLevelsetLength(); j++) {
            loadLevel(j);
            try {
                Solution s = twsReader.readSolution(level);
                // System.out.println(s.efficiency);
                s.load(this);
                if (level.getLayerFG().get(level.getChip().getPosition()) != Tile.EXITED_CHIP && !level.isCompleted()) {
                    System.out.println("failed level "+level.getLevelNumber()+" "+ level.getTitle());
                }
            }
            catch (Exception exc) {
                System.out.println("Error loading "+level.getLevelNumber()+" "+ level.getTitle());
                exc.printStackTrace();
            }
        }
    }
    
    private void runBenchmark(int levelNumber, int runs){
        loadLevel(levelNumber);
        Solution s;
        try {
            s = twsReader.readSolution(level);
            System.out.println("Running test without writing.");
            long startTime = System.nanoTime();
            for (int i = 0; i < runs; i++) s.load(this, TickFlags.LIGHT);
            long endTime = System.nanoTime();
            double timePerIteration = (endTime - startTime) / (double) runs;
            System.out.println("Time per iteration:");
            System.out.println((timePerIteration / 1000000)+"ms");
            System.out.println((timePerIteration / 1000000000)+"s\n");
            System.out.println("Running test with writing.");
            startTime = System.nanoTime();
            for (int i = 0; i < runs; i++) s.load(this);
            endTime = System.nanoTime();
            timePerIteration = (endTime - startTime) / (double) runs;
            System.out.println("Time per iteration:");
            System.out.println((timePerIteration / 1000000)+"ms");
            System.out.println((timePerIteration / 1000000000)+"s");
            double numMoves = savestates.getMoves().length;
            int size = savestates.getSavestate().length;
            while (savestates.getNode().hasParent()){
                savestates.rewind();
                size += savestates.getSavestate().length;
            }
            System.out.println("\nTotal state size:");
            System.out.println((size / (double) 1000)+" kb");
            System.out.println("\nAverage state size:");
            System.out.println((size / numMoves / 1000)+" kb");
        }
        catch (IOException e){
            System.out.println("Benchmark of level "+level+"failed");
        }
    }

    public static void initialise(String[] args){
        SuperCC emulator = new SuperCC();

        try {
            ArgumentParser.parseArguments(emulator, args); //Parses any command line arguments given
        } catch (IllegalArgumentException e) {
            emulator.throwError(e.toString() + "\nSee stderr for flag use");
        }

        emulator.initialiseTilesheet();
    }

    private void initialiseTilesheet() {
        Gui window = this.getMainWindow();
        SuccPaths paths = this.getPaths();
        TileSheet[] tileSheets = TileSheet.values();
        TileSheet tileSheet = tileSheets[paths.getMSTilesetNum()];
        int[] tileSizes = paths.getTileSizes();
        int width = tileSizes[0];
        int height = tileSizes[1];
        SmallGamePanel gamePanel = (SmallGamePanel) window.getGamePanel();
        this.getMainWindow().getGamePanel().setTileSheet(tileSheet);
        BufferedImage[] tilesetImages = null;
        try {
            tilesetImages = tileSheet.getTileSheets(width, height);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //see if all of this can't be refactored out of existence by pressing the buttons in MenuBar if their setting is changed
        window.getGamePanel().initialise(this, tilesetImages, tileSheet, tileSizes[0], tileSizes[1]);
        window.getInventoryPanel().initialise(this);
        window.setSize(200+width*gamePanel.getWindowSizeX(), 200+height*gamePanel.getWindowSizeY());
        window.getGamePanel().setPreferredSize(new Dimension(width * gamePanel.getWindowSizeX(), height * gamePanel.getWindowSizeY()));
        window.getGamePanel().setSize(width*gamePanel.getWindowSizeX(), height*gamePanel.getWindowSizeY());

        window.getLevelPanel().changeNotation(paths.getTWSNotation());

        window.pack();
        window.repaint(true);
    }

    public static boolean areToolsRunning() {
        return (SeedSearch.isRunning() || TSPGUI.isRunning() || VariationTesting.isRunning());
    }

    public void throwError(String s){
        if (hasGui)
            JOptionPane.showMessageDialog(getMainWindow(), s, "Error", JOptionPane.ERROR_MESSAGE);
        else
            System.err.println("[SuperCC Error] " + s);
    }

    public void throwMessage(String s){
        if (hasGui)
            JOptionPane.showMessageDialog(getMainWindow(), s, "SuCC Message", JOptionPane.PLAIN_MESSAGE);
        else
            System.out.println("[SuperCC Message] " + s);
    }

    public boolean throwQuestion(String s) {
        if (hasGui) {
            return JOptionPane.showConfirmDialog(getMainWindow(), s, "SuCC Option",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
        }
        else {
            System.err.println("Tried throwing question without GUI!");
            return false;
        }
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> initialise(args));
    }

}
