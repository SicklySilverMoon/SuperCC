package util;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.*;

/**
 * This class is basically an ArrayList for the byte primitive, used for
 * storing moves. The reason I don't just use ArrayList[Byte] is to make
 * copying and String conversions simpler and faster.
 *
 * The initial capacity is set to 200. This doubles when it is reached. The
 * capacity never decreases.
 */
public class ByteList implements Iterable<Byte>, RandomAccess, Serializable {
    
    private static final int INITIAL_CAPACITY = 200;
    
    private byte[] bytes = new byte[INITIAL_CAPACITY];
    private int size = 0;
    private int capacity = INITIAL_CAPACITY;

    /**
     * Appends the specified element to the end of this list.
     * @param b byte to be appended to this list
     */
    public void add(byte b){
        bytes[size++] = b;
        if (size == capacity){
            capacity *= 2;
            bytes = Arrays.copyOf(bytes, capacity);
        }
    }
    
    /**
     * Appends the specified element to the end of this list.
     * @param n int to be appended to this list
     */
    public void add(int n){
        add((byte) n);
    }
    
    public byte get(int index) {
        return bytes[index];
    }
    
    public void set(int index, byte b) {
        bytes[index] = b;
    }
    
    /**
     * Removes the element at the end of this list.
     */
    public void removeLast(){
        size--;
    }
    
    /**
     * Returns an array containing all of the elements in this list in proper
     * sequence (from first to last element).
     *
     * The returned array will be "safe" in that no references to it are
     * maintained by this list. (In other words, this method must allocate a
     * new array). The caller is thus free to modify the returned array.
     *
     * This method acts as bridge between array-based and collection-based APIs.
     *
     * @return an array containing all of the elements in this list in proper
     * sequence
     */
    public byte[] toArray(){
        return Arrays.copyOf(bytes, size);
    }
    
    public void copy(byte[] dest, int destPos){
        System.arraycopy(bytes, 0, dest, destPos, size);
    }
    
    public void copy(int srcPos, byte[] dest, int destPos, int length){
        System.arraycopy(bytes, srcPos, dest, destPos, length);
    }

    public ByteList sublist(int from, int to) {
        return new ByteList(Arrays.copyOfRange(bytes, from, to+1), to-from, capacity);
    }
    
    /**
     * Returns the number of elements in this list.
     * @return the number of elements in this list
     */
    public int size(){
        return size;
    }
    
    /**
     * The default constructor for ByteList. The initial capacity is set to
     * INITIAL_CAPACITY.
     */
    public ByteList(){};
    
    // Constructor used for cloning
    private ByteList(byte[] moves, int size, int capacity){
        this.bytes = moves;
        this.size = size;
        this.capacity = capacity;
    }
    
    /**
     * Removes all of the elements from this list (optional operation). The
     * list will be empty after this call returns.
     */
    public void clear(){
        size = 0;
    }

    /**
     * Reverses the order of the elements in the specified list.<p>
     * Just a slight modification of Collections.reverse as it can't be run on a ByteList
     */
    public void reverse() {
        for (int i=0, mid=size>>1, j=size-1; i<mid; i++, j--)
            swap(i, j);
    }

    /**
     * Swaps the elements at the specified positions in the specified list.
     */
    private void swap(int i, int j) {
        byte a = this.get(i);
        byte b = this.get(j);
        this.set(i, b);
        this.set(j, a);
    }

    /**
     * Returns an iterator over the elements in this list in proper sequence.
     * @return an iterator over the elements in this list in proper sequence
     */
    @Override
    public Iterator<Byte> iterator() {
        return new ListItr(false);
    }

    /**
     * Returns a ListIterator over the elements in this list in proper sequence (ascending or descending).
     * @return a ListIterator over the elements in this list in proper sequence (ascending or descending).
     * @param reverse if the list should be iterated in ascending (false) or descending (true) order.
     */
    public ListIterator<Byte> listIterator(boolean reverse){
        return new ListItr(reverse);
    }

    private class ListItr implements java.util.ListIterator<Byte> {
        private int cursor;
        ListItr(boolean reverse) {
            if (reverse) cursor = size;
            else cursor = 0;
        }

        @Override
        public boolean hasNext() {
            return cursor < size;
        }

        @Override
        public Byte next() {
            if (cursor >= size) throw new NoSuchElementException();
            return bytes[cursor++];
        }

        @Override
        public boolean hasPrevious() {
            return cursor > 0;
        }

        @Override
        public Byte previous() {
            if ((cursor - 1) < 0) throw new NoSuchElementException();
            return bytes[--cursor];
        }

        @Override
        public int nextIndex() {
            return cursor;
        }

        @Override
        public int previousIndex() {
            return cursor-1;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(Byte aByte) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(Byte aByte) {
            throw new UnsupportedOperationException();
        }
    }
    
    @Override
    public ByteList clone(){
        return new ByteList(Arrays.copyOf(bytes, capacity), size, capacity);
    }
    
    /**
     * Converts the bytes into a String.
     * Use of this method is not recommended - use toString(Charset charset)
     * instead.
     * @return The bytes as a string
     */
    @Override
    public String toString(){
        return new String(bytes, 0, size);
    }
    
    /**
     * Converts the bytes into a String, using a user-defined charset encoding.
     * @param charset The charset to encode to
     * @param size The length of the list to copy
     * @return The bytes as a string
     */
    public String toString(Charset charset, int size){
        return new String(bytes, 0, size, charset);
    }

}
