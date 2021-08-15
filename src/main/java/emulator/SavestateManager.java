package emulator;

import game.Level;
import game.Position;
import game.Savestate;
import graphics.Gui;
import graphics.SmallGamePanel;
import util.CharList;
import util.TreeNode;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.IntStream;

public class SavestateManager implements Serializable {

    private HashMap<Integer, TreeNode<byte[]>> savestates = new HashMap<>();
    private HashMap<Integer, CharList> savestateMoves = new HashMap<>();
    private TreeNode<byte[]> currentNode;
    private CharList moves;
    private CharList[] macros = new CharList[10];
    private transient SuperCC emulator;
    private transient List<TreeNode<byte[]>> playbackNodes = new ArrayList<>();
    private transient int playbackIndex = 0;
    private transient boolean[] recordingMacros = new boolean[10];
    private transient int[] macroStartIndices = new int[10];
    private transient byte[] levelTitle;

    private transient boolean pause = true;
    private static final int STANDARD_WAIT_TIME = 100;              // 100 ms means 10 half-ticks per second.
    private transient int playbackWaitTime = STANDARD_WAIT_TIME;
    private static final int[] waitTimes = new int[]{
        STANDARD_WAIT_TIME * 8,
        STANDARD_WAIT_TIME * 4,
        STANDARD_WAIT_TIME * 2,
        STANDARD_WAIT_TIME,
        STANDARD_WAIT_TIME / 2,
        STANDARD_WAIT_TIME / 4,
        STANDARD_WAIT_TIME / 8
    };
    public static final int NUM_SPEEDS = waitTimes.length;
    
    @Serial
    private static final long serialVersionUID = -3232323211211410511L;
    private static final int VERSION_V0 = 0;

    public void setPlaybackSpeed(int i) {
        playbackWaitTime = waitTimes[i];
    }

    public int getPlaybackSpeed() {
        return IntStream.range(0, waitTimes.length)
                .filter(i -> waitTimes[i] == playbackWaitTime)
                .findFirst()
                .orElse(-1);
    }

    public byte[] getLevelTitle() {
        return levelTitle;
    }

    public void setNode(TreeNode<byte[]> node) {
        currentNode = node;
    }

    @Serial
    private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
        stream.write(VERSION_V0);
        stream.writeObject(savestates);
        stream.write(savestateMoves.size());
        for (int i : savestateMoves.keySet()) {
            stream.write(i);
            stream.writeObject(savestateMoves.get(i).toArray());
        }
        stream.write(macros.length);
        for (CharList m : macros) {
            stream.writeObject(m.toArray());
        }
        stream.write(levelTitle.length);
        for(byte b : levelTitle) {
            stream.write(b);
        }
    }

    @SuppressWarnings("unchecked")
    private void readObject(java.io.ObjectInputStream stream)
        throws IOException, ClassNotFoundException {
        if (stream.read() == VERSION_V0) {
            savestates = (HashMap<Integer, TreeNode<byte[]>>) stream.readObject();
            savestateMoves = new HashMap<>();
            int movesLength = stream.read();
            for (int i=0; i < movesLength; i++) {
                savestateMoves.put(stream.read(), (new CharList((char[]) stream.readObject())));
            }
            int macroListLength = stream.read();
            macros = new CharList[macroListLength];
            for (int i=0; i < macroListLength; i++) {
                macros[i] = new CharList((char[]) stream.readObject());
            }
            int levelTitleLength = stream.read();
            levelTitle = new byte[levelTitleLength];
            for(int i = 0; i < levelTitleLength; i++) {
                levelTitle[i] = (byte) stream.read();
            }
        }
    }

    public void addRewindState(Level level, char c){
        pause = true;
        while (playbackNodes.get(playbackNodes.size()-1) != currentNode) {
            playbackNodes.remove(playbackNodes.size()-1);
            moves.removeLast();
        }
        currentNode = new TreeNode<>(level.save(), currentNode);
//        emulator.savestateCompressor.add(currentNode);
        playbackNodes.add(currentNode);
        moves.add(c);
        playbackIndex = playbackNodes.size() - 1;
    }
    
    public void restart() {
        pause = true;
        while (currentNode.hasParent()) {
            currentNode = currentNode.getParent();
            playbackIndex--;
        }
    }
    
    public void rewind(){
        pause = true;
        if (currentNode.hasParent()) {
            currentNode = currentNode.getParent();
            playbackIndex--;
        }
    }
    
    public void playbackRewind(int index){
        pause = true;
        currentNode = playbackNodes.get(index);
        playbackIndex = index;
    }
    
    public void replay(){
        if (playbackIndex + 1 < playbackNodes.size()) {
            currentNode = playbackNodes.get(++playbackIndex);
        }
    }

    public void replayAll() {
        pause = true;
        while (playbackIndex + 1 < playbackNodes.size()) {
            currentNode = playbackNodes.get(++playbackIndex);
        }
    }
    
    public void togglePause() {
        pause = !pause;
    }
    
    public boolean isPaused() {
        return pause;
    }
    
    public void play(SuperCC emulator) {
        pause = false;
        try {
            while (!pause && playbackIndex + 1 < playbackNodes.size()) {
                emulator.getLevel().load(currentNode.getData());
                char c = SuperCC.lowerCase(moves.get(playbackIndex));
                boolean tickMulti = emulator.tick(c, TickFlags.REPLAY);
                Thread.sleep(playbackWaitTime);
                if (tickMulti) {
                    for (int i=0; i < emulator.getLevel().ticksPerMove() - 1; i++) {
                        emulator.tick(SuperCC.WAIT, TickFlags.REPLAY);
                        Thread.sleep(playbackWaitTime);
                    }
                }
                replay();
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (!pause) {
            emulator.getMainWindow().getPlayButton().doClick();
            emulator.showAction("Playback finished");
        }
        emulator.repaint(false);
    }
    
    public List<BufferedImage> play(SuperCC emulator, int numTicks) {
        pause = true;
        ArrayList<BufferedImage> images = new ArrayList<>();
        Gui window = emulator.getMainWindow();
        int tileWidth = window.getGamePanel().getTileWidth();
        int tileHeight = window.getGamePanel().getTileHeight();
        int windowSizeY = ((SmallGamePanel) window.getGamePanel()).getWindowSizeY();
        int windowSizeX = ((SmallGamePanel) window.getGamePanel()).getWindowSizeX();
        BufferedImage img = new BufferedImage(windowSizeX * tileWidth, windowSizeY * tileHeight, BufferedImage.TYPE_4BYTE_ABGR);
        window.getGamePanel().paintComponent(img.getGraphics());
        images.add(img);
        while (numTicks-- > 0 && playbackIndex + 1 < playbackNodes.size()) {
            char c = SuperCC.lowerCase(moves.get(playbackIndex));
            boolean tickMulti = emulator.tick(c, TickFlags.REPLAY);
            img = new BufferedImage(32 * 20, 32 * 20, BufferedImage.TYPE_4BYTE_ABGR);
            window.getGamePanel().paintComponent(img.getGraphics());
            images.add(img);
            if (tickMulti && numTicks-- > 0) {
                for (int i=0; i < emulator.getLevel().ticksPerMove() - 1; i++) {
                    emulator.tick(SuperCC.WAIT, TickFlags.REPLAY);
                    img = new BufferedImage(32 * 20, 32 * 20, BufferedImage.TYPE_4BYTE_ABGR);
                    window.getGamePanel().paintComponent(img.getGraphics());
                    images.add(img);
                }
            }
            replay();
        }
        if (!pause) {
            window.getPlayButton().doClick();
        }
        emulator.repaint(false);
        return images;
    }
    
    public void addSavestate(int key){
        savestates.put(key, currentNode);
        savestateMoves.put(key, moves.clone());
    }

    /**
     * Begins or ends 'recording' a move macro in the specified key slot.
     * @param key an int value between 0 and 9 representing the macro slot to use.
     * @return true if this is start of a recording, false if this the end and the recording has been saved.
     */
    boolean macroRecorder(int key) {
        if (!recordingMacros[key]) {
            macroStartIndices[key] = playbackIndex; //Store the current position in the move array so we can come back to it later
            recordingMacros[key] = true;
            return true;
        }
        else {
            macros[key] = moves.sublist(macroStartIndices[key], playbackIndex); //Puts all the moves recorded into this list
            recordingMacros[key] = false;
            return false;
        }
    }

    void playMacro(int key) {
        pause = true;
        CharList macro = macros[key];
        for (char c : macro) {
            emulator.tick(SuperCC.lowerCase(c), TickFlags.PRELOADING);
        }
    }

    public CharList[] getMacros() {
        return macros;
    }
    
    public boolean load(int key, Level level){
        pause = true;
        TreeNode<byte[]> loadedNode = savestates.get(key);
        if (loadedNode == null) return false;
        currentNode = loadedNode;
        level.load(currentNode.getData());
        if (!playbackNodes.contains(currentNode)) {
            playbackNodes = currentNode.getHistory();
            playbackIndex = playbackNodes.size() - 1;
            moves = savestateMoves.get(key).clone();
        }
        else {
            playbackIndex = playbackNodes.indexOf(currentNode);
        }
        return true;
    }
    
    public List<TreeNode<byte[]>> getPlaybackNodes() {
        return playbackNodes;
    }

    public void setPlaybackNodes(TreeNode<byte[]> node) {
        pause = true;
        this.playbackNodes = new ArrayList<>();
        this.playbackNodes.addAll(node.getHistory());
    }
    
    public int getPlaybackIndex() {
        return playbackIndex;
    }

    public void setPlaybackIndex(int index) {
        pause = true;
        this.playbackIndex = index;
    }
    
    public byte[] getSavestate(){
        return currentNode.getData();
    }
    
    public byte[] getStartingState() {
        TreeNode<byte[]> state = currentNode;
        while (state.hasParent()) state = state.getParent();
        return state.getData();
    }
    
    public CharList getMoveList(){
        return moves;
    }
    
    public char[] getMoves(){
        char[] moves = new char[playbackIndex];
        this.moves.copy(0, moves, 0, playbackIndex);
        return moves;
    }

    public void setMoves(CharList moves) {
        pause = true;
        this.moves = moves;
    }
    
    public String movesToString() {
        return moves.toString(playbackIndex);
    }

    public TreeNode<byte[]> getNode(){
        return currentNode;
    }
    
    SavestateManager(SuperCC emulator, Level level) throws UnsupportedEncodingException {
        this.emulator = emulator;
        levelTitle = level.getTitle().getBytes("Windows-1252");
//        emulator.savestateCompressor.initialise();
        currentNode = new TreeNode<>(level.save(), null);
        playbackNodes.add(currentNode);
        moves = new CharList();
        for (int i = 0; i < macros.length; i++)
            macros[i] = new CharList();
    }
    
    public LinkedList<Position> getChipHistory(){
        LinkedList<Position> chipHistory = new LinkedList<>();
        for (TreeNode<byte[]> node : currentNode.getHistory()) chipHistory.add(Savestate.getChip(node.getData()).getPosition());
        return chipHistory;
    }

    public void setEmulator(SuperCC emulator) throws UnsupportedEncodingException {
        pause = true;
        this.emulator = emulator;
        levelTitle = emulator.getLevel().getTitle().getBytes("Windows-1252");
        emulator.repaint(true);
//        emulator.savestateCompressor.initialise();
        /* Yes having this here does make the method do more than its name implies, however seeing as the only reason
        emulator is used is in order to have access to the compressor, and whenever emulator is changed it means that
        a new savestate manager was created/read from a file, meaning that the compressor should be reset as well */
    }
}
