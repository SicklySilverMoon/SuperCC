package savestateTree;

/**
 * A node of the savestate tree. This consists of a savestate and a pointer to
 * its parent.
 */
class Node{

    private byte[] savestate;
    private Node parent;

    byte[] getSavestate(){
        return savestate;
    }

    Node getParent(){
        if (hasParent()) return parent;
        else return this;
    }
    
    boolean hasParent(){
        return parent != null;
    }

    Node(byte[] savestate, Node parent){
        this.parent = parent;
        this.savestate = savestate;
    }

}
