package graphics;

import emulator.SuperCC;
import game.Level;
import game.Step;
import io.Solution;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.*;

import static java.awt.event.ActionEvent.CTRL_MASK;
import static java.awt.event.KeyEvent.*;

public class MenuBar extends JMenuBar{

    private static final String FILE_PATH = "C:\\Users\\Markus\\Games\\tworld-2.1.0\\sets";

    private SuperCC emulator;
    MainWindow window;

    private class LevelMenu extends JMenu{
        public LevelMenu(){
            super("Level");

            JMenuItem openLevelset = new JMenuItem("Open levelset");
            openLevelset.addActionListener(e -> {
                JFileChooser fc = new JFileChooser();
                fc.setFileFilter(new FileNameExtensionFilter("", "dat"));
                fc.setCurrentDirectory(new File(FILE_PATH));
                if (fc.showOpenDialog(window) == JFileChooser.APPROVE_OPTION) {
                    emulator.openLevelset(fc.getSelectedFile());
                }
            });
            openLevelset.setAccelerator(KeyStroke.getKeyStroke(VK_O, CTRL_MASK));
            add(openLevelset);

            add(new JSeparator());

            JMenuItem previous = new JMenuItem("Previous");
            previous.addActionListener(e ->
                emulator.loadLevel(emulator.getLevel().levelNumber - 1)
            );
            previous.setAccelerator(KeyStroke.getKeyStroke(VK_P, CTRL_MASK));
            add(previous);

            JMenuItem next = new JMenuItem("Next");
            next.addActionListener(e ->{
                emulator.loadLevel(emulator.getLevel().levelNumber + 1);
                try {
                    emulator.playSolution(emulator.twsReader.readSolution(emulator.getLevel()));
                }
                catch (Exception ex){
                    ex.printStackTrace();
                }
                });
            next.setAccelerator(KeyStroke.getKeyStroke(VK_N, CTRL_MASK));
            add(next);

            JMenuItem goTo = new JMenuItem("Go to...");
            goTo.addActionListener(e -> {
                String s = JOptionPane.showInputDialog(window, "Choose a level number");
                if (s == "") return;
                try{
                    int n = Integer.parseInt(s);
                    emulator.loadLevel(n, 0, Step.EVEN);
                } catch (NumberFormatException nfe){
                    JOptionPane.showMessageDialog(window, "Not a number");
                }
            });
            goTo.setAccelerator(KeyStroke.getKeyStroke(VK_G, CTRL_MASK));
            add(goTo);

            add(new JSeparator());

            JMenuItem toggleStep = new JMenuItem("Toogle odd/even step");
            toggleStep.addActionListener(e -> {
                Level oldLevel = emulator.getLevel();
                Step newStep = Step.EVEN;
                if (oldLevel.getStep() == Step.EVEN) newStep = Step.ODD;
                emulator.loadLevel(oldLevel.levelNumber, oldLevel.getRngSeed(), newStep);
            });
            add(toggleStep);

            JMenuItem rngSeed = new JMenuItem("Set RNG Seed");
            rngSeed.addActionListener(e -> {
                String s = JOptionPane.showInputDialog(window, "Choose a starting seed (Will reset the level");
                if (s == "") return;
                try{
                    Level oldLevel = emulator.getLevel();
                    int n = Integer.parseInt(s);
                    emulator.loadLevel(oldLevel.levelNumber, n, oldLevel.getStep());
                }
                catch (NumberFormatException nfe){
                    JOptionPane.showMessageDialog(window, "Not a number");
                }
            });
            add(rngSeed);

        }
    }

    // TODO Everything
    private class SolutionMenu extends JMenu{
        public SolutionMenu(){
            super("Solution");

            JMenuItem save = new JMenuItem("Save");
            save.setAccelerator(KeyStroke.getKeyStroke(VK_S, CTRL_MASK));
            save.addActionListener(event -> {
                Level l = emulator.getLevel();
                Solution solution = new Solution(
                        l.getMoves(), l.getRngSeed(), l.getStep(), Solution.SUCC_MOVES
                );
                try{
                    FileOutputStream fos = new FileOutputStream("test.ser");
                    ObjectOutputStream oos = new ObjectOutputStream(fos);
                    oos.writeObject(solution);
                    fos.close();
                    oos.close();
                }
                catch (IOException e){
                    e.printStackTrace();
                    emulator.throwError("Could not save file");
                }
            });
            add(save);

            JMenuItem load = new JMenuItem("Load");
            load.setAccelerator(KeyStroke.getKeyStroke(VK_O, CTRL_MASK));
            load.addActionListener(event -> {
                try{
                    FileInputStream fis = new FileInputStream("test.ser");
                    ObjectInputStream ois = new ObjectInputStream(fis);
                    Solution solution = (Solution) ois.readObject();
                    emulator.playSolution(solution);
                    ois.close();
                    fis.close();
                }
                catch (IOException e){
                    e.printStackTrace();
                    emulator.throwError("Could not load file");
                } catch (ClassNotFoundException e){}
            });
            add(load);

            JMenuItem copy = new JMenuItem("Copy moves to clipboard");
            copy.setAccelerator(KeyStroke.getKeyStroke(VK_C, CTRL_MASK));
            copy.addActionListener(event -> {
                String moves = new String(emulator.getLevel().getMoves());
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(moves), null);
            });
            add(copy);
        }
    }

    // TODO fromOrdinal new tws file, save a solution
    private class TWSMenu extends JMenu{
        public TWSMenu(){
            super("TWS");

            JMenuItem newTWS = new JMenuItem("Create new tws");
            newTWS.setEnabled(false);
            add(newTWS);

            JMenuItem openTWS = new JMenuItem("Open tws");
            openTWS.addActionListener(e -> {
                JFileChooser fc = new JFileChooser();
                fc.setFileFilter(new FileNameExtensionFilter("", "dat"));
                fc.setCurrentDirectory(new File(FILE_PATH));
                if (fc.showOpenDialog(window) == JFileChooser.APPROVE_OPTION) {
                    emulator.setTWSFile(fc.getSelectedFile());
                }
            });
            add(openTWS);

            add(new JSeparator());

            JMenuItem playSolution = new JMenuItem("Play solution");
            playSolution.addActionListener(event ->{
                try{
                    emulator.playSolution(emulator.twsReader.readSolution(emulator.getLevel()));
                }
                catch (IOException e){
                    emulator.throwError("Error while loading solution");
                }
            });
            add(playSolution);

            JMenuItem saveSolution = new JMenuItem("Save solution");
            saveSolution.setEnabled(false);
            add(saveSolution);

        }
    }

    private class ViewMenu extends JMenu{
        public ViewMenu(){
            super("View");

        }
    }

    public MenuBar(MainWindow window, SuperCC emulator){
        setPreferredSize(new Dimension(0, 24));
        setLocation(0, 0);
        add(new LevelMenu());
        add(new SolutionMenu());
        add(new TWSMenu());
        this.window = window;
        this.emulator = emulator;
    }

}
