package emulator;

import game.Level;
import game.Position;
import game.SaveState;
import util.ByteList;
import util.TreeNode;

import java.util.HashMap;
import java.util.LinkedList;

public class SavestateManager {
    
    private HashMap<Integer, TreeNode<byte[]>> savestates = new HashMap<>();
    private HashMap<Integer, ByteList> savestateMoves = new HashMap<>();
    private TreeNode<byte[]> currentNode;
    private ByteList currentMoves;

    public void addRewindState(byte[] savestate){
        currentNode = new TreeNode<>(savestate, currentNode);
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
    
    public void load(int key, Level level){
        currentNode = savestates.getOrDefault(key, currentNode);
        currentMoves = savestateMoves.getOrDefault(key, currentMoves).clone();
        level.load(currentNode.getData());
        level.setMoves(currentMoves);
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
