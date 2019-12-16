package emulator;

import game.Level;
import game.Position;
import game.SaveState;
import graphics.Gui;
import graphics.SmallGamePanel;
import util.ByteList;
import util.TreeNode;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static game.SaveState.*;

public class SavestateManager implements Serializable {

    private HashMap<Integer, TreeNode<byte[]>> savestates = new HashMap<>();
    private HashMap<Integer, ByteList> savestateMoves = new HashMap<>();
    private HashMap<Integer, ByteList> savestatetwsMouseMoves = new HashMap<>();
    private TreeNode<byte[]> currentNode;
    private ByteList moves;
    private transient SavestateCompressor compressor;
    private transient List<TreeNode<byte[]>> playbackNodes = new ArrayList<>();
    private transient int playbackIndex = 1;
    private ByteList twsMouseMoves;
    private ArrayList<TreeNode<byte[]>> undesirableSavestates = new ArrayList<>();
    private ByteList[] checkpoints = new ByteList[10];
    private boolean[] recordingCheckpoints = new boolean[10];
    private int[] checkpointStartIndex = new int[10];
    private boolean checkpointRestart = false; //If you restart then set a checkpoint it breaks due to a weird index out of bounds array

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
    
    public void setPlaybackSpeed(int i) {
        playbackWaitTime = waitTimes[i];
    }
    
    public void setNode(TreeNode<byte[]> node) {
        currentNode = node;
    }
    
    private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        compressor = new SavestateCompressor();
        pause = false;
        playbackWaitTime = STANDARD_WAIT_TIME;
        playbackIndex = currentNode.depth();
        playbackNodes = new ArrayList<>(playbackIndex*2);
        playbackNodes.addAll(currentNode.getHistory());
        System.out.println(currentNode.depth());
    }

    public void addRewindState(Level level, byte b){
        pause = true;
        while (playbackNodes.get(playbackNodes.size()-1) != currentNode) {
            playbackNodes.remove(playbackNodes.size()-1);
            moves.removeLast();
        }
        currentNode = new TreeNode<>(level.save(), currentNode);
        compressor.add(currentNode);
        playbackNodes.add(currentNode);
        moves.add(b);
        if (b <= 0) { //Use to math out the relative click position for TWS writing with mouse moves
            Position screenPosition = Position.screenPosition(level.getChip().getPosition());
            Position clickedPosition = Position.clickPosition(screenPosition, b);
            int relativeClickX = clickedPosition.getX() - level.getChip().getPosition().getX(); //down and to the right of Chip are positive, this just quickly gets the relative position following that
            int relativeClickY = clickedPosition.getY() - level.getChip().getPosition().getY();

            twsMouseMoves.add(relativeClickX);
            twsMouseMoves.add(relativeClickY);
        }
        playbackIndex = playbackNodes.size() - 1;
    }
    
    public void restart() {
        while (currentNode.hasParent()) {
            currentNode = currentNode.getParent();
            playbackIndex--;
        }
        checkpointRestart = true;
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
                byte b = SuperCC.lowerCase(moves.get(playbackIndex))[0];
                boolean tickTwice = emulator.tick(b, replayNoSave);
                Thread.sleep(playbackWaitTime);
                if (tickTwice) {
                    emulator.tick((byte) '-', replayNoSave);
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
            byte b = SuperCC.lowerCase(moves.get(playbackIndex))[0];
            boolean tickTwice = emulator.tick(b, TickFlags.REPLAY);
            img = new BufferedImage(32 * 20, 32 * 20, BufferedImage.TYPE_4BYTE_ABGR);
            window.getGamePanel().paintComponent(img.getGraphics());
            images.add(img);
            if (tickTwice && numHalfTicks-- > 0) {
                emulator.tick((byte) '-', TickFlags.REPLAY);
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
        savestatetwsMouseMoves.put(key, twsMouseMoves.clone());
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
            if (!checkpointRestart) checkpointStartIndex[key]--; //The very first time you run this you get a weird thing where its 1 ahead of where it should be
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
            playbackIndex = playbackNodes.size();
            moves = savestateMoves.get(key).clone();
            twsMouseMoves = savestatetwsMouseMoves.get(key).clone();
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

    boolean isUndesirableSaveState() {
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
    
    public ByteList getMoveList(){
        return moves;
    }

    public ByteList gettwsMouseMoveList(){
        return twsMouseMoves;
    }
    
    public byte[] getMoves(){
        byte[] moves = new byte[playbackIndex];
        this.moves.copy(0, moves, 0, playbackIndex);
        return moves;
    }

    public ByteList getCheckpoint(int key) {
        return checkpoints[key];
    }
    
    public String movesToString() {
        return moves.toString(StandardCharsets.ISO_8859_1, playbackIndex);
    }

    public TreeNode<byte[]> getNode(){
        return currentNode;
    }
    
    public SavestateManager(Level level){
        currentNode = new TreeNode<>(level.save(), null);
        playbackNodes.add(currentNode);
        moves = new ByteList();
        twsMouseMoves = new ByteList();
        compressor = new SavestateCompressor();
    }
    
    public LinkedList<Position> getChipHistory(){
        LinkedList<Position> chipHistory = new LinkedList<>();
        for (TreeNode<byte[]> node : currentNode.getHistory()) chipHistory.add(SaveState.getChip(node.getData()).getPosition());
        return chipHistory;
    }
    
    private class SavestateCompressor implements Runnable{
        
        private static final int LAYER_BG_LOCATION = 3,
            LAYER_FG_LOCATION = LAYER_BG_LOCATION + 32 * 32,
            LAYER_FG_END = LAYER_FG_LOCATION + 32 * 32;
        
        private final Stack<TreeNode<byte[]>> uncompressedSavestates;
        private final ByteList list;
        
        private final Thread thread;
        
        void add(TreeNode<byte[]> n){
            uncompressedSavestates.add(n);
            synchronized(thread) {
                thread.notify();
            }
        }
        
        @Override
        public void run(){
            while (true) {
                try {
                    if (uncompressedSavestates.isEmpty()) {
                        synchronized (thread) {
                            thread.wait();
                        }
                    }
                    else {
                        compress(uncompressedSavestates.pop());
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
        private void rleCompress(byte[] uncompressed, ByteList out, int startIndex, int length){
            int lastOrdinal = uncompressed[startIndex];
            int ordinal;
            int copyCount = -1;
            for (int i = startIndex; i < startIndex + length; i++) {
                ordinal = uncompressed[i];
                if (ordinal == lastOrdinal){
                    if (copyCount == 255){
                        out.add(RLE_MULTIPLE);
                        out.add(copyCount);
                        copyCount = 0;
                        out.add(ordinal);
                    }
                    else copyCount++;
                }
                else {
                    if (copyCount != 0){
                        out.add(RLE_MULTIPLE);
                        out.add(copyCount);
                    }
                    out.add(lastOrdinal);
                    copyCount = 0;
                    lastOrdinal = ordinal;
                }
            }
            if (copyCount != 0){
                out.add(RLE_MULTIPLE);
                out.add(copyCount);
            }
            out.add(lastOrdinal);
            out.add(RLE_END);
        }
        
        private void compress(TreeNode<byte[]> n){
            list.clear();
            byte[] uncompressedState = n.getData();
            rleCompress(uncompressedState, list, LAYER_BG_LOCATION, 32*32);
            rleCompress(uncompressedState, list, LAYER_FG_LOCATION, 32*32);
            byte[] out = new byte[uncompressedState.length - 2 * 32 * 32 + list.size()];
            out[0] = COMPRESSED_V2;
            out[1] = uncompressedState[1];
            out[2] = uncompressedState[2];
            list.copy(out, 3);
            System.arraycopy(uncompressedState, LAYER_FG_END, out, 3+list.size(), uncompressedState.length - 2 * 32 * 32 - 3);
            n.setData(out);
        }
        
        SavestateCompressor(){
            uncompressedSavestates = new Stack<>();
            list = new ByteList();
            thread = new Thread(() -> run());
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
        }
        
    }
    
}
