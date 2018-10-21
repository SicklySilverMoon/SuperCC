package savestateTree;

import game.Level;

import java.util.HashMap;

public class Tree{

    private HashMap<Integer, Node> leaves;
    private Node currentNode;

    public void addSaveState(byte[] savestate){
        currentNode = new Node(savestate, currentNode);
    }
    public void addSaveState(byte[] savestate, int branch){
        leaves.put(branch, currentNode);
    }

    public byte[] rewind(){
        currentNode = currentNode.getParent();
        return currentNode.getSavestate();
    }

    public byte[] load(int key){
        currentNode = leaves.getOrDefault(key, currentNode);
        return currentNode.getSavestate();
    }

    public Tree(Level startingLevel){
        this.leaves = new HashMap<>();
        currentNode = new Node(startingLevel.save(), null);
    }

}
