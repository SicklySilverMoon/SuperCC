package savestateTree;

import game.Level;
import game.Position;
import game.SaveState;

import java.util.HashMap;
import java.util.LinkedList;

public class Tree{

    private HashMap<Integer, Node> leaves;
    private Node currentNode;

    public void addSaveState(byte[] savestate){
        currentNode = new Node(savestate, currentNode);
    }
    public void addSaveState(byte[] savestate, int branch){
        leaves.put(branch, new Node(savestate, currentNode.getParent()));
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
    
    public LinkedList<Position> getChipHistory(){
        Node n = currentNode;
        LinkedList<Position> history = new LinkedList<>();
        history.add(SaveState.getChip(n.getSavestate()).getPosition());
        while (n.hasParent()){
            n = n.getParent();
            history.add(SaveState.getChip(n.getSavestate()).getPosition());
        }
        return history;
    }

}
