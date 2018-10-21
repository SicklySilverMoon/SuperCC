package emulator;

import game.Creature;
import game.Step;
import graphics.MainWindow;
import io.DatParser;
import io.Solution;
import io.TWSReader;
import savestateTree.Tree;
import game.Level;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;

public class SuperCC implements KeyListener{

    private final int[] MOVEMENT_KEYS = {KeyEvent.VK_UP, KeyEvent.VK_RIGHT, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT,
            KeyEvent.VK_SPACE};
    private final char[] CHAR_MOVEMENT_KEYS = {'u', 'r', 'd', 'l', '-'};
    private final int[] DIRECTIONS = {Creature.DIRECTION_UP, Creature.DIRECTION_RIGHT, Creature.DIRECTION_DOWN,
            Creature.DIRECTION_LEFT, -1};

    private boolean shiftPressed = false;

    private Tree savestates;
    private Level level;
    private MainWindow window;
    private DatParser dat;
    public TWSReader twsReader;

    public void setTWSFile(File twsFile){
        try{
            this.twsReader = new TWSReader(twsFile);
        }
        catch (IOException e){
            JOptionPane.showMessageDialog(window, "Could not read file:\n"+e.getLocalizedMessage());
        }
    }

    public Level getLevel(){
        return level;
    }

    public SuperCC() throws IOException {
        this.window = new MainWindow(this);
    }

    public void openLevelset(File levelset){
        try{
            dat = new DatParser(levelset);
        }
        catch (IOException e){
            JOptionPane.showMessageDialog(window, "Could not read file:\n"+e.getLocalizedMessage());
        }
        loadLevel(1);
    }

    public synchronized void loadLevel(int levelNumber, int rngSeed, Step step){
        try{
            level = dat.parseLevel(levelNumber, rngSeed, step);
            savestates = new Tree(level);
            window.repaint(level, true);
        }
        catch (IOException e){
            JOptionPane.showMessageDialog(window, "Could not load level");
        }
    }

    public synchronized void loadLevel(int levelNumber){
        loadLevel(levelNumber, 0, Step.EVEN);
    }

    private synchronized boolean tick(char c, int direction){
        if (level == null) return false;
        boolean tickedTwice = level.tick(c, direction, -1);
        window.repaint(level, false);
        savestates.addSaveState(level.save());
        return tickedTwice;
    }
    
    public synchronized boolean tick(int mouseClick){
        if (level == null) return false;
        boolean tickedTwice = level.tick('?', -1, mouseClick);
        window.repaint(level, false);
        savestates.addSaveState(level.save());
        return tickedTwice;
    }

    public void playSolution(Solution solution){
        try{
            loadLevel(level.levelNumber, solution.rngSeed, solution.step);
            for (int move = 0; move < solution.halfMoves.length; move++){
                byte m = solution.halfMoves[move];
                int i;
                for (i = 0; i < 5; i++) if (CHAR_MOVEMENT_KEYS[i] == m) break;
                boolean tickedTwice = tick((char) m, DIRECTIONS[i]);
                if (tickedTwice) move++;
                if (level.getChip().isDead()){
                    break;
                }
            }
            while (level.getChip().isSliding()){
                tick('-', -1);
                if (level.getChip().isDead()){
                    break;
                }
            }
        }
        catch (Exception e){
            throwError("Something went wrong:\n"+e.getMessage());
        }
        window.repaint(level, true);
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
                tick(CHAR_MOVEMENT_KEYS[i], DIRECTIONS[i]);
                return;
            }
        }

        if (key == KeyEvent.VK_BACK_SPACE) {
            level.load(savestates.rewind());
            window.repaint(level, false);
            return;
        }

        if (shiftPressed) savestates.addSaveState(level.save(), key);
        else {
            level.load(savestates.load(key));
            window.repaint(level, false);
        }

    }

    public static void initialise(){
        try{
            SuperCC emulator = new SuperCC();
            //emulator.openLevelset(new File("C:\\Users\\Markus\\Downloads\\CCTools\\tworld-2.2.0\\data\\CCLP1.dat"));
            emulator.openLevelset(new File("C:\\Users\\Markus\\Downloads\\CCTools\\tworld-2.2.0\\data\\CHIPS.dat"));
            emulator.setTWSFile(new File("C:\\Users\\Markus\\Downloads\\CCTools\\tworld-2.2.0\\save\\public_CHIPS.dac.tws"));
            //emulator.setTWSFile(new File("C:\\Users\\Markus\\Downloads\\CCTools\\tworld-2.2.0\\save\\public_CCLP1.dac.tws"));
        }
        catch (IOException e){
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void throwError(String s){
        JOptionPane.showMessageDialog(window, s, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) throws IOException{
        SwingUtilities.invokeLater(() -> initialise());
    }

}
