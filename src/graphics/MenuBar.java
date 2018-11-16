package graphics;

import emulator.SuperCC;
import game.Level;
import game.Step;
import graphics.popup.ChangeInventory;
import tools.GifSequenceWriter;
import emulator.Solution;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

import static java.awt.event.ActionEvent.CTRL_MASK;
import static java.awt.event.KeyEvent.*;
import static java.nio.charset.StandardCharsets.ISO_8859_1;

public class MenuBar extends JMenuBar{

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
                fc.setCurrentDirectory(new File(emulator.getPaths().getLevelsetPath()));
                if (fc.showOpenDialog(window) == JFileChooser.APPROVE_OPTION) {
                    emulator.getPaths().setLevelsetPath(fc.getSelectedFile().getParent());
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
                    //emulator.twsReader.readSolution(emulator.getLevel()).load(emulator);
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
                if (s.length() == 0) return;
                try {
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
            saveAs.setAccelerator(KeyStroke.getKeyStroke(VK_S, CTRL_MASK + SHIFT_MASK));
            saveAs.addActionListener(event -> {
                Level l = emulator.getLevel();
                Solution solution = new Solution(l.getMoves(), l.getRngSeed(), l.getStep());
                try{
                    JFileChooser fc = new JFileChooser();
                    fc.setFileFilter(new FileNameExtensionFilter("", "json"));
                    fc.setCurrentDirectory(new File(emulator.getJSONPath()));
                    fc.setSelectedFile(new File(emulator.getJSONPath()));
                    if (fc.showOpenDialog(window) == JFileChooser.APPROVE_OPTION) {
                        File file = fc.getSelectedFile();
                        String filename = file.toString();
                        if (!filename .endsWith(".json")) filename += ".json";
                        FileOutputStream fos = new FileOutputStream(filename);
                        fos.write(solution.toString().getBytes());
                        fos.close();
                    }
                }
                catch (IOException e){
                    e.printStackTrace();
                    emulator.throwError("Could not save file: "+e.getMessage());
                }
            });
            addIcon(saveAs, "/resources/icons/saveAs.gif");
            add(saveAs);
    
            JMenuItem save = new JMenuItem("Save");
            save.setAccelerator(KeyStroke.getKeyStroke(VK_S, CTRL_MASK));
            save.addActionListener(event -> {
                Level l = emulator.getLevel();
                Solution solution = new Solution(l.getMoves(), l.getRngSeed(), l.getStep());
                try{
                    FileOutputStream fos = new FileOutputStream(emulator.getJSONPath());
                    fos.write(solution.toString().getBytes(ISO_8859_1));
                    fos.close();
                }
                catch (IOException e){
                    e.printStackTrace();
                    emulator.throwError("Could not save file: "+e.getMessage());
                }
            });
            addIcon(save, "/resources/icons/save.gif");
            add(save);

            JMenuItem open = new JMenuItem("Open");
            open.setAccelerator(KeyStroke.getKeyStroke(VK_O, CTRL_MASK));
            open.addActionListener(event -> {
                try{
                    JFileChooser fc = new JFileChooser();
                    fc.setFileFilter(new FileNameExtensionFilter("", "json"));
                    fc.setCurrentDirectory(new File(emulator.getJSONPath()));
                    fc.setSelectedFile(new File(emulator.getJSONPath()));
                    if (fc.showOpenDialog(window) == JFileChooser.APPROVE_OPTION) {
                        Solution solution = Solution.fromJSON(new String(Files.readAllBytes(fc.getSelectedFile().toPath())));
                        solution.load(emulator);
                    }
                }
                catch (IOException e){
                    e.printStackTrace();
                    emulator.throwError("Could not load file:\n" + e.getMessage());
                }
            });
            addIcon(open, "/resources/icons/open.gif");
            add(open);
            
            addSeparator();
    
            JMenuItem copy = new JMenuItem("Copy solution");
            copy.setAccelerator(KeyStroke.getKeyStroke(VK_C, CTRL_MASK));
            copy.addActionListener(event -> {
                Level level = emulator.getLevel();
                Solution solution = new Solution(level.getMoves(), level.getRngSeed(), level.getStep());
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(solution.toString()), null);
                emulator.showAction("Copied solution");
                emulator.getMainWindow().repaint(emulator.getLevel(), false);
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
                    Solution.fromJSON((String) t.getTransferData(DataFlavor.stringFlavor)).load(emulator);
                    emulator.showAction("Pasted solution");
                    emulator.getMainWindow().repaint(emulator.getLevel(), false);
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
            addIcon(newTWS, "/resources/icons/new.gif");

            JMenuItem openTWS = new JMenuItem("Open tws");
            openTWS.addActionListener(e -> {
                JFileChooser fc = new JFileChooser();
                fc.setFileFilter(new FileNameExtensionFilter("", "tws"));
                fc.setCurrentDirectory(new File(emulator.getPaths().getTwsPath()));
                if (fc.showOpenDialog(window) == JFileChooser.APPROVE_OPTION) {
                    emulator.getPaths().setTwsPath(fc.getSelectedFile().getParent());
                    emulator.setTWSFile(fc.getSelectedFile());
                }
            });
            add(openTWS);
            addIcon(openTWS, "/resources/icons/open.gif");

            add(new JSeparator());

            JMenuItem loadSolution = new JMenuItem("Load solution");
            loadSolution.addActionListener(event -> {
                Thread t = new Thread(() -> {
                    try {
                        emulator.twsReader.readSolution(emulator.getLevel()).load(emulator);
                    } catch (IOException e) {
                        emulator.throwError("Error while loading solution");
                    }
                });
                t.start();
            });
            add(loadSolution);
            addIcon(loadSolution, "/resources/icons/skip.gif");

            JMenuItem saveSolution = new JMenuItem("Save solution");
            saveSolution.setEnabled(false);
            add(saveSolution);
            addIcon(saveSolution, "/resources/icons/save.gif");

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
    
    private class ToolMenu extends JMenu{
        public ToolMenu() {
            super("Tools");
    
            JMenuItem gif = new JMenuItem("Record gif");
            gif.addActionListener(e -> {
                String s = JOptionPane.showInputDialog(window, "Choose gif length");
                if (s.length() == 0) return;
                try {
                    int n = Integer.parseInt(s);
                    ArrayList<BufferedImage> images = new ArrayList<>();
                    emulator.getSavestates().addSavestate(-1);
                    emulator.showAction("Recording gif, please wait");
                    BufferedImage b = new BufferedImage(32 * 20, 32 * 20, BufferedImage.TYPE_4BYTE_ABGR);
                    emulator.getMainWindow().getGamePanel().paintComponent(b.getGraphics());
                    images.add(b);
                    for (int i = 1; i < n && emulator.getSavestates().getNode().hasParent(); i++){
                        emulator.getSavestates().rewind();
                        emulator.getLevel().load(emulator.getSavestates().getSavestate());
                        window.repaint(emulator.getLevel(), false);
                        emulator.getMainWindow().repaint(emulator.getLevel(), false);
                        b = new BufferedImage(32 * 20, 32 * 20, BufferedImage.TYPE_4BYTE_ABGR);
                        emulator.getMainWindow().getGamePanel().paintComponent(b.getGraphics());
                        images.add(b);
                    }
                    System.out.println(images.size());
                    ImageOutputStream output = new FileImageOutputStream(new File("out.gif"));
                    GifSequenceWriter writer = new GifSequenceWriter(output, b.getType(), 100, true);
                    for (int i = images.size() - 1; i >= 0; i--) {
                        writer.writeToSequence(images.get(i));
                    }
    
                    writer.close();
                    output.close();
                    emulator.getSavestates().load(-1, emulator.getLevel());
                    emulator.showAction("Recorded out.gif");
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(window, "Not a number");
                } catch (IOException exc) {
                    exc.printStackTrace();
                }
            });
            addIcon(gif, "/resources/icons/video.gif");
            add(gif);
        }
    }
    
    private class CheatMenu extends JMenu{
        public CheatMenu() {
            super("Cheats");
            
            JMenuItem inventory = new JMenuItem("Change inventory");
            inventory.addActionListener(e -> {
                ChangeInventory c = new ChangeInventory(emulator);
            });
            add(inventory);
        }
    }
    
    public MenuBar(Gui window, SuperCC emulator){
        setPreferredSize(new Dimension(0, 24));
        setLocation(0, 0);
        add(new LevelMenu());
        add(new SolutionMenu());
        add(new TWSMenu());
        add(new ViewMenu());
        add(new ToolMenu());
        add(new CheatMenu());
        this.window = window;
        this.emulator = emulator;
    }

}
