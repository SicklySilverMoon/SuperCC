package game;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;

public class Moves implements Iterable<Byte>{
    
    public static final int INITIAL_CAPACITY = 200;

    private byte[] moves = new byte[INITIAL_CAPACITY];
    private int size = 0;
    private int capacity = INITIAL_CAPACITY;
    
    public void add(byte b){
        moves[size++] = b;
        if (size == capacity){
            capacity *= 2;
            moves = Arrays.copyOf(moves, capacity);
        }
    }
    
    public void remove(){
        size--;
    }
    
    public byte[] getMoves(){
        return Arrays.copyOf(moves, size);
    }
    
    public Moves(){};
    
    private Moves(byte[] moves, int size, int capacity){
        this.moves = moves;
        this.size = size;
        this.capacity = capacity;
    }
    
    @Override
    public Iterator<Byte> iterator() {
        Iterator<Byte> it = new Iterator<Byte>() {
            
            private int i = 0;
            
            @Override
            public boolean hasNext() {
                return i < size;
            }
            
            @Override
            public Byte next() {
                return moves[i++];
            }
            
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
        return it;
    }
    
    @Override
    public String toString(){
        return new String(moves, 0, size, StandardCharsets.ISO_8859_1);
    }
    
    @Override
    public Moves clone(){
        return new Moves(Arrays.copyOf(moves, capacity), size, capacity);
    }
    
    public static void main(String[] args){
        Moves m1 = new Moves();
        m1.add((byte) '1');
        m1.add((byte) '2');
        Moves m2 = m1.clone();
        m1.add((byte) '3');
        System.out.println(m1);
        System.out.println(m2);
    }

}
