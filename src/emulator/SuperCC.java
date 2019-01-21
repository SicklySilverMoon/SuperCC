package emulator;

import game.*;
import graphics.Gui;
import io.DatParser;
import io.SuccPaths;
import io.TWSReader;
import javax.swing.*;
import java.io.*;

public class SuperCC {

    public static final byte UP = 'u', LEFT = 'l', DOWN = 'd', RIGHT = 'r', WAIT = '-';
    private static final byte[] BYTE_MOVEMENT_KEYS = {UP, LEFT, DOWN, RIGHT, WAIT};
    private static final Direction[][] DIRECTIONS = new Direction[][] {{Direction.UP}, {Direction.LEFT},
        {Direction.DOWN}, {Direction.RIGHT}, {}};
    public static final byte CHIP_RELATIVE_CLICK = 1;

    private SavestateManager savestates;
    private Level level;
    private Gui window;
    private DatParser dat;
    private Solution solution;
    public TWSReader twsReader;
    private SuccPaths paths;
    private EmulatorKeyListener controls;
    
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
        String levelName = new String(level.getTitle());
        levelName = levelName.substring(0, levelName.length()-1).replaceAll("\\s","_");
        return paths.getJSONPath(dat.getLevelsetName(), level.getLevelNumber(), levelName);
    }
    
    public String getSerPath() {
        return getJSONPath().replace(".json", ".ser");
    }
    
    public void repaint(boolean fromScratch) {
        window.repaint(level, fromScratch);
    }
    
    public static boolean isDoubleMove(byte b) {
        return b == 'U' || b == 'L' || b == 'D' || b == 'R' || b == '_';
    }
    
    private static byte capital(byte b){
        if (b == '-') return '_';
        return (byte) Character.toUpperCase((char) b);
    }
    
    public static byte[] lowerCase(byte b) {
        if (b == 'U') return new byte[] {'u', '-'};
        else if (b == 'L') return new byte[] {'l', '-'};
        else if (b == 'D') return new byte[] {'d', '-'};
        else if (b == 'R') return new byte[] {'r', '-'};
        else return new byte[] {b};
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

    public SuperCC() throws IOException {
        File f = new File("settings.txt");
        paths = new SuccPaths(f);
        window = new Gui(this);
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

    public synchronized void loadLevel(int levelNumber, int rngSeed, Step step, boolean keepMoves){
        try{
            if (keepMoves && level != null && levelNumber == level.getLevelNumber()) {
                solution = new Solution(getSavestates().getMoveList(), rngSeed, step);
                solution.load(this);
            }
            else {
                level = dat.parseLevel(levelNumber, rngSeed, step);
                savestates = new SavestateManager(level);
                solution = new Solution(new byte[] {}, 0, Step.EVEN, Solution.HALF_MOVES);
                window.repaint(level, true);
                window.setTitle("SuperCC - " + new String(level.getTitle()));
            }
        }
        catch (Exception e){
            e.printStackTrace();
            throwError("Could not load level: "+e.getMessage());
        }
    }

    public synchronized void loadLevel(int levelNumber){
        loadLevel(levelNumber, 0, Step.EVEN, true);
    }

    public boolean tick(byte b, Direction[] directions, TickFlags flags){
        if (level == null) return false;
        boolean tickTwice = level.tick(b, directions);
        if (flags.doubleTick && tickTwice) {
            b = capital(b);
            level.tick(b, DIRECTIONS[4]);
        }
        if (flags.save) savestates.addRewindState(level, b);
        if (flags.repaint) window.repaint(level, false);
        return tickTwice;
    }
    
    public boolean isClick(byte b){
        return b <= 0;
    }
    
    public boolean tick(byte b, TickFlags flags){
        if (level == null) return false;
        Direction[] directions;
        if (isClick(b)){
            Position screenPosition = Position.screenPosition(level.getChip().getPosition());
            Position clickedPosition = Position.clickPosition(screenPosition, b);
            directions = level.getChip().getPosition().seek(clickedPosition);
            level.setClick(clickedPosition.getIndex());
            return tick(b, directions, flags);
        }
        else{
            for (int i = 0; i < BYTE_MOVEMENT_KEYS.length; i++) {
                if (BYTE_MOVEMENT_KEYS[i] == b) {
                    directions = DIRECTIONS[i];
                    return tick(b, directions, flags);
                }
            }
        }
        return false;
    }
    
    public void showAction(String s){
        getMainWindow().getLastActionPanel().update(s);
        getMainWindow().getLastActionPanel().repaint();
    }
    
    private void runTests() {
        String[] levelsets = new String[] {
            "C:\\Users\\Markus\\Downloads\\CCTools\\tworld-2.2.0\\data\\CHIPS.dat",
            "C:\\Users\\Markus\\Downloads\\CCTools\\tworld-2.2.0\\data\\CCLP1.dat",
            "C:\\Users\\Markus\\Downloads\\CCTools\\tworld-2.2.0\\data\\CCLP3.dat",
            "C:\\Users\\Markus\\Downloads\\CCTools\\tworld-2.2.0\\data\\CCLP4.dat"
        };
        String[] twss = new String[] {
            "C:\\Users\\Markus\\Downloads\\CCTools\\tworld-2.2.0\\save\\public_CHIPS.dac.tws",
            "C:\\Users\\Markus\\Downloads\\CCTools\\tworld-2.2.0\\save\\public_CCLP1.dac.tws",
            "C:\\Users\\Markus\\Downloads\\CCTools\\tworld-2.2.0\\save\\public_CCLP3.dac.tws",
            "C:\\Users\\Markus\\Downloads\\CCTools\\tworld-2.2.0\\save\\public_CCLP4.dac.tws"
        };
        
        for (int i = 0; i < levelsets.length; i++) {
            openLevelset(new File(levelsets[i]));
            setTWSFile(new File(twss[i]));
            System.out.println(new File(levelsets[i]).getName());
            
            for (int j = 1; j <= 149; j++) {
                loadLevel(j);
                try {
                    Solution s = twsReader.readSolution(level);
                    // System.out.println(s.efficiency);
                    s.load(this);
                    for (int waits = 0; waits < 100 & !level.getChip().isDead(); waits++) {
                        level.tick(WAIT, new Direction[] {});
                    }
                    if (level.getLayerFG().get(level.getChip().getPosition()) != Tile.EXITED_CHIP) {
                        System.out.println("failed level "+level.getLevelNumber()+" "+new String(level.getTitle()));
                    }
                }
                catch (Exception exc) {
                    System.out.println("Error loading "+level.getLevelNumber()+" "+new String(level.getTitle()));
                    exc.printStackTrace();
                }
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

    public static void initialise(){
        try{
            SuperCC emulator = new SuperCC();
            emulator.openLevelset(new File("C:\\Users\\Markus\\Downloads\\CCTools\\tworld-2.2.0\\data\\CHIPS.dat")); emulator.setTWSFile(new File("C:\\Users\\Markus\\Downloads\\CCTools\\tworld-2.2.0\\save\\public_CHIPS.dac.tws"));
            //emulator.openLevelset(new File("C:\\Users\\Markus\\Downloads\\CCTools\\tworld-2.2.0\\data\\CCLP1.dat")); emulator.setTWSFile(new File("C:\\Users\\Markus\\Downloads\\CCTools\\tworld-2.2.0\\save\\public_CCLP1.dac.tws"));
            //emulator.openLevelset(new File("C:\\Users\\Markus\\Downloads\\CCTools\\tworld-2.2.0\\data\\CCLP3.dat")); emulator.setTWSFile(new File("C:\\Users\\Markus\\Downloads\\CCTools\\tworld-2.2.0\\save\\public_CCLP3.dac.tws"));
            //emulator.openLevelset(new File("C:\\Users\\Markus\\Downloads\\CCTools\\tworld-2.2.0\\data\\CCLP4.dat")); emulator.setTWSFile(new File("C:\\Users\\Markus\\Downloads\\CCTools\\tworld-2.2.0\\save\\public_CCLP4.dac.tws"));
            //emulator.runBenchmark(134, 200);
            //emulator.runTests();
        }
        catch (IOException e){
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void throwError(String s){
        JOptionPane.showMessageDialog(getMainWindow(), s, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> initialise());
    }

}
