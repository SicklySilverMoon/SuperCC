package savestateTree;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Writer{

    private static final int MAGIC_NUMBER = 0x20FA3390;

    private Tree tree;

    private static int max(int[] a){
        int max = 0;
        for (int n : a) if (n > max) max = n;
        return max;
    }

    private static ArrayList<Node> getDepthSortedNodes(HashMap<Integer, Node> endPoints){
        Integer[] keys = (Integer[]) endPoints.keySet().toArray();
        int width = keys.length;
        int[] depths = new int[width];
        for (int i = 0; i < width; i++){
            depths[i] = endPoints.get(keys[i]).getDepth();
        }
        int maxDepth = max(depths);

        ArrayList<Node> depthSortedNodes = new ArrayList<Node>(10000);

        int currentDepth = maxDepth;
        ArrayList<Node> nodesAtThisDepth = new ArrayList<>();
        for (int i = 0; i < width; i++) {
            if (depths[i] == maxDepth) {
                nodesAtThisDepth.add(endPoints.get(i));
                depthSortedNodes.add(endPoints.get(i));
            }

            while (currentDepth >= 0) {
                ArrayList<Node> nodesAtNextDepth = new ArrayList<>();
                for (i = 0; i < width; i++) {
                    if (depths[i] == currentDepth - 1) nodesAtNextDepth.add(endPoints.get(i));
                }
                for (Node n : nodesAtThisDepth) {
                    depthSortedNodes.add(n);
                    if (currentDepth > 0 && !nodesAtNextDepth.contains(n.getParent()))
                        nodesAtNextDepth.add(n.getParent());
                }
            }
        }
        return depthSortedNodes;
    }

    private static void writeNodeIndexes(ArrayList<Node> sortedNodes){
        int len = sortedNodes.size();
        sortedNodes.get(len-1).nodeIndex = 0;
        sortedNodes.get(len-1).parentIndex = -1;
        for (int i = 1; i < len; i++){
            Node node = sortedNodes.get(len-1-i);
            node.nodeIndex = i;
            node.parentIndex = node.getParent().nodeIndex;
        }
    }

    public void write(String filePath) throws IOException {
        ArrayList<Node> sortedNodes = getDepthSortedNodes(tree.leaves);
        writeNodeIndexes(sortedNodes);
        FileOutputStream outStream = new FileOutputStream(new File(filePath));
        outStream.write(sortedNodes.size());
        for (Node n : sortedNodes){
            outStream.write(n.branch);
            outStream.write(n.nodeIndex);
            outStream.write(n.parentIndex);
            outStream.write(n.getSavestate());
        }
    }

    public Writer(Tree t){
        this.tree = t;
    }

}
