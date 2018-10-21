package savestateTree;

/**
 * A node of the savestate tree. This consists of a game and a pointer to its
 * parent.
 */
class Node{

    private byte[] savestate;
    private Node parent;

    byte[] getSavestate(){
        return savestate;
    }

    Node getParent(){
        if (parent != null) return parent;
        else return this;
    }

    Node(byte[] savestate, Node parent){
        this.parent = parent;
        this.savestate = savestate;
    }

}
