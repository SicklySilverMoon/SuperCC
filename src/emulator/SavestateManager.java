package emulator;

import game.Level;
import game.Position;
import game.SaveState;
import util.ByteList;
import util.TreeNode;

import java.security.KeyException;
import java.util.HashMap;
import java.util.LinkedList;

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

}
