package savestateTree;

import game.Level;
import game.Moves;
import game.Position;
import game.SaveState;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

public class Tree{

    private HashMap<Integer, Node> leafStates = new HashMap<>();
    private HashMap<Integer, Moves> leafMoves = new HashMap<>();
    private Node currentNode;
    private Moves moves;

    public void addRewindState(byte[] savestate, Moves moves){
        currentNode = new Node(savestate, currentNode);
    }
    public void addSavestate(byte[] savestate, Moves moves, int branch){
        leafStates.put(branch, new Node(savestate, currentNode.getParent()));
        leafMoves.put(branch, moves.clone());
    }

    public void rewind(){
        if (currentNode.hasParent()) {
            currentNode = currentNode.getParent();
            moves.remove();
        }
    }

    public void load(int key){
        currentNode = leafStates.getOrDefault(key, currentNode);
        moves = leafMoves.getOrDefault(key, moves).clone();
    }
    
    public byte[] getSavestate(){
        return currentNode.getSavestate();
    }
    
    public Moves getMoves(){
        return moves;
    }

    public Tree(Level startingLevel){
        currentNode = new Node(startingLevel.save(), null);
        moves = startingLevel.getMoves();
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
