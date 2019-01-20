package util;

import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class TreeNode<T> implements Serializable {
    
    private transient T data;
    private transient TreeNode<T> parent;
    
    public T getData(){
        return data;
    }
    
    public void setData(T object){
        this.data = object;
    }
    
    public TreeNode<T> getParent(){
        return parent;
    }
    
    public boolean hasParent(){
        return parent != null;
    }
    
    public LinkedList<TreeNode<T>> getHistory() {
        TreeNode<T> state = this;
        LinkedList<TreeNode<T>> list = new LinkedList<>();
        list.add(this);
        while (state.hasParent()) {
            state = state.getParent();
            list.addFirst(state);
        }
        return list;
    }
    
    public int depth() {
        TreeNode<T> state = this;
        int depth = 1;
        while (state.hasParent()) {
            state = state.getParent();
            depth++;
        }
        return depth;
    }
    
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        LinkedList<TreeNode<T>> history = getHistory();
        out.writeInt(history.size());
        for (TreeNode<T> node : history) {
            out.writeObject(node.data);
        }
    }
    
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        int depth = in.readInt();
        TreeNode<T> node = new TreeNode<>((T) in.readObject(), null);
        for (int i = 1; i < depth; i++) {
            node = new TreeNode<>((T) in.readObject(), node);
        }
        this.data = node.data;
        this.parent = node.parent;
    }
    
    public TreeNode(T object, TreeNode<T> parent){
        this.parent = parent;
        this.data = object;
    }
    
}
