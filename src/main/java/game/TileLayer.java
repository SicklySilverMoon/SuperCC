package game;

import java.util.Iterator;
import java.util.function.Consumer;

public class TileLayer implements Layer {
    
    private final Tile[] tiles;

    public Tile get(int i){
        return tiles[i];
    }

    public Tile get(Position p){
        if (!p.isValid()) return Tile.WALL;
        return get(p.getIndex());
    }
    
    public void set(int i, Tile t) {
        tiles[i] = t;
    }
    
    public void set(Position p, Tile t) {
        tiles[p.getIndex()] = t;
    }
    
    public byte[] getBytes() {
        byte[] out = new byte[32*32];
        for (int i = 0; i < 32 * 32; i++) {
            out[i] = (byte) get(i).ordinal();
        }
        return out;
    }
    
    public Tile[] getTiles() {
        return tiles;
    }
    
    public void load(byte[] b) {
        for (int i = 0; i < 32 * 32; i++) {
            tiles[i] = Tile.fromOrdinal(b[i] & 0xFF);
        }
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
        for (Tile t : tiles) action.accept(t);
    }
    
    public TileLayer(Tile[] tiles) {
        this.tiles = tiles;
    }
    
    public TileLayer(byte[] tiles) {
        this.tiles = new Tile[32*32];
        for (int i = 0; i < 32*32; i++) {
            this.tiles[i] = Tile.fromOrdinal(tiles[i]);
        }
    }
    
}
