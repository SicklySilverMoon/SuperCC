package emulator;

import util.ByteList;
import util.TreeNode;

import java.util.Stack;

import static game.SaveState.*;

public class SavestateCompressor implements Runnable{

    private static final int LAYER_BG_LOCATION = 3,
            LAYER_FG_LOCATION = LAYER_BG_LOCATION + 32 * 32,
            LAYER_FG_END = LAYER_FG_LOCATION + 32 * 32;

    private Stack<TreeNode<byte[]>> uncompressedSavestates;
    private ByteList list;

    private final Thread thread;

    void add(TreeNode<byte[]> n){
        uncompressedSavestates.add(n);
        synchronized(thread) {
            thread.notify();
        }
    }

    @Override
    public void run(){
        while (true) {
            try {
                if (uncompressedSavestates.isEmpty()) {
                    synchronized (thread) {
                        thread.wait();
                    }
                }
                else {
                    compress(uncompressedSavestates.pop());
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void rleCompress(byte[] uncompressed, ByteList out, int startIndex, int length){
        int lastOrdinal = uncompressed[startIndex];
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
        byte[] out = new byte[uncompressedState.length - 2 * 32 * 32 + list.size()];
        out[0] = COMPRESSED_V2;
        out[1] = uncompressedState[1];
        out[2] = uncompressedState[2];
        list.copy(out, 3);
        System.arraycopy(uncompressedState, LAYER_FG_END, out, 3+list.size(), uncompressedState.length - 2 * 32 * 32 - 3);
        n.setData(out);
    }

    void initialise() {
        uncompressedSavestates = new Stack<>();
        list = new ByteList();
    }

    SavestateCompressor(){
        initialise();
        thread = new Thread(this);
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }

}
