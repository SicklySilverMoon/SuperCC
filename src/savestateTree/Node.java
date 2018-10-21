package savestateTree;

/**
 * A node of the savestate tree. This consists of a game and a pointer to its
 * parent.
 */
class Node{

    private byte[] savestate;
    private Node parent;
    public int branch;
    public int nodeIndex;
    public int parentIndex;

    //TODO
    public int getDepth(){
        return 1;
    }

    public byte[] getSavestate(){
        return savestate;
    }

    public Node getParent(){
        if (parent != null) return parent;
        else return this;
    }

    public Node(byte[] savestate, Node parent){
        this.parent = parent;
        this.savestate = savestate;
        this.branch = branch;
    }

}
