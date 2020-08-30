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
import java.io.Serializable;
import java.util.*;

public class SavestateManager implements Serializable {

    private HashMap<Integer, TreeNode<byte[]>> savestates = new HashMap<>();
    private HashMap<Integer, CharList> savestateMoves = new HashMap<>();
    private TreeNode<byte[]> currentNode;
    private CharList moves;
    private CharList[] checkpoints = new CharList[10];
    private transient SuperCC emulator;
    private transient List<TreeNode<byte[]>> playbackNodes = new ArrayList<>();
    private transient int playbackIndex = 0;
    private transient ArrayList<TreeNode<byte[]>> undesirableSavestates = new ArrayList<>();
    private transient boolean[] recordingCheckpoints = new boolean[10];
    private transient int[] checkpointStartIndex = new int[10];

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
    
    private static final long serialVersionUID = -3232323211211410511L;
    private static final int VERSION_V0 = 0;

    public void setPlaybackSpeed(int i) {
        playbackWaitTime = waitTimes[i];
    }

    public void setNode(TreeNode<byte[]> node) {
        currentNode = node;
    }

    private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
        stream.write(VERSION_V0);
        stream.writeObject(savestates);
        stream.write(savestateMoves.size());
        for (int i : savestateMoves.keySet()) {
            stream.write(i);
            stream.writeObject(savestateMoves.get(i).toArray());
        }
        stream.writeObject(currentNode);
        stream.writeObject(moves.toArray());
        stream.write(checkpoints.length);
        for (CharList checkpoint : checkpoints) {
            stream.writeObject(checkpoint.toArray());
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
            currentNode = (TreeNode<byte[]>) stream.readObject();
            moves = new CharList((char[]) stream.readObject());
            int checkpointLength = stream.read();
            checkpoints = new CharList[checkpointLength];
            for (int i=0; i < checkpointLength; i++) {
                checkpoints[i] = new CharList((char[]) stream.readObject());
            }

            undesirableSavestates = new ArrayList<>();
            pause = false;
            playbackWaitTime = STANDARD_WAIT_TIME;
            playbackIndex = currentNode.depth();
            playbackNodes = new ArrayList<>(playbackIndex * 2);
            playbackNodes.addAll(currentNode.getHistory());
            System.out.println("Current node depth: " + currentNode.depth());
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
        while (currentNode.hasParent()) {
            currentNode = currentNode.getParent();
            playbackIndex--;
        }
    }
    
    public void rewind(){
        if (currentNode.hasParent()) {
            currentNode = currentNode.getParent();
            playbackIndex--;
        }
    }
    
    public void playbackRewind(int index){
        currentNode = playbackNodes.get(index);
        playbackIndex = index;
    }
    
    public void replay(){
        if (playbackIndex + 1 < playbackNodes.size()) {
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
        final TickFlags replayNoSave = new TickFlags(true, false, false);
        pause = false;
        int levelNumber = emulator.getLevel().getLevelNumber();
        try {
            while (emulator.getLevel().getLevelNumber() == levelNumber && !pause && playbackIndex + 1 < playbackNodes.size()) {
                emulator.getLevel().load(currentNode.getData());
                char c = SuperCC.lowerCase(moves.get(playbackIndex))[0];
                boolean tickTwice = emulator.tick(c, replayNoSave);
                Thread.sleep(playbackWaitTime);
                if (tickTwice) {
                    emulator.tick(SuperCC.WAIT, replayNoSave);
                    Thread.sleep(playbackWaitTime);
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
    
    public List<BufferedImage> play(SuperCC emulator, int numHalfTicks) {
        ArrayList<BufferedImage> images = new ArrayList<>();
        Gui window = emulator.getMainWindow();
        int tileWidth = window.getGamePanel().getTileWidth();
        int tileHeight = window.getGamePanel().getTileHeight();
        int windowSizeY = ((SmallGamePanel) window.getGamePanel()).getWindowSizeY();
        int windowSizeX = ((SmallGamePanel) window.getGamePanel()).getWindowSizeX();
        BufferedImage img = new BufferedImage(windowSizeX * tileWidth, windowSizeY * tileHeight, BufferedImage.TYPE_4BYTE_ABGR);
        window.getGamePanel().paintComponent(img.getGraphics());
        images.add(img);
        while (numHalfTicks-- > 0 && playbackIndex + 1 < playbackNodes.size()) {
            char c = SuperCC.lowerCase(moves.get(playbackIndex))[0];
            boolean tickTwice = emulator.tick(c, TickFlags.REPLAY);
            img = new BufferedImage(32 * 20, 32 * 20, BufferedImage.TYPE_4BYTE_ABGR);
            window.getGamePanel().paintComponent(img.getGraphics());
            images.add(img);
            if (tickTwice && numHalfTicks-- > 0) {
                emulator.tick(SuperCC.WAIT, TickFlags.REPLAY);
                img = new BufferedImage(32 * 20, 32 * 20, BufferedImage.TYPE_4BYTE_ABGR);
                window.getGamePanel().paintComponent(img.getGraphics());
                images.add(img);
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

    void addUndesirableSavestate(){
        undesirableSavestates.add(currentNode); //Marks a level state as undesired so it can be checked for and alerted
    }

    boolean checkpointRecorder(int key) {
        if (!recordingCheckpoints[key]) {
            checkpointStartIndex[key] = playbackIndex; //Store the current position in the move array so we can come back to it later
            recordingCheckpoints[key] = true;
            return true;
        }
        else {
            checkpoints[key] = moves.sublist(checkpointStartIndex[key], moves.size()); //Puts all the moves recorded into this list
            recordingCheckpoints[key] = false;
            return false;
        }
    }
    
    public boolean load(int key, Level level){
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
    
    public int getPlaybackIndex() {
        return playbackIndex;
    }
    
    public byte[] getSavestate(){
        return currentNode.getData();
    }

    boolean isUndesirableSavestate() {
        for (TreeNode<byte[]> node : undesirableSavestates) {
            byte[] savedState = node.getData();
            if (Arrays.equals(savedState, currentNode.getData())) return true;
        }
        return false;
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

    public CharList getCheckpoint(int key) {
        return checkpoints[key];
    }
    
    public String movesToString() {
        return moves.toString(playbackIndex);
    }

    public TreeNode<byte[]> getNode(){
        return currentNode;
    }
    
    SavestateManager(SuperCC emulator, Level level){
        this.emulator = emulator;
//        emulator.savestateCompressor.initialise();
        currentNode = new TreeNode<>(level.save(), null);
        playbackNodes.add(currentNode);
        moves = new CharList();
        for (int i=0; i < checkpoints.length; i++) checkpoints[i] = new CharList();
    }
    
    public LinkedList<Position> getChipHistory(){
        LinkedList<Position> chipHistory = new LinkedList<>();
        for (TreeNode<byte[]> node : currentNode.getHistory()) chipHistory.add(Savestate.getChip(node.getData()).getPosition());
        return chipHistory;
    }

    public void setEmulator(SuperCC emulator) {
        this.emulator = emulator;
//        emulator.savestateCompressor.initialise();
        /* Yes having this here does make the method do more than its name implies, however seeing as the only reason
        emulator is used is in order to have access to the compressor, and whenever emulator is changed it means that
        a new savestate manager was created/read from a file, meaning that the compressor should be reset as well */
    }
}
