package graphics;

import emulator.SavestateManager;
import emulator.Solution;
import emulator.SuperCC;
import emulator.TickFlags;
import game.Direction;
import game.Level;
import game.Ruleset;
import game.Step;
import io.TWSWriter;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import tools.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import static java.awt.event.KeyEvent.*;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;

class MenuBar extends JMenuBar{

    private final SuperCC emulator;
    private final Gui window;
    
    private void addIcon(JMenuItem m, String path){
        try {
            m.setIcon(new ImageIcon(ImageIO.read(getClass().getResource(path))));
        }
        catch (Exception ignored) {}
    }

    private class LevelMenu extends JMenu{
        LevelMenu(){
            super("Level");

            JMenuItem openLevelset = new JMenuItem("Open levelset");
            openLevelset.addActionListener(e -> {
                File levelset = openFile(emulator.getPaths().getLevelsetFolderPath(), "dat", "ccl");
                if (levelset != null) {
                    emulator.getPaths().setLevelsetFolderPath(levelset.getParent());
                    emulator.openLevelset(levelset);
                }
            });
            openLevelset.setAccelerator(KeyStroke.getKeyStroke(VK_O, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
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
            restart.setAccelerator(KeyStroke.getKeyStroke(VK_R, InputEvent.CTRL_DOWN_MASK));
            addIcon(restart, "/resources/icons/restart.gif");
            add(restart);
    
            JMenuItem next = new JMenuItem("Next");
            next.addActionListener(e -> {
                if (!SuperCC.areToolsRunning()) emulator.loadLevel(emulator.getLevel().getLevelNumber() + 1);
            });
            next.setAccelerator(KeyStroke.getKeyStroke(VK_N, InputEvent.CTRL_DOWN_MASK));
            addIcon(next, "/resources/icons/right.gif");
            add(next);

            JMenuItem previous = new JMenuItem("Previous");
            previous.addActionListener(e -> {
                if (!SuperCC.areToolsRunning()) emulator.loadLevel(emulator.getLevel().getLevelNumber() - 1);
            });
            previous.setAccelerator(KeyStroke.getKeyStroke(VK_P, InputEvent.CTRL_DOWN_MASK));
            addIcon(previous, "/resources/icons/left.gif");
            add(previous);

            JMenuItem goTo = new JMenuItem("Go to...");
            goTo.addActionListener(e -> {
                if (!SuperCC.areToolsRunning()) {
                    String s = JOptionPane.showInputDialog(window, "Choose a level number");
                    if (s.length() == 0) return;
                    try {
                        int n = Integer.parseInt(s);
                        emulator.loadLevel(n, 0, Step.EVEN, true, Ruleset.CURRENT, Direction.UP);
                    } catch (NumberFormatException nfe) {
                        JOptionPane.showMessageDialog(window, "Please Enter a Whole Number");
                    }
                }
            });
            goTo.setAccelerator(KeyStroke.getKeyStroke(VK_G, InputEvent.CTRL_DOWN_MASK));
            addIcon(goTo, "/resources/icons/goto.gif");
            add(goTo);

            add(new JSeparator());

            JMenuItem changeStep = new JMenuItem("Change stepping value");
            changeStep.addActionListener(e -> {
                if (!SuperCC.areToolsRunning()) {
                    Level oldLevel = emulator.getLevel();
                    Step newStep = Step.EVEN;
                    if (oldLevel.ticksPerMove() == 2 && oldLevel.getStep() == Step.EVEN)
                        newStep = Step.ODD;
                    else if (oldLevel.ticksPerMove() != 2)
                        newStep = oldLevel.getStep().next();
                    emulator.loadLevel(oldLevel.getLevelNumber(), oldLevel.getRngSeed(), newStep, true,
                            Ruleset.CURRENT, oldLevel.getInitialRFFDirection());
                }
            });
            add(changeStep);

            JMenuItem rngSeed = new JMenuItem("Set RNG Seed");
            rngSeed.addActionListener(e -> {
                if (!SuperCC.areToolsRunning()) {
                    String s = JOptionPane.showInputDialog(window, "Choose a starting seed");
                    if (s.equals("")) return;
                    try {
                        Level oldLevel = emulator.getLevel();
                        int n = Integer.parseInt(s);
                        emulator.loadLevel(oldLevel.getLevelNumber(), n, oldLevel.getStep(), true,
                                Ruleset.CURRENT, oldLevel.getInitialRFFDirection());
                    } catch (NumberFormatException nfe) {
                        JOptionPane.showMessageDialog(window, "Please Enter a Whole Number");
                    }
                }
            });
            addIcon(rngSeed, "/resources/icons/rng.gif");
            add(rngSeed);

            JMenuItem changeRules = new JMenuItem("Change ruleset");
            changeRules.addActionListener(e -> {
                if (!SuperCC.areToolsRunning()) {
                    Level level = emulator.getLevel();
                    emulator.loadLevel(level.getLevelNumber(), level.getRngSeed(), level.getStep(), false,
                            level.getRuleset().swap(), level.getInitialRFFDirection());
                }
            });
            addIcon(changeRules, "/resources/icons/change.gif");
            add(changeRules);

            JMenuItem changeInitialSlide = new JMenuItem("Change Initial RFF Direction");
            changeInitialSlide.addActionListener(e -> {
                if (!SuperCC.areToolsRunning()) {
                    Level level = emulator.getLevel();
                    emulator.loadLevel(level.getLevelNumber(), level.getRngSeed(), level.getStep(), true,
                            level.getRuleset(), level.getInitialRFFDirection().turn(Direction.TURN_RIGHT));
                }
            });
            addIcon(changeInitialSlide, "/resources/icons/RFF.png");
            add(changeInitialSlide);
        }
    }
    
    private class SolutionMenu extends JMenu{
        SolutionMenu(){
            super("Solution");

            JMenuItem saveAs = new JMenuItem("Save as");
            saveAs.setAccelerator(KeyStroke.getKeyStroke(VK_S, InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
            saveAs.addActionListener(event -> {
                Level l = emulator.getLevel();
                Solution solution = new Solution(emulator.getSavestates().getMoveList(), l.getRngSeed(), l.getStep(), l.getRuleset(), l.getInitialRFFDirection());
                saveNewFile(solution.toString().getBytes(UTF_8), emulator.getJSONPath(), "json");
            });
            addIcon(saveAs, "/resources/icons/saveAs.gif");
            add(saveAs);
    
            JMenuItem save = new JMenuItem("Save");
            save.setAccelerator(KeyStroke.getKeyStroke(VK_S, InputEvent.CTRL_DOWN_MASK));
            save.addActionListener(event -> {
                Level l = emulator.getLevel();
                Solution solution = new Solution(emulator.getSavestates().getMoveList(), l.getRngSeed(), l.getStep(), l.getRuleset(), l.getInitialRFFDirection());
                try{
                    FileOutputStream fos = new FileOutputStream(emulator.getJSONPath());
                    fos.write(solution.toString().getBytes(UTF_8));
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
            open.setAccelerator(KeyStroke.getKeyStroke(VK_O, InputEvent.CTRL_DOWN_MASK));
            open.addActionListener(event -> {
                byte[] fileBytes = openFileBytes(emulator.getJSONPath(), "json");
                if (fileBytes != null) {
                    Solution solution;
                    JSONParser parser = new JSONParser();
                    String fileString = new String(fileBytes, UTF_8);
                    JSONObject json;
                    try {
                        json = (JSONObject) parser.parse(fileString);
                    } catch (ParseException e) {
                        throw new IllegalArgumentException("Invalid solution file:\n" + emulator.getJSONPath());
                    }
                    String encoding = (String) json.get(Solution.ENCODE);

                    if ("UTF-8".equals(encoding)) solution = Solution.fromJSON(fileString);
                    else solution = Solution.fromJSON(new String(fileBytes, ISO_8859_1));
                    solution.load(emulator);
                }
            });
            addIcon(open, "/resources/icons/open.gif");
            add(open);
            
            JMenuItem seedSearch = new JMenuItem("Search for seeds");
            seedSearch.addActionListener(event -> {
                byte[] fileBytes = openFileBytes(emulator.getJSONPath(), "json");
                if (fileBytes != null) {
                    Solution solution;
                    JSONParser parser = new JSONParser();
                    String fileString = new String(fileBytes, UTF_8);
                    JSONObject json;
                    try {
                        json = (JSONObject) parser.parse(fileString);
                    } catch (ParseException e) {
                        throw new IllegalArgumentException("Invalid solution file:\n" + emulator.getJSONPath());
                    }
                    String encoding = (String) json.get(Solution.ENCODE);

                    if ("UTF-8".equals(encoding)) solution = Solution.fromJSON(fileString);
                    else solution = Solution.fromJSON(new String(fileBytes, ISO_8859_1));
                    new SeedSearch(emulator, solution);
                }
            });
            addIcon(seedSearch, "/resources/icons/open.gif");
            add(seedSearch);
            
            addSeparator();
    
            JMenuItem copy = new JMenuItem("Copy solution");
            copy.setAccelerator(KeyStroke.getKeyStroke(VK_C, InputEvent.CTRL_DOWN_MASK));
            copy.addActionListener(event -> {
                Level level = emulator.getLevel();
                Solution solution = new Solution(emulator.getSavestates().getMoveList(), level.getRngSeed(), level.getStep(), level.getRuleset(), level.getInitialRFFDirection());
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(solution.toString()), null);
                emulator.showAction("Copied solution");
                emulator.getMainWindow().repaint(false);
            });
            addIcon(copy, "/resources/icons/copy.gif");
            add(copy);
    
            JMenuItem paste = new JMenuItem("Paste solution");
            paste.setAccelerator(KeyStroke.getKeyStroke(VK_V, InputEvent.CTRL_DOWN_MASK));
            paste.addActionListener(event -> {
                Level level = emulator.getLevel();
                Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(this);
                try {
                    Solution.fromJSON((String) t.getTransferData(DataFlavor.stringFlavor)).load(emulator);
                    emulator.showAction("Pasted solution");
                    emulator.getMainWindow().repaint(false);
                }
                catch (IllegalArgumentException e){ //If the clipboard isn't an entire JSON solution it might be raw moves, which should be put in
                    try {
                        char[] moves = (t.getTransferData(DataFlavor.stringFlavor)).toString().toCharArray();
                        for (int i = 0; i < moves.length; i++) {
                            char ch = moves[i];
                            if (level.getChip().isDead())
                                break;
                            char c = SuperCC.lowerCase(ch);
                            if (emulator.tick(c, TickFlags.PRELOADING))
                                i += level.ticksPerMove() - 1;
                        }
                        emulator.repaint(true);
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
                try {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    ObjectOutputStream out = new ObjectOutputStream(bos);
                    out.writeObject(emulator.getSavestates());
                    out.flush();
                    saveNewFile(bos.toByteArray(), emulator.getSerPath(), "ser");
                    out.close();
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    emulator.throwError("Could not save file:" + e.getMessage());
                }
            });
            addIcon(saveSavestates, "/resources/icons/saveAs.gif");
            add(saveSavestates);
    
            JMenuItem loadStates = new JMenuItem("Load states from disk");
            loadStates.addActionListener(event -> {
                try {
                    byte[] fileBytes = openFileBytes(emulator.getSerPath(), "ser");
                    if (fileBytes != null) {
                        ByteArrayInputStream bis = new ByteArrayInputStream(fileBytes);
                        SavestateManager savestates = (SavestateManager) new ObjectInputStream(bis).readObject();
                        if(Arrays.equals(emulator.getLevel().getTitle(), savestates.getLevelTitle())) {
                            savestates.setEmulator(emulator);
                            savestates.setNode(emulator.getSavestates().getNode());
                            savestates.setMoves(emulator.getSavestates().getMoveList().clone());
                            savestates.setPlaybackIndex(emulator.getSavestates().getPlaybackIndex());
                            savestates.setPlaybackNodes(emulator.getSavestates().getNode());
                            savestates.setPlaybackSpeed(emulator.getSavestates().getPlaybackSpeed());
                            emulator.setSavestates(savestates);
                        }
                        else {
                            String levelString = new String(savestates.getLevelTitle());
                            emulator.throwMessage("Cannot load savestates for level " + levelString + " into current level.");
                        }
                    }
                }
                catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    emulator.throwError("could not open file: " + e.getMessage());
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
                Level level = emulator.getLevel();
                Solution solution = new Solution(emulator.getSavestates().getMoveList(),
                        level.getRngSeed(),
                        level.getStep(), level.getRuleset(), level.getInitialRFFDirection());

                Path tws = saveNewFile(TWSWriter.write(level, solution, emulator.getSavestates()), emulator.getPaths().getTWSPath(), "tws");
                emulator.getPaths().setTWSPath(tws.getParent().toString());
            });
            add(newTWS);
            addIcon(newTWS, "/resources/icons/new.gif");

            JMenuItem openTWS = new JMenuItem("Open tws");
            openTWS.addActionListener(e -> {
                File file = openFile(emulator.getPaths().getTWSPath(), "tws");
                if (file != null) {
                    emulator.getPaths().setTWSPath(file.getParent());
                    emulator.setTWSFile(file);
                }
            });
            add(openTWS);
            addIcon(openTWS, "/resources/icons/open.gif");

            add(new JSeparator());

            JMenuItem loadSolution = new JMenuItem("Load solution");
            loadSolution.addActionListener(event -> {
                try {
                    emulator.twsReader.readSolution(emulator.getLevel()).load(emulator);
                } catch (IOException e) {
                    emulator.throwError("Error while loading solution");
                }
            });
            add(loadSolution);
            addIcon(loadSolution, "/resources/icons/skip.gif");

            JMenuItem verify = new JMenuItem("Verify tws");
            verify.addActionListener(e -> new VerifyTWS(emulator));
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
                    int tileWidth, tileHeight;
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
            for (int size : sizes) {
                JRadioButton sizeButton = new JRadioButton(size + "x" + size);
                sizeButton.addActionListener((e) -> {
                    try {
                        TileSheet ts;
                        try {
                            ts = emulator.getMainWindow().getGamePanel().getTileSheet();
                        } catch (NullPointerException npe) {
                            ts = Gui.DEFAULT_TILESHEET;
                        }
                        SmallGamePanel gamePanel = (SmallGamePanel) emulator.getMainWindow().getGamePanel();
                        emulator.getMainWindow().getGamePanel().initialise(emulator, ts.getTileSheet(size, size), ts, size, size);
                        window.getInventoryPanel().initialise(emulator);
                        window.getInventoryPanel().setPreferredSize(new Dimension(4 * size + 10, 2 * size + 10));
                        window.getInventoryPanel().setSize(4 * size + 10, 2 * size + 10);
                        window.setSize(200 + size * gamePanel.getWindowSizeX(), 200 + size * gamePanel.getWindowSizeY());
                        window.getGamePanel().setPreferredSize(new Dimension(size * gamePanel.getWindowSizeX(), size * gamePanel.getWindowSizeY()));
                        window.getGamePanel().setSize(size * gamePanel.getWindowSizeX(), size * gamePanel.getWindowSizeY());
                        window.pack();
                        window.repaint(true);

                        emulator.getPaths().setTileSizes(new int[]{size, size});
                    } catch (IOException e1) {
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
            for (int size : sizes) {
                sizeButton = new JRadioButton(size + "x" + size);
                sizeButton.addActionListener((e) -> {
                    SmallGamePanel gamePanel = (SmallGamePanel) emulator.getMainWindow().getGamePanel();
                    gamePanel.setWindowSize(size, size);
                    int tileWidth = gamePanel.getTileWidth(), tileHeight = gamePanel.getTileHeight();
                    window.setSize(200 + tileWidth * size, 200 + tileHeight * size);
                    window.getGamePanel().setPreferredSize(new Dimension(tileWidth * size, tileHeight * size));
                    window.getGamePanel().setSize(tileWidth * size, tileHeight * size);
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

            List<Boolean> selected = Arrays.asList(
                    emulator.getPaths().getTWSNotation()
            );

            for (int i = 0; i < setterHUDNames.length; i++){
                JToggleButton b = new JToggleButton(setterHUDNames[i]);
                Consumer<Boolean> setter = HUDSetters.get(i);
                b.setSelected(selected.get(i));
                b.addActionListener(e -> {
                    setter.accept(((AbstractButton) e.getSource()).isSelected());
                    window.repaint(false);
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
                protected Void doInBackground() {
                    new ControlGUI(emulator);
                    return null;
                }
            }.execute()) ;
            add(controls);
    
            JMenuItem gif = new JMenuItem("Record gif");
            gif.addActionListener(e -> new GameGifRecorder(emulator));
            addIcon(gif, "/resources/icons/video.gif");
            add(gif);

            JMenuItem variations = new JMenuItem("Variation testing");
            variations.addActionListener(e -> new VariationTesting(emulator));
            add(variations);

            JMenuItem tsp = new JMenuItem("TSP Solver");
            tsp.addActionListener(e -> new TSPGUI(emulator));
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

            JMenuItem monsterList = new JMenuItem("Change Monster List Positions");
            monsterList.addActionListener(e -> new MonsterlistRearrangeGUI(emulator));
            add(monsterList);
            
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

    private Path saveNewFile(byte[] out, String path, String extension) {
        try {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter(extension, extension));
            File filePath = new File(path);
            if (filePath.isDirectory()) fc.setCurrentDirectory(filePath);
            else {
                fc.setCurrentDirectory(filePath.getParentFile());
                fc.setSelectedFile(filePath);
            }
            if (fc.showSaveDialog(window) == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                String filename = file.toString();
                if (!filename.endsWith("." + extension)) filename += "." + extension;
                FileOutputStream fos = new FileOutputStream(filename);
                fos.write(out);
                fos.close();
                return new File(filename).toPath();
            }
        }
        catch (IOException e){
            e.printStackTrace();
            emulator.throwError("Could not save file: "+e.getMessage());
        }
        return null;
    }

    private byte[] openFileBytes(String path, String... extensions) {
        try{
            return Files.readAllBytes(openFile(path, extensions).toPath());
        }
        catch (IOException e){
            e.printStackTrace();
            emulator.throwError("Could not load file:\n" + e.getMessage());
        }
        return null;
    }

    private File openFile(String path, String... extensions) {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter(extensions[0], extensions));
        File file = new File(path);
        if (file.isDirectory()) fc.setCurrentDirectory(file);
        else {
            fc.setCurrentDirectory(file.getParentFile());
            fc.setSelectedFile(new File(path));
        }
        if (fc.showOpenDialog(window) == JFileChooser.APPROVE_OPTION) {
            return fc.getSelectedFile();
        }
        return null;
    }

    MenuBar(Gui window, SuperCC emulator){
        this.window = window;
        this.emulator = emulator;
        setPreferredSize(new Dimension(0, 24));
        setLocation(0, 0);
        add(new LevelMenu());
        add(new SolutionMenu());
        add(new TWSMenu());
        add(new ViewMenu());
        add(new ToolMenu());
        add(new CheatMenu());
        add(new HelpMenu());
    }

}
