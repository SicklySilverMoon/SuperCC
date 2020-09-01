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
import java.util.Calendar;
import java.util.GregorianCalendar;

public class SuperCC {

    public static final char UP = 'u', LEFT = 'l', DOWN = 'd', RIGHT = 'r', WAIT = '-', UP_LEFT = '↖', DOWN_LEFT = '↙',
            DOWN_RIGHT = '↘', UP_RIGHT = '↗',  MIN_CLICK = '¯', MAX_CLICK = 'ÿ';
    private static final char[] CHAR_MOVEMENT_KEYS = {UP, LEFT, DOWN, RIGHT, WAIT, UP_LEFT, DOWN_LEFT, DOWN_RIGHT, UP_RIGHT};
    private static final Direction[][] DIRECTIONS = new Direction[][] {{Direction.UP}, {Direction.LEFT},
        {Direction.DOWN}, {Direction.RIGHT}, {}, {Direction.UP, Direction.LEFT}, {Direction.DOWN, Direction.LEFT},
            {Direction.DOWN, Direction.RIGHT}, {Direction.UP, Direction.RIGHT}};
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
        String levelName = new String(level.getTitle()).replaceAll("[^a-zA-Z0-9 ]",""); //Delete everything except letters, numbers, and spaces so you won't get issues with illegal filenames
        //levelName = levelName.substring(0, levelName.length()-1).replaceAll("\\s","_"); //No longer needed as the previous line now takes care of this but kept commented in case its needed in future
        return paths.getJSONPath(dat.getLevelsetName(), level.getLevelNumber(), levelName, level.getRuleset().name());
    }
    
    public String getSerPath() {
        return getJSONPath().replace(".json", ".ser");
    }
    
    public void repaint(boolean fromScratch) {
        window.repaint(fromScratch);
    }
    
    public static char capital(char c){
        switch (c) {
            case WAIT: return '_';
            case UP_LEFT: return '⇖';
            case DOWN_LEFT: return '⇙';
            case DOWN_RIGHT: return '⇘';
            case UP_RIGHT: return '⇗';
            default: return Character.toUpperCase(c);
        }
    }
    
    public static char[] lowerCase(char c) {
        switch (c) {
            case 'U': return new char[] {UP, WAIT};
            case 'L': return new char[] {LEFT, WAIT};
            case 'D': return new char[] {DOWN, WAIT};
            case 'R': return new char[] {RIGHT, WAIT};
            case '_': return new char[] {WAIT, WAIT};
            case '⇖': return new char[] {UP_LEFT, WAIT};
            case '⇙': return new char[] {DOWN_LEFT, WAIT};
            case '⇘': return new char[] {DOWN_RIGHT, WAIT};
            case '⇗': return new char[] {UP_RIGHT, WAIT};
            default: return new char[] {c};
        }
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
            throwError("Could not find settings.ini file, creating"); //If it can't find the settings file make it with some defaults
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
                solution = new Solution(new char[] {}, 0, Step.EVEN, Solution.HALF_MOVES, level.getRuleset(), Direction.UP);
                if(hasGui) {
                    window.repaint(true);
                    window.setTitle("SuperCC - " + new String(level.getTitle()));
                }
            }
        }
        catch (Exception e){
            e.printStackTrace();
            throwError("Could not load level: "+e.getMessage());
        }
    }

    public synchronized void loadLevel(int levelNumber){
        SeedSearch.kill();
        loadLevel(levelNumber, 0, Step.EVEN, true, Ruleset.CURRENT, Direction.UP);
    }

    public boolean tick(char c, Direction[] directions, TickFlags flags){
        if (level == null) return false;
        boolean tickMulti = level.tick(c, directions);
        if (flags.multiTick && tickMulti) {
            for (int i=0; i < level.ticksPerMove() - 1; i++) {
                c = capital(c); //todo: see the todo in Solution about capitalization and switching to another system
                level.tick(c, DIRECTIONS[4]);
            }
        }
        if (flags.save) {
            savestates.addRewindState(level, c);
        }
        if (flags.repaint) window.repaint(false);

        if (savestates.isUndesirableSavestate()) { //As far as I'm aware the way the actual method is setup its only possible to encounter this during gameplay, which is what we want
            throwMessage("Undesirable State Reached"); //Just a little pop up window that tells the user that they reached a prior marked undesirable state
        }

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
            for (int i = 0; i < CHAR_MOVEMENT_KEYS.length; i++) {
                if (CHAR_MOVEMENT_KEYS[i] == c) {
                    directions = DIRECTIONS[i];
                    return tick(c, directions, flags);
                }
            }
        }
        return false;
    }

    public static boolean isClick(char c){
        return c <= MAX_CLICK && c >= MIN_CLICK;
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
                for (int waits = 0; waits < 100 & !level.getChip().isDead(); waits++) {
                    level.tick(WAIT, new Direction[] {});
                }
                if (level.getLayerFG().get(level.getChip().getPosition()) != Tile.EXITED_CHIP && !level.isCompleted()) {
                    System.out.println("failed level "+level.getLevelNumber()+" "+new String(level.getTitle()));
                }
            }
            catch (Exception exc) {
                System.out.println("Error loading "+level.getLevelNumber()+" "+new String(level.getTitle()));
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

//        Calendar rightNow = Calendar.getInstance();
//        Calendar endDate = new GregorianCalendar(2020, Calendar.SEPTEMBER, 13);
//        if (rightNow.after(endDate)) {
//            System.err.println("Today's date is after the specified end period of: " + endDate.getTime());
//            System.exit(0);
//        }

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
        TileSheet tileSheet = tileSheets[paths.getTilesetNum()];
        int[] tileSizes = paths.getTileSizes();
        int width = tileSizes[0];
        int height = tileSizes[1];
        SmallGamePanel gamePanel = (SmallGamePanel) window.getGamePanel();
        this.getMainWindow().getGamePanel().setTileSheet(tileSheet);
        BufferedImage tilesetImage = null;
        try {
            tilesetImage = tileSheet.getTileSheet(width, height);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //see if all of this can't be refactored out of existence by pressing the buttons in MenuBar if their setting is changed
        window.getGamePanel().initialise(this, tilesetImage, tileSheet, tileSizes[0], tileSizes[1]);
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
        JOptionPane.showMessageDialog(getMainWindow(), s, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public void throwMessage(String s){
        JOptionPane.showMessageDialog(getMainWindow(), s, "SuCC Message", JOptionPane.PLAIN_MESSAGE);
    }

    public boolean throwQuestion(String s) {
        return JOptionPane.showConfirmDialog(getMainWindow(), s, "SuCC Option",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> initialise(args));
    }

}
