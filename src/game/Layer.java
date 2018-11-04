package game;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

public class Layer implements Iterable<Tile> {
    
    private final byte[] layer;
    
    public Tile get(int i){
        return Tile.fromOrdinal(layer[i]);
    }
    
    public Tile get(Position p){
        return get(p.getIndex());
    }
    
    public byte[] getLayer(){
        return layer;
    }
    
    public void set(int i, Tile t){
        layer[i] = (byte) t.ordinal();
    }
    
    public void set(Position p, Tile t){
        set(p.getIndex(), t);
    }
    
    public Layer(byte[] layer){
        this.layer = layer;
    }
    
    public Layer(){
        this.layer = new byte[32*32];
    }
    
    @Override
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
    
    @Override
    public void forEach(Consumer<? super Tile> action) {
        throw new UnsupportedOperationException("not implemented");
    }
    
    @Override
    public Spliterator<Tile> spliterator() {
        throw new UnsupportedOperationException("not implemented");
    }
}
