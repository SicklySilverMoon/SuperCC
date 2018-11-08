package emulator;

import game.Level;
import game.Position;
import game.SaveState;
import util.ByteList;
import util.TreeNode;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Stack;

import static game.SaveState.COMPRESSED;
import static game.SaveState.RLE_END;
import static game.SaveState.RLE_MULTIPLE;

public class SavestateManager {
    
    private HashMap<Integer, TreeNode<byte[]>> savestates = new HashMap<>();
    private HashMap<Integer, ByteList> savestateMoves = new HashMap<>();
    private TreeNode<byte[]> currentNode;
    private ByteList currentMoves;
    private SavestateCompressor compressor;

    public void addRewindState(Level level){
        currentNode = new TreeNode<>(level.save(), currentNode);
        compressor.add(currentNode);
    }
    
    public void rewind(){
        if (currentNode.hasParent()) {
            if (!SaveState.getChip(currentNode.getData()).isDead()) currentMoves.removeLast();
            currentNode = currentNode.getParent();
        }
    }
    
    public void addSavestate(int key){
        savestates.put(key, currentNode);
        savestateMoves.put(key, currentMoves.clone());
    }
    
    public boolean load(int key, Level level){
        TreeNode<byte[]> loadedNode = savestates.get(key);
        if (loadedNode == null) return false;
        currentNode = loadedNode;
        currentMoves = savestateMoves.get(key).clone();
        level.load(currentNode.getData());
        level.setMoves(currentMoves);
        return true;
    }
    
    public byte[] getSavestate(){
        return currentNode.getData();
    }
    
    public byte[] getStartingState() {
        TreeNode<byte[]> state = currentNode;
        while (state.hasParent()) state = state.getParent();
        return state.getData();
    }
    
    public ByteList getMoves(){
        return currentMoves;
    }

    public TreeNode<byte[]> getNode(){
        return currentNode;
    }
    
    public SavestateManager(Level level){
        currentNode = new TreeNode<>(level.save(), null);
        currentMoves = level.getMoves();
        compressor = new SavestateCompressor();
    }
    
    public LinkedList<Position> getChipHistory(){
        TreeNode<byte[]> n = currentNode;
        LinkedList<Position> history = new LinkedList<>();
        history.add(SaveState.getChip(n.getData()).getPosition());
        while (n.hasParent()){
            n = n.getParent();
            history.add(SaveState.getChip(n.getData()).getPosition());
        }
        return history;
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
            System.arraycopy(uncompressedState, LAYER_FG_END, out, 3 + list.size(), uncompressedState.length - LAYER_FG_END);
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
