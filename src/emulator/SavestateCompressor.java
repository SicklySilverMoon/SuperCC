package emulator;

import game.Tile;
import util.ByteList;
import util.TreeNode;

import java.util.Stack;

import static game.SaveState.COMPRESSED;
import static game.SaveState.RLE_END;
import static game.SaveState.RLE_MULTIPLE;

public class SavestateCompressor {
    
    private static final int LAYER_BG_LOCATION = 4,
                             LAYER_FG_LOCATION = LAYER_BG_LOCATION + 32 * 32,
                             LAYER_FG_END = LAYER_FG_LOCATION + 32 * 32;
    
    private final Stack<TreeNode<byte[]>> stateStack;
    private final ByteList list;
    
    public void add(TreeNode<byte[]> n){
        stateStack.add(n);
    }
    
    private void run(){
        try {
            if (stateStack.isEmpty()) {
                Thread.sleep(1);
            }
            else {
                compress(stateStack.pop());
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    
    private void rleCompress(byte[] uncompressed, ByteList out, int startIndex, int length){
        int lastOrdinal = uncompressed[0];
        int ordinal;
        int copyCount = -1;
        for (int i = startIndex; i < startIndex + length; i++) {
            ordinal = uncompressed[i];
            if (ordinal == lastOrdinal){
                if (copyCount == 255){
                    out.add(RLE_MULTIPLE);
                    out.add(copyCount);
                    copyCount = 0;
                    out.add(ordinal);
                }
                else copyCount++;
            }
            else {
                if (copyCount != 0){
                    out.add(RLE_MULTIPLE);
                    out.add(copyCount);
                }
                out.add(lastOrdinal);
                copyCount = 0;
                lastOrdinal = ordinal;
            }
        }
        if (copyCount != 0){
            out.add(RLE_MULTIPLE);
            out.add(copyCount);
        }
        out.add(lastOrdinal);
        out.add(RLE_END);
    }
    
    private void compress(TreeNode<byte[]> n){
        list.clear();
        byte[] uncompressedState = n.getData();
        rleCompress(uncompressedState, list, LAYER_BG_LOCATION, 32*32);
        rleCompress(uncompressedState, list, LAYER_FG_LOCATION, 32*32);
        byte[] out = new byte[uncompressedState.length - 32*32 + list.size()];
        out[0] = COMPRESSED;
        out[1] = uncompressedState[1];
        out[2] = uncompressedState[2];
        list.copy(out, 3);
        System.arraycopy(uncompressedState, LAYER_FG_END, out, 3, uncompressedState.length - LAYER_FG_END);
        n.setData(out);
    }
    
    private void Layer(Tile[] layer){
    }

    public SavestateCompressor(){
        stateStack = new Stack<>();
        list = new ByteList();
        Thread t = new Thread(() -> run());
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }

}
