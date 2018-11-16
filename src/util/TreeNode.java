package util;

import java.util.LinkedList;

public class TreeNode<T> {
    
    private T object;
    private TreeNode<T> parent;
    
    public T getData(){
        return object;
    }
    
    public void setData(T object){
        this.object = object;
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
    
    public TreeNode(T object, TreeNode<T> parent){
        this.parent = parent;
        this.object = object;
    }
    
}
