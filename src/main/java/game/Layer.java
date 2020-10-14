package game;

/**
 *
 *
 * Benchmarks:
 *
 * Time taken to run pain, without writing savesates:
 * ByteLayer: 12.42920485ms
 * TileLayer: 12.07655951ms
 *
 * Time taken to run pain, with writing savesates:
 * ByteLayer: 20.73663555ms
 * TileLayer: 26.42774384ms
 *
 */

public interface Layer extends Iterable<Tile> {
    
    public Tile get(int i);
    
    public Tile get(Position p);
    
    public void set(int i, Tile t);
    
    public void set(Position p, Tile t);
    
    public byte[] getBytes();
    
    public Tile[] getTiles();
    
    public void load(byte[] b);
    
}
