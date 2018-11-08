package util;

import java.util.ArrayList;

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
    
    public ArrayList<TreeNode<T>> getHistory() {
        return getHistoryRecursion(this, new ArrayList<TreeNode<T>>());
    }
    
    private ArrayList<TreeNode<T>> getHistoryRecursion(TreeNode<T> state, ArrayList<TreeNode<T>> list) {
        if (state.hasParent()) getHistoryRecursion(state, list);
        list.add(state);
        return list;
    }
    
    public TreeNode(T object, TreeNode<T> parent){
        this.parent = parent;
        this.object = object;
    }
    
}
