package game;

import java.util.Iterator;
import java.util.function.Consumer;

public class ByteLayer implements Layer {
    
    private final byte[] layer;
    
    public Tile get(int i){
        if (i >= 0 && i < 32*32) return Tile.fromOrdinal(layer[i]);
        return Tile.WALL;
    }
    
    public Tile get(Position p){
        return get(p.getIndex());
    }
    
    public void set(int i, Tile t){
        layer[i] = (byte) t.ordinal();
    }
    
    public void set(Position p, Tile t){
        set(p.getIndex(), t);
    }
    
    public byte[] getBytes() {
        return layer;
    }
    
    public Tile[] getTiles() {
        Tile[] out = new Tile[32*32];
        for (int i = 0; i < 32 * 32; i++) {
            out[i] = get(i);
        }
        return out;
    }
    
    public void load(byte[] b) {
        System.arraycopy(b, 0, layer, 0, layer.length);
    }
    
    public ByteLayer(byte[] layer){
        this.layer = layer;
    }
    
    public Iterator<Tile> iterator() {
        return new Iterator<Tile>() {
            private int i;
            
            @Override
            public boolean hasNext() {
                return i < 32*32;
            }
    
            @Override
            public Tile next() {
                return get(i++);
            }
        };
    }
    
    public void forEach(Consumer<? super Tile> action) {
        for (byte b : layer) action.accept(Tile.fromOrdinal(b));
    }
    
}
