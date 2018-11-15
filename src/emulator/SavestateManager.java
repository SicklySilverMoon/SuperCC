package emulator;

import game.Level;
import game.Position;
import game.SaveState;
import util.ByteList;
import util.TreeNode;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static game.SaveState.COMPRESSED;
import static game.SaveState.RLE_END;
import static game.SaveState.RLE_MULTIPLE;

public class SavestateManager {
    
    private HashMap<Integer, TreeNode<byte[]>> savestates = new HashMap<>();
    private HashMap<Integer, ByteList> savestateMoves = new HashMap<>();
    private TreeNode<byte[]> currentNode;
    private ByteList moves;
    private SavestateCompressor compressor;
    private List<TreeNode<byte[]>> playbackNodes = new ArrayList<>();
    private int playbackIndex = 1;
    
    private boolean pause = true;
    private static final int STANDARD_WAIT_TIME = 100;              // 100 ms means 10 half-ticks per second.
    private int playbackWaitTime = STANDARD_WAIT_TIME;
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
        playbackIndex = playbackNodes.size() - 1;
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
        int levelNumber = emulator.getLevel().levelNumber;
        try {
            while (emulator.getLevel().levelNumber == levelNumber && !pause && playbackIndex + 1 < playbackNodes.size()) {
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
    
    public void addSavestate(int key){
        savestates.put(key, currentNode);
        savestateMoves.put(key, moves.clone());
    }
    
    public boolean load(int key, Level level){
        TreeNode<byte[]> loadedNode = savestates.get(key);
        if (loadedNode == null) return false;
        currentNode = loadedNode;
        level.load(currentNode.getData());
        level.setMoves(moves);
        if (!playbackNodes.contains(currentNode)) {
            playbackNodes = currentNode.getHistory();
            playbackIndex = playbackNodes.size();
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
    
    public byte[] getStartingState() {
        TreeNode<byte[]> state = currentNode;
        while (state.hasParent()) state = state.getParent();
        return state.getData();
    }
    
    public byte[] getMoves(){
        byte[] moves = new byte[playbackIndex];
        this.moves.copy(0, moves, 0, playbackIndex);
        return moves;
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
        moves = level.getMoves();
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
            out[0] = COMPRESSED;
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
