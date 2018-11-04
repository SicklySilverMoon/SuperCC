package util;

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
    
    public TreeNode(T object, TreeNode<T> parent){
        this.parent = parent;
        this.object = object;
    }
    
}
