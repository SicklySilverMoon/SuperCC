package graphics;

import emulator.SuperCC;
import game.Level;
import game.Step;
import io.Solution;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.*;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;

import static java.awt.event.ActionEvent.CTRL_MASK;
import static java.awt.event.KeyEvent.*;

public class MenuBar extends JMenuBar{

    private static final String FILE_PATH = "C:\\Users\\Markus\\Games\\tworld-2.1.0\\sets";

    private SuperCC emulator;
    Gui window;
    
    private void addIcon(JMenuItem m, String path){
        try {
            m.setIcon(new ImageIcon(ImageIO.read(getClass().getResource(path))));
        }
        catch (Exception e) {}
    }

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
            addIcon(openLevelset, "/resources/icons/open.gif");
            add(openLevelset);

            add(new JSeparator());

            JMenuItem previous = new JMenuItem("Previous");
            previous.addActionListener(e ->
                emulator.loadLevel(emulator.getLevel().levelNumber - 1)
            );
            previous.setAccelerator(KeyStroke.getKeyStroke(VK_P, CTRL_MASK));
            addIcon(previous, "/resources/icons/left.gif");
            add(previous);

            JMenuItem next = new JMenuItem("Next");
            next.addActionListener(e ->{
                emulator.loadLevel(emulator.getLevel().levelNumber + 1);
                try {
                    //emulator.twsReader.readSolution(emulator.getLevel()).play(emulator);
                }
                catch (Exception ex){
                    ex.printStackTrace();
                }
                });
            next.setAccelerator(KeyStroke.getKeyStroke(VK_N, CTRL_MASK));
            addIcon(next, "/resources/icons/right.gif");
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
            addIcon(goTo, "/resources/icons/goto.gif");
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
            addIcon(rngSeed, "/resources/icons/rng.gif");
            add(rngSeed);

        }
    }
    
    private class SolutionMenu extends JMenu{
        public SolutionMenu(){
            super("Solution");
    
            JMenuItem saveAs = new JMenuItem("Save as");
            saveAs.addActionListener(event -> {
                Level l = emulator.getLevel();
                Solution solution = new Solution(l.getMoves(), l.getRngSeed(), l.getStep());
                try{
                    JFileChooser fc = new JFileChooser();
                    fc.setFileFilter(new FileNameExtensionFilter("", "sol"));
                    fc.setCurrentDirectory(new File("."));
                    if (fc.showOpenDialog(window) == JFileChooser.APPROVE_OPTION) {
                        FileOutputStream fos = new FileOutputStream(fc.getSelectedFile());
                        fos.write(solution.toString().getBytes());
                        fos.close();
                    }
                }
                catch (IOException e){
                    e.printStackTrace();
                    emulator.throwError("Could not save file");
                }
            });
            addIcon(saveAs, "/resources/icons/saveAs.gif");
            add(saveAs);
    
            JMenuItem save = new JMenuItem("Save");
            saveAs.setAccelerator(KeyStroke.getKeyStroke(VK_S, CTRL_MASK));
            addIcon(save, "/resources/icons/save.gif");
            add(save);
            save.setEnabled(false);

            JMenuItem open = new JMenuItem("Open");
            open.setAccelerator(KeyStroke.getKeyStroke(VK_O, CTRL_MASK));
            open.addActionListener(event -> {
                /*
                try{
                    JFileChooser fc = new JFileChooser();
                    fc.setFileFilter(new FileNameExtensionFilter("", "sol"));
                    fc.setCurrentDirectory(new File("."));
                    if (fc.showOpenDialog(window) == JFileChooser.APPROVE_OPTION) {
                        FileInputStream fis = new FileInputStream(fc.getSelectedFile());
                        Solution solution = new Solution(new String(fis.readAllBytes()));
                        solution.play(emulator);
                        fis.close();
                    }
                }
                catch (IOException e){
                    e.printStackTrace();
                    emulator.throwError("Could not load file:\n" + e.getMessage());
                }
                */
            });
            addIcon(open, "/resources/icons/open.gif");
            add(open);
            open.setEnabled(false);
    
            JMenuItem copy = new JMenuItem("Copy solution");
            copy.setAccelerator(KeyStroke.getKeyStroke(VK_C, CTRL_MASK));
            copy.addActionListener(event -> {
                Level level = emulator.getLevel();
                Solution solution = new Solution(level.getMoves(), level.getRngSeed(), level.getStep());
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(solution.toString()), null);
            });
            addIcon(copy, "/resources/icons/copy.gif");
            add(copy);
    
            JMenuItem paste = new JMenuItem("Paste solution");
            paste.setAccelerator(KeyStroke.getKeyStroke(VK_V, CTRL_MASK));
            paste.addActionListener(event -> {
                Level level = emulator.getLevel();
                Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(this);
                Solution s;
                try {
                    new Solution((String) t.getTransferData(DataFlavor.stringFlavor)).play(emulator);
                }
                catch (IllegalArgumentException e){
                    emulator.throwError(e.getMessage());
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            });
            addIcon(paste, "/resources/icons/paste.gif");
            add(paste);
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
            playSolution.addActionListener(event -> {
                Thread t = new Thread(() -> {
                    try {
                        emulator.twsReader.readSolution(emulator.getLevel()).play(emulator, 10);
                    } catch (IOException e) {
                        emulator.throwError("Error while loading solution");
                    }
                });
                t.start();
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
    
            JMenu tileset = new JMenu("Tileset");
            ButtonGroup allTilesets = new ButtonGroup();
            String[] tilesetNames = new String[] {"Tile World (Editor)", "Tile World", "MSCC (Editor)", "MSCC"};
            String[] tilesetPaths = new String[] {"/resources/tw-editor.png", "/resources/tw.png",
                "/resources/mscc-editor.png", "/resources/mscc.png"};
            for (int i = 0; i < tilesetNames.length; i++) {
                JRadioButton msccEditor = new JRadioButton(tilesetNames[i]);
                String tilesetPath = tilesetPaths[i];
                msccEditor.addActionListener(e -> {
                    try {
                        window.getGamePanel().initialiseTileGraphics(ImageIO.read(getClass().getResource(tilesetPath)));
                        window.getInventoryPanel().initialise(emulator);
                        window.repaint(emulator.getLevel(), true);
                    } catch (IOException exc) {}
                });
                allTilesets.add(msccEditor);
                tileset.add(msccEditor);
            }
            add(tileset);
    
            String[] setterNames = new String[] {
                "Show Monster List",
                "Show Slip List",
                "Show Clone connections",
                "Show Trap Connections",
                "Show Move History"
            };
            
            List<Consumer<Boolean>> setters = Arrays.asList(
                b -> window.getGamePanel().setMonsterListVisible(b),
                b -> window.getGamePanel().setSlipListVisible(b),
                b -> window.getGamePanel().setClonesVisible(b),
                b -> window.getGamePanel().setTrapsVisible(b),
                b -> window.getGamePanel().setHistoryVisible(b)
            );
            
            for (int i = 0; i < setterNames.length; i++){
                JToggleButton b = new JToggleButton(setterNames[i]);
                Consumer<Boolean> setter = setters.get(i);
                b.addActionListener(e -> {
                    setter.accept(((AbstractButton) e.getSource()).isSelected());
                    window.repaint(emulator.getLevel(), true);
                });
                add(b);
            }

        }
    }

    public MenuBar(Gui window, SuperCC emulator){
        setPreferredSize(new Dimension(0, 24));
        setLocation(0, 0);
        add(new LevelMenu());
        add(new SolutionMenu());
        add(new TWSMenu());
        add(new ViewMenu());
        this.window = window;
        this.emulator = emulator;
    }

}
