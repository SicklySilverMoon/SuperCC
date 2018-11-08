package emulator;

import game.Creature;
import game.Level;
import game.Step;
import graphics.Gui;
import game.Position;
import io.DatParser;
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
    
    public void repaint(boolean fromScratch) {
        window.repaint(level, fromScratch);
    }
    
    private static byte capital(byte b){
        if (b == '-') return '_';
        return (byte) Character.toUpperCase((char) b);
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
        this.window = new Gui(this);
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
            throwError("Could not load level: "+e.getMessage());
        }
    }

    public synchronized void loadLevel(int levelNumber){
        loadLevel(levelNumber, 0, Step.EVEN);
    }

    public boolean tick(byte b, int[] directions, boolean repaint){
        if (level == null) return false;
        boolean tickTwice = level.tick(b, directions);
        if (tickTwice) {
            b = capital(b);
            level.tick(b, DIRECTIONS[4]);
        }
        savestates.addRewindState(level, b);
        if (repaint) window.repaint(level, false);
        return tickTwice;
    }
    
    public boolean isClick(byte b){
        return b <= 0;
    }
    
    public boolean tick(byte b, boolean repaint){
        if (level == null) return false;
        int[] directions;
        if (isClick(b)){
            Position screenPosition = Position.screenPosition(level.getChip().getPosition());
            Position clickedPosition = Position.clickPosition(screenPosition, b);
            directions = level.getChip().getPosition().seek(clickedPosition);
            level.setClick(clickedPosition.getIndex());
            return tick(b, directions, repaint);
        }
        else{
            for (int i = 0; i < BYTE_MOVEMENT_KEYS.length; i++) {
                if (BYTE_MOVEMENT_KEYS[i] == b) {
                    directions = DIRECTIONS[i];
                    return tick(b, directions, repaint);
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
                tick(BYTE_MOVEMENT_KEYS[i], DIRECTIONS[i], true);
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
    
    private void runBenchmark(int levelNumber, int runs){
        loadLevel(levelNumber);
        Solution s;
        try {
            s = twsReader.readSolution(level);
            long startTime = System.nanoTime();
            for (int i = 0; i < runs; i++) s.play(this, true);
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
