package graphics;

import emulator.SavestateManager;
import emulator.Solution;
import emulator.SuperCC;
import emulator.TickFlags;
import game.Level;
import game.Position;
import game.Step;
import io.TWSWriter;
import tools.*;
import util.ByteList;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;

import static java.awt.event.ActionEvent.CTRL_MASK;
import static java.awt.event.KeyEvent.*;
import static java.nio.charset.StandardCharsets.ISO_8859_1;

class MenuBar extends JMenuBar{

    private SuperCC emulator;
    private Gui window;

//    private JPanel aboutPanel;
//    private JLabel aboutLabel;
    
    private void addIcon(JMenuItem m, String path){
        try {
            m.setIcon(new ImageIcon(ImageIO.read(getClass().getResource(path))));
        }
        catch (Exception e) {}
    }

    private class LevelMenu extends JMenu{
        LevelMenu(){
            super("Level");

            JMenuItem openLevelset = new JMenuItem("Open levelset");
            openLevelset.addActionListener(e -> {
                if (!SuperCC.areToolsRunning()) {
                    JFileChooser fc = new JFileChooser();
                    fc.setFileFilter(new FileNameExtensionFilter("dat, ccl", "dat", "ccl"));
                    fc.setCurrentDirectory(new File(emulator.getPaths().getLevelsetPath()));
                    if (fc.showOpenDialog(window) == JFileChooser.APPROVE_OPTION) {
                        emulator.getPaths().setLevelsetPath(fc.getSelectedFile().getParent());
                        emulator.openLevelset(fc.getSelectedFile());
                    }
                }
            });
            openLevelset.setAccelerator(KeyStroke.getKeyStroke(VK_O, CTRL_MASK + SHIFT_MASK));
            addIcon(openLevelset, "/resources/icons/open.gif");
            add(openLevelset);

            add(new JSeparator());
    
            JMenuItem restart = new JMenuItem("Restart");
            restart.addActionListener(e -> {
                if (!SuperCC.areToolsRunning()) {
                    emulator.getSavestates().restart();
                    emulator.getLevel().load(emulator.getSavestates().getSavestate());
                    emulator.showAction("Restarted Level");
                    window.repaint(false);
                }
            });
            restart.setAccelerator(KeyStroke.getKeyStroke(VK_R, CTRL_MASK));
            addIcon(restart, "/resources/icons/restart.gif");
            add(restart);
    
            JMenuItem next = new JMenuItem("Next");
            next.addActionListener(e -> {
                if (!SuperCC.areToolsRunning()) emulator.loadLevel(emulator.getLevel().getLevelNumber() + 1);
            });
            next.setAccelerator(KeyStroke.getKeyStroke(VK_N, CTRL_MASK));
            addIcon(next, "/resources/icons/right.gif");
            add(next);

            JMenuItem previous = new JMenuItem("Previous");
            previous.addActionListener(e -> {
                if (!SuperCC.areToolsRunning()) emulator.loadLevel(emulator.getLevel().getLevelNumber() - 1);
            });
            previous.setAccelerator(KeyStroke.getKeyStroke(VK_P, CTRL_MASK));
            addIcon(previous, "/resources/icons/left.gif");
            add(previous);

            JMenuItem goTo = new JMenuItem("Go to...");
            goTo.addActionListener(e -> {
                if (!SuperCC.areToolsRunning()) {
                    String s = JOptionPane.showInputDialog(window, "Choose a level number");
                    if (s.length() == 0) return;
                    try {
                        int n = Integer.parseInt(s);
                        emulator.loadLevel(n, 0, Step.EVEN, true);
                    } catch (NumberFormatException nfe) {
                        JOptionPane.showMessageDialog(window, "Please Enter a Whole Number");
                    }
                }
            });
            goTo.setAccelerator(KeyStroke.getKeyStroke(VK_G, CTRL_MASK));
            addIcon(goTo, "/resources/icons/goto.gif");
            add(goTo);

            add(new JSeparator());

            JMenuItem toggleStep = new JMenuItem("Toggle odd/even step");
            toggleStep.addActionListener(e -> {
                if (!SuperCC.areToolsRunning()) {
                    Level oldLevel = emulator.getLevel();
                    Step newStep = Step.EVEN;
                    if (oldLevel.getStep() == Step.EVEN) newStep = Step.ODD;
                    emulator.loadLevel(oldLevel.getLevelNumber(), oldLevel.getRngSeed(), newStep, true);
                }
            });
            add(toggleStep);

            JMenuItem rngSeed = new JMenuItem("Set RNG Seed");
            rngSeed.addActionListener(e -> {
                if (!SuperCC.areToolsRunning()) {
                    String s = JOptionPane.showInputDialog(window, "Choose a starting seed");
                    if (s.equals("")) return;
                    try {
                        Level oldLevel = emulator.getLevel();
                        int n = Integer.parseInt(s);
                        emulator.loadLevel(oldLevel.getLevelNumber(), n, oldLevel.getStep(), true);
                    } catch (NumberFormatException nfe) {
                        JOptionPane.showMessageDialog(window, "Please Enter a Whole Number");
                    }
                }
            });
            addIcon(rngSeed, "/resources/icons/rng.gif");
            add(rngSeed);

        }
    }
    
    private class SolutionMenu extends JMenu{
        SolutionMenu(){
            super("Solution");

            JMenuItem saveAs = new JMenuItem("Save as"); //TODO: make saving a solution or opening from a new file adjust sccpath so things actually change and you don't have to go get the new solution each time
            saveAs.setAccelerator(KeyStroke.getKeyStroke(VK_S, CTRL_MASK + SHIFT_MASK));
            saveAs.addActionListener(event -> {
                Level l = emulator.getLevel();
                Solution solution = new Solution(emulator.getSavestates().getMoveList(), l.getRngSeed(), l.getStep());
                try{
                    JFileChooser fc = new JFileChooser();
                    fc.setFileFilter(new FileNameExtensionFilter("json", "json"));
                    fc.setCurrentDirectory(new File(emulator.getJSONPath()));
                    fc.setSelectedFile(new File(emulator.getJSONPath()));
                    if (fc.showSaveDialog(window) == JFileChooser.APPROVE_OPTION) {
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
                Solution solution = new Solution(emulator.getSavestates().getMoveList(), l.getRngSeed(), l.getStep());
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
                    fc.setFileFilter(new FileNameExtensionFilter("json", "json"));
                    fc.setCurrentDirectory(new File(emulator.getJSONPath()).getParentFile());
                    fc.setSelectedFile(new File(emulator.getJSONPath()));
                    if (fc.showOpenDialog(window) == JFileChooser.APPROVE_OPTION) {
                        Solution solution = Solution.fromJSON(new String(Files.readAllBytes(fc.getSelectedFile().toPath()), StandardCharsets.ISO_8859_1));
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
            
            JMenuItem seedSearch = new JMenuItem("Search for seeds");
            seedSearch.addActionListener(event -> {
                try{
                    JFileChooser fc = new JFileChooser();
                    fc.setFileFilter(new FileNameExtensionFilter("json", "json"));
                    fc.setCurrentDirectory(new File(emulator.getJSONPath()).getParentFile());
                    fc.setSelectedFile(new File(emulator.getJSONPath()));
                    if (fc.showOpenDialog(window) == JFileChooser.APPROVE_OPTION) {
                        Solution solution = Solution.fromJSON(new String(Files.readAllBytes(fc.getSelectedFile().toPath()), StandardCharsets.ISO_8859_1));
                        new SeedSearch(emulator, solution);
                    }
                }
                catch (IOException e){
                    e.printStackTrace();
                    emulator.throwError("Could not load file:\n" + e.getMessage());
                }
            });
            addIcon(seedSearch, "/resources/icons/open.gif");
            add(seedSearch);
            
            addSeparator();
    
            JMenuItem copy = new JMenuItem("Copy solution");
            copy.setAccelerator(KeyStroke.getKeyStroke(VK_C, CTRL_MASK));
            copy.addActionListener(event -> {
                Level level = emulator.getLevel();
                Solution solution = new Solution(emulator.getSavestates().getMoveList(), level.getRngSeed(), level.getStep());
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(solution.toString()), null);
                emulator.showAction("Copied solution");
                emulator.getMainWindow().repaint(false);
            });
            addIcon(copy, "/resources/icons/copy.gif");
            add(copy);
    
            JMenuItem paste = new JMenuItem("Paste solution");
            paste.setAccelerator(KeyStroke.getKeyStroke(VK_V, CTRL_MASK));
            paste.addActionListener(event -> {
                Level level = emulator.getLevel();
                Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(this);
                //Solution s = emulator.getSolution();
                try {
                    Solution.fromJSON((String) t.getTransferData(DataFlavor.stringFlavor)).load(emulator);
                    emulator.showAction("Pasted solution");
                    emulator.getMainWindow().repaint(false);
                }
                catch (IllegalArgumentException e){ //If the clipboard isn't an entire JSON solution it might be raw moves, which should be put in
                    try {
                        for (char ch : (t.getTransferData(DataFlavor.stringFlavor)).toString().toCharArray()) {
                            char[] lowerCaseArray = SuperCC.lowerCaseChar(ch);
                            byte b = (byte) lowerCaseArray[0];
                            switch (b){
                                case 85: b = SuperCC.UP; break;
                                case 76: b = SuperCC.LEFT; break;
                                case 68: b = SuperCC.DOWN; break;
                                case 82: b = SuperCC.RIGHT; break;
                                case 45: b = SuperCC.WAIT; break;
                            }
                            emulator.tick(b, TickFlags.GAME_PLAY);
                            if (level.getChip().isDead()) break;
                        }
                        emulator.showAction("Pasted moves");
                    }
                    catch (UnsupportedFlavorException | IOException e2) {
                        emulator.throwError(e2.getMessage());
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            });
            addIcon(paste, "/resources/icons/paste.gif");
            add(paste);
    
            addSeparator();
    
            JMenuItem saveSavestates = new JMenuItem("Save all states to disk");
            saveSavestates.addActionListener(event -> {
                Level l = emulator.getLevel();
                try{
                    JFileChooser fc = new JFileChooser();
                    fc.setFileFilter(new FileNameExtensionFilter("ser", "ser"));
                    fc.setCurrentDirectory(new File(emulator.getSerPath()).getParentFile());
                    fc.setSelectedFile(new File(emulator.getSerPath()));
                    if (fc.showSaveDialog(window) == JFileChooser.APPROVE_OPTION) {
                        File file = fc.getSelectedFile();
                        String filename = file.toString();
                        if (!filename .endsWith(".ser")) filename += ".ser";
                        FileOutputStream fos = new FileOutputStream(filename);
                        ObjectOutputStream out = new ObjectOutputStream(fos);
                        out.writeObject(emulator.getSavestates());
                        out.close();
                        fos.close();
                    }
                }
                catch (IOException e){
                    e.printStackTrace();
                    emulator.throwError("Could not save file: "+e.getMessage());
                }
            });
            addIcon(saveSavestates, "/resources/icons/saveAs.gif");
            add(saveSavestates);
    
            JMenuItem loadStates = new JMenuItem("Load states from disk");
            loadStates.addActionListener(event -> {
                try{
                    JFileChooser fc = new JFileChooser();
                    fc.setFileFilter(new FileNameExtensionFilter("ser", "ser"));
                    fc.setCurrentDirectory(new File(emulator.getSerPath()).getParentFile());
                    fc.setSelectedFile(new File(emulator.getSerPath()));
                    if (fc.showOpenDialog(window) == JFileChooser.APPROVE_OPTION) {
                        FileInputStream fis = new FileInputStream(fc.getSelectedFile());
                        ObjectInputStream ois = new ObjectInputStream(fis);
                        emulator.setSavestates((SavestateManager) ois.readObject());
                        ois.close();
                        fis.close();
                    }
                }
                catch (IOException | ClassNotFoundException e){
                    e.printStackTrace();
                }
            });
            addIcon(loadStates, "/resources/icons/open.gif");
            add(loadStates);
            
        }
    }

    private class TWSMenu extends JMenu{
        TWSMenu(){
            super("TWS");

            JMenuItem newTWS = new JMenuItem("Write solution to new tws");
            newTWS.addActionListener(event -> {
                JFileChooser fc = new JFileChooser();
                fc.setFileFilter(new FileNameExtensionFilter("tws", "tws"));
                fc.setCurrentDirectory(new File(emulator.getPaths().getTwsPath()));
                fc.setSelectedFile(new File(emulator.getPaths().getTwsPath()));
                if (fc.showSaveDialog(window) == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    String filename = file.toString();
                    if (!filename.endsWith(".tws")) filename += ".tws";
                    file = new File(filename);
                    SavestateManager savestates = emulator.getSavestates();
                    Level level = emulator.getLevel();
                    Solution solution = new Solution(savestates.getMoveList(),
                            level.getRngSeed(),
                            level.getStep());

                    ByteList twsMouseMovesX = new ByteList(); //This probably isn't the best place for this, however considering it used to be in SavestateManager, its a hell of a lot better here
                    ByteList twsMouseMovesY = new ByteList();
                    ByteList twsMouseMoves = new ByteList();
                    savestates.addSavestate(-1); //Ideally a savestate should never be possible to be saved to this key
                    ListIterator<Byte> itr = savestates.getMoveList().listIterator(true);
                    while (itr.hasPrevious()) {
                        byte b = itr.previous();
                        if (SuperCC.isClick(b)) { //Use to math out the relative click position for TWS writing with mouse moves
                            Position screenPosition = Position.screenPosition(level.getChip().getPosition());
                            Position clickedPosition = Position.clickPosition(screenPosition, b);
                            int relativeClickX = clickedPosition.getX() - level.getChip().getPosition().getX(); //down and to the right of Chip are positive, this just quickly gets the relative position following that
                            int relativeClickY = clickedPosition.getY() - level.getChip().getPosition().getY();

                            twsMouseMovesX.add(relativeClickX);
                            twsMouseMovesY.add(relativeClickY);
                        }
                        savestates.rewind();
                    }
                    twsMouseMovesX.reverse(); //We were rewind through the entire solution so now we have to reverse the resulting list
                    twsMouseMovesY.reverse();
                    for (int i = 0; i < twsMouseMovesX.size(); i++){
                        twsMouseMoves.add(twsMouseMovesX.get(i));
                        twsMouseMoves.add(twsMouseMovesY.get(i));
                    }

                    savestates.load(-1, level);

                    TWSWriter.write(file, level, solution, twsMouseMoves);
                }
            });
            add(newTWS);
            addIcon(newTWS, "/resources/icons/new.gif");

            JMenuItem openTWS = new JMenuItem("Open tws");
            openTWS.addActionListener(e -> {
                JFileChooser fc = new JFileChooser();
                fc.setFileFilter(new FileNameExtensionFilter("tws", "tws"));
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
                //Thread t = new Thread(() -> {
                    try {
                        emulator.twsReader.readSolution(emulator.getLevel()).load(emulator);
                    } catch (IOException e) {
                        emulator.throwError("Error while loading solution");
                    }
                //});
                //t.start();
            });
            add(loadSolution);
            addIcon(loadSolution, "/resources/icons/skip.gif");

            JMenuItem saveSolution = new JMenuItem("Save solution");
            saveSolution.setEnabled(false);
            add(saveSolution);
            addIcon(saveSolution, "/resources/icons/save.gif");

            JMenuItem verify = new JMenuItem("Verify tws");
            verify.addActionListener(e -> {
                new VerifyTWS(emulator);
            });
            add(verify);

        }
    }

    private class ViewMenu extends JMenu{
        ViewMenu(){
            super("View");
    
            JMenu tileset = new JMenu("Tileset");
            ButtonGroup allTilesets = new ButtonGroup();
            String[] tilesetNames = TileSheet.getNames();
            TileSheet[] tileSheets = TileSheet.values();
            for (int i = 0; i < tilesetNames.length; i++) {
                JRadioButton msccEditor = new JRadioButton(tilesetNames[i]);
                TileSheet tileSheet = tileSheets[i];
                msccEditor.addActionListener(e -> {
                    int tileWidth = 0, tileHeight = 0;
                    try {
                        tileWidth = emulator.getMainWindow().getGamePanel().getTileWidth();
                        tileHeight = emulator.getMainWindow().getGamePanel().getTileHeight();
                    }
                    catch (NullPointerException exc) {
                        tileWidth = Gui.DEFAULT_TILE_WIDTH;
                        tileHeight = Gui.DEFAULT_TILE_HEIGHT;
                    }
                    try {
                        BufferedImage tilesetImage = tileSheet.getTileSheet(tileWidth, tileHeight);
                        window.getGamePanel().initialise(emulator, tilesetImage, tileSheet,
                                window.getGamePanel().getTileWidth(), window.getGamePanel().getTileHeight());
                        window.getInventoryPanel().initialise(emulator);
                        window.repaint(true);

                        emulator.getPaths().setTilesetNum(tileSheet.ordinal());
                    } catch (IOException exc) {
                        emulator.throwError(exc.getMessage());
                    }
                });
                allTilesets.add(msccEditor);
                tileset.add(msccEditor);
            }
            add(tileset);
    
            JMenu tileSize = new JMenu("Tile size");
            ButtonGroup tileSizes = new ButtonGroup();
            int[] sizes = new int[] {16, 20, 24, 32};
            for (int i = 0; i < sizes.length; i++) {
                int size = sizes[i];
                JRadioButton sizeButton = new JRadioButton(size + "x" + size);
                sizeButton.addActionListener((e) -> {
                    try {
                        TileSheet ts;
                        try {
                            ts = emulator.getMainWindow().getGamePanel().getTileSheet();
                        }
                        catch (NullPointerException npe) {
                            ts = Gui.DEFAULT_TILESHEET;
                        }
                        SmallGamePanel gamePanel = (SmallGamePanel) emulator.getMainWindow().getGamePanel();
                        emulator.getMainWindow().getGamePanel().initialise(emulator, ts.getTileSheet(size, size), ts, size, size);
                        window.getInventoryPanel().initialise(emulator);
                        window.getInventoryPanel().setPreferredSize(new Dimension(4*size+10, 2*size+10));
                        window.getInventoryPanel().setSize(4*size+10, 2*size+10);
                        window.setSize(200+size*gamePanel.getWindowSizeX(), 200+size*gamePanel.getWindowSizeY());
                        window.getGamePanel().setPreferredSize(new Dimension(size*gamePanel.getWindowSizeX(), size*gamePanel.getWindowSizeY()));
                        window.getGamePanel().setSize(size*gamePanel.getWindowSizeX(), size*gamePanel.getWindowSizeY());
                        window.pack();
                        window.repaint(true);

                        emulator.getPaths().setTileSizes(new int[]{size, size});
                    }
                    catch (IOException e1) {
                        emulator.throwError(e1.getMessage());
                    }
                });
                tileSizes.add(sizeButton);
                tileSize.add(sizeButton);
            }
            JRadioButton sizeButton = new JRadioButton("custom");
            sizeButton.addActionListener(e -> new ChooseTileSize(emulator, window.getGamePanel().getTileWidth(), 0, 256));
            tileSizes.add(sizeButton);
            tileSize.add(sizeButton);
            add(tileSize);
            
            JMenu windowSize = new JMenu("Game window size");
            ButtonGroup windowSizes = new ButtonGroup();
            sizes = new int[] {9, 32};
            for (int i = 0; i < sizes.length; i++) {
                int size = sizes[i];
                sizeButton = new JRadioButton(size + "x" + size);
                sizeButton.addActionListener((e) -> {
                    SmallGamePanel gamePanel = (SmallGamePanel) emulator.getMainWindow().getGamePanel();
                    gamePanel.setWindowSize(size, size);
                    int tileWidth = gamePanel.getTileWidth(), tileHeight = gamePanel.getTileHeight();
                    window.setSize(200+tileWidth*size, 200+tileHeight*size);
                    window.getGamePanel().setPreferredSize(new Dimension(tileWidth*size, tileHeight*size));
                    window.getGamePanel().setSize(tileWidth*size, tileHeight*size);
                    window.pack();
                    window.repaint(true);
                });
                windowSizes.add(sizeButton);
                windowSize.add(sizeButton);
            }
            sizeButton = new JRadioButton("custom");
            sizeButton.addActionListener(e -> new ChooseWindowSize(emulator));
            windowSizes.add(sizeButton);
            windowSize.add(sizeButton);
            add(windowSize);
            
            add(new JSeparator());
    
            String[] setterNames = new String[] {
                "Show Background Tiles",
                "Show Monster List",
                "Show Slip List",
                "Show Clone connections",
                "Show Trap Connections",
                "Show Move History"
            };
            
            List<Consumer<Boolean>> setters = Arrays.asList(
                b -> window.getGamePanel().setBGVisible(b),
                b -> window.getGamePanel().setMonsterListVisible(b),
                b -> window.getGamePanel().setSlipListVisible(b),
                b -> window.getGamePanel().setClonesVisible(b),
                b -> window.getGamePanel().setTrapsVisible(b),
                b -> window.getGamePanel().setHistoryVisible(b)
            );
            
            for (int i = 0; i < setterNames.length; i++){
                JToggleButton b = new JToggleButton(setterNames[i]);
                Consumer<Boolean> setter = setters.get(i);
                if (i == 0 || i == 1 || i == 2) b.setSelected(true); //These are already selected by default in the backend (GamePanel.java, at the top with all the variable declarations) so i just make the buttons default the on state
                b.addActionListener(e -> {
                    setter.accept(((AbstractButton) e.getSource()).isSelected());
                    window.repaint(true);
                });
                add(b);
            }

            add(new JSeparator());

            String[] setterHUDNames = new String[] { //Should probably rename this sometime
                    "Switch Decimal Notation",
            };

            List<Consumer<Boolean>> HUDSetters = Arrays.asList( //Yes yes this and the next are stupid as its only one value, but this also future proofs it
                    b -> window.getLevelPanel().changeNotation(b)
            );

            for (int i = 0; i < setterHUDNames.length; i++){
                JToggleButton b = new JToggleButton(setterHUDNames[i]);
                Consumer<Boolean> setter = HUDSetters.get(i);
                b.addActionListener(e -> {
                    setter.accept(((AbstractButton) e.getSource()).isSelected());
                    window.repaint(true);
                });
                add(b);
            }
        }
    }
    
    private class ToolMenu extends JMenu{
        ToolMenu() {
            super("Tools");
    
            JMenuItem controls = new JMenuItem("Controls");
            controls.addActionListener(e -> new SwingWorker<Void, Void>(){
                @Override
                protected Void doInBackground() throws Exception {
                    new ControlGUI(emulator);
                    return null;
                }
            }.execute()) ;
            add(controls);
    
            JMenuItem gif = new JMenuItem("Record gif");
            gif.addActionListener(e -> {
                GameGifRecorder c = new GameGifRecorder(emulator);
            });
            addIcon(gif, "/resources/icons/video.gif");
            add(gif);

            JMenuItem variations = new JMenuItem("Variation testing");
            variations.addActionListener(e -> {
                VariationTesting v = new VariationTesting(emulator);
            });
            add(variations);

            JMenuItem tsp = new JMenuItem("TSP Solver");
            tsp.addActionListener(e -> {
                TSPGUI t = new TSPGUI(emulator);
            });
            add(tsp);

        }
    }
    
    private class CheatMenu extends JMenu{
        CheatMenu() {
            super("Cheats");
    
            JMenuItem inventory = new JMenuItem("Change inventory");
            inventory.addActionListener(e -> new ChangeInventory(emulator));
            addIcon(inventory, "/resources/icons/green_key.gif");
            add(inventory);
    
            JMenuItem time = new JMenuItem("Change timer");
            time.addActionListener(e -> new ChangeTimer(emulator));
            add(time);
            
            add(new JSeparator());
    
            JMenuItem toggle = new JMenuItem("Press Green Button");
            toggle.addActionListener(e -> {
                emulator.getLevel().getCheats().pressGreenButton();
                emulator.getMainWindow().repaint(false);
            });
            addIcon(toggle, "/resources/icons/green_button.gif");
            add(toggle);
            
            JMenuItem tank = new JMenuItem("Press Blue Button");
            tank.addActionListener(e -> {
                emulator.getLevel().getCheats().pressBlueButton();
                emulator.getMainWindow().repaint(false);
            });
            addIcon(tank, "/resources/icons/blue_button.gif");
            add(tank);
            
            
        }
    }

    private class HelpMenu extends JMenu{
        HelpMenu() {
            super("Help");

            JMenuItem helpPopup = new JMenuItem("Help");
            helpPopup.addActionListener(e -> new HelpWindow(emulator));
            addIcon(helpPopup, "/resources/icons/help.gif");
            add(helpPopup);

            JMenuItem variationHelp = new JMenuItem("Variation Tester Documentation");
            variationHelp.addActionListener(e -> {
                if (Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().browse(new URI("https://supercc.bitbusters.club/VariationScriptDocumentation.pdf"));
                    } catch (IOException | URISyntaxException ex) {
                        ex.printStackTrace();
                    }
                }
            });
            add(variationHelp);

            JMenuItem tspHelpPopup = new JMenuItem("TSP Solver Help");
            tspHelpPopup.addActionListener(e -> new TSPHelpWindow(emulator));
            addIcon(tspHelpPopup, "/resources/icons/help.gif");
            add(tspHelpPopup);
        }
    }

    MenuBar(Gui window, SuperCC emulator){
        setPreferredSize(new Dimension(0, 24));
        setLocation(0, 0);
        add(new LevelMenu());
        add(new SolutionMenu());
        add(new TWSMenu());
        add(new ViewMenu());
        add(new ToolMenu());
        add(new CheatMenu());
        add(new HelpMenu());
        this.window = window;
        this.emulator = emulator;
    }

}
