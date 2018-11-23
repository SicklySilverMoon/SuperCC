package emulator;

import game.*;
import graphics.Gui;
import io.DatParser;
import io.SuccPaths;
import io.TWSReader;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;

public class SuperCC implements KeyListener{

    private static final int[] MOVEMENT_KEYS = {KeyEvent.VK_UP, KeyEvent.VK_LEFT, KeyEvent.VK_DOWN, KeyEvent.VK_RIGHT,
            KeyEvent.VK_SPACE};
    public static final byte UP = 'u', LEFT = 'l', DOWN = 'd', RIGHT = 'r', WAIT = '-';
    private static final byte[] BYTE_MOVEMENT_KEYS = {UP, LEFT, DOWN, RIGHT, WAIT};
    private static final int[][] DIRECTIONS = new int[][] {{Creature.DIRECTION_UP}, {Creature.DIRECTION_LEFT},
        {Creature.DIRECTION_DOWN}, {Creature.DIRECTION_RIGHT}, {}};
    public static final byte CHIP_RELATIVE_CLICK = 1;

    private boolean shiftPressed = false;

    private SavestateManager savestates;
    private Level level;
    private Gui window;
    private DatParser dat;
    private Solution solution;
    public TWSReader twsReader;
    private SuccPaths paths;
    
    public SuccPaths getPaths() {
        return paths;
    }
    
    public String getJSONPath() {
        String levelName = new String(level.title);
        levelName = levelName.substring(0, levelName.length()-1).replaceAll("\\s","_");
        return paths.getJSONPath(dat.getLevelsetName(), level.levelNumber, levelName);
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
    
    public void setTWSFile(File twsFile){
        try{
            this.twsReader = new TWSReader(twsFile);
        }
        catch (IOException e){
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
    public Gui getMainWindow(){
        return window;
    }

    public SuperCC() throws IOException {
        System.out.println(Thread.currentThread().getContextClassLoader().getResource("emulator/settings.txt"));
        paths = new SuccPaths(new File(Thread.currentThread().getContextClassLoader().getResource("emulator/settings.txt").getFile()));
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

    public synchronized void loadLevel(int levelNumber, int rngSeed, Step step){
        try{
            level = dat.parseLevel(levelNumber, rngSeed, step);
            savestates = new SavestateManager(level);
            solution = new Solution(new byte[] {}, 0, Step.EVEN, Solution.HALF_MOVES);
            window.repaint(level, true);
            window.setTitle("SuperCC - " + new String(level.title));
        }
        catch (Exception e){
            e.printStackTrace();
            throwError("Could not load level: "+e.getMessage());
        }
    }

    public synchronized void loadLevel(int levelNumber){
        loadLevel(levelNumber, 0, Step.EVEN);
    }

    public boolean tick(byte b, int[] directions, TickFlags flags){
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
        int[] directions;
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

    public void keyTyped(KeyEvent e){}
    public void keyReleased(KeyEvent e){
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_SHIFT) shiftPressed = false;
    }
    public void keyPressed(KeyEvent e){
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_SHIFT){
            shiftPressed = true;
            return;
        }

        for (int i = 0; i < 5; i++){
            if (key == MOVEMENT_KEYS[i]){
                if (level.getChip().isDead()) return;
                tick(BYTE_MOVEMENT_KEYS[i], DIRECTIONS[i], TickFlags.GAME_PLAY);
                return;
            }
        }
    
        if (key == KeyEvent.VK_BACK_SPACE) {
            savestates.rewind();
            level.load(savestates.getSavestate());
            showAction("Rewind");
            window.repaint(level, false);
            return;
        }
        
        if (key == KeyEvent.VK_ENTER) {
            savestates.replay();
            level.load(savestates.getSavestate());
            showAction("Replay");
            window.repaint(level, false);
            return;
        }

        if (shiftPressed) {
            savestates.addSavestate(key);
            showAction("State " + KeyEvent.getKeyText(e.getKeyCode()) + " saved");
            window.repaint(level, false);
        }
        else {
            if (savestates.load(key, level)) {
                showAction("State " + KeyEvent.getKeyText(e.getKeyCode()) + " loaded");
                window.repaint(level, false);
            }
        }

    }
    
    public void showAction(String s){
        getMainWindow().getLastActionPanel().update(s);
    }
    
    private void runTests() {
        String[] levelsets = new String[] {
            //"C:\\Users\\Markus\\Downloads\\CCTools\\tworld-2.2.0\\data\\CHIPS.dat",
            "C:\\Users\\Markus\\Downloads\\CCTools\\tworld-2.2.0\\data\\CCLP1.dat",
            //"C:\\Users\\Markus\\Downloads\\CCTools\\tworld-2.2.0\\data\\CCLP3.dat",
            //"C:\\Users\\Markus\\Downloads\\CCTools\\tworld-2.2.0\\data\\CCLP4.dat"
        };
        String[] twss = new String[] {
            //"C:\\Users\\Markus\\Downloads\\CCTools\\tworld-2.2.0\\save\\public_CHIPS.dac.tws",
            "C:\\Users\\Markus\\Downloads\\CCTools\\tworld-2.2.0\\save\\public_CCLP1.dac.tws",
            //"C:\\Users\\Markus\\Downloads\\CCTools\\tworld-2.2.0\\save\\public_CCLP3.dac.tws",
            //"C:\\Users\\Markus\\Downloads\\CCTools\\tworld-2.2.0\\save\\public_CCLP4.dac.tws"
        };
        
        for (int i = 0; i < levelsets.length; i++) {
            openLevelset(new File(levelsets[i]));
            setTWSFile(new File(twss[i]));
            System.out.println(new File(levelsets[i]).getName());
            
            for (int j = 1; j <= 149; j++) {
                loadLevel(j);
                try {
                    twsReader.readSolution(level).load(this);
                    for (int waits = 0; waits < 100 & !level.getChip().isDead(); waits++) {
                        level.tick(WAIT, new int[] {});
                    }
                    if (level.getLayerFG().get(level.getChip().getPosition()) != Tile.EXITED_CHIP) {
                        System.out.println("failed level "+level.levelNumber+" "+new String(level.title));
                    }
                }
                catch (Exception exc) {
                    System.out.println("Error loading "+level.levelNumber+" "+new String(level.title));
                    exc.printStackTrace();
                }
            }
            System.out.println();
            
        }
        
    }
    
    private void runBenchmark(int levelNumber, int runs){
        loadLevel(levelNumber);
        Solution s;
        try {
            s = twsReader.readSolution(level);
            long startTime = System.nanoTime();
            for (int i = 0; i < runs; i++) s.load(this);
            long endTime = System.nanoTime();
            double timePerIteration = (endTime - startTime) / (double) runs;
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
            //emulator.runBenchmark(134, 1);
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
