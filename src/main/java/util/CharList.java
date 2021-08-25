package util;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.*;

/**
 * This class is basically an ArrayList for the char primitive, used for
 * storing moves. The reason we don't just use ArrayList[Char] is to make
 * copying and String conversions simpler and faster.
 *
 * The initial capacity is set to 200. This doubles when it is reached. The
 * capacity never decreases.
 */
public class CharList implements Iterable<Character>, RandomAccess, Serializable {

    private static final int INITIAL_CAPACITY = 200;

    private char[] chars = new char[INITIAL_CAPACITY];
    private int size = 0;
    private int capacity = INITIAL_CAPACITY;

    /**
     * Appends the specified element to the end of this list.
     * @param c char to be appended to this list
     */
    public void add(char c){
        chars[size++] = c;
        if (size == capacity){
            capacity *= 2;
            chars = Arrays.copyOf(chars, capacity);
        }
    }

    /**
     * Appends the specified element to the end of this list.
     * @param n int to be appended to this list
     */
    public void add(int n){
        add((char) n);
    }

    public char get(int index) {
        return chars[index];
    }

    public void set(int index, char c) {
        chars[index] = c;
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
    public char[] toArray(){
        return Arrays.copyOf(chars, size);
    }

    public void copy(char[] dest, int destPos){
        System.arraycopy(chars, 0, dest, destPos, size);
    }

    public void copy(int srcPos, char[] dest, int destPos, int length){
        System.arraycopy(chars, srcPos, dest, destPos, length);
    }

    public CharList sublist(int from, int to) {
        return new CharList(Arrays.copyOfRange(chars, from, to+1), to-from, capacity);
    }

    /**
     * Returns the number of elements in this list.
     * @return the number of elements in this list
     */
    public int size(){
        return size;
    }

    /**
     * The default constructor for CharList. The initial capacity is set to
     * INITIAL_CAPACITY.
     */
    public CharList(){};

    public CharList(char[] chars) {
        this.chars = chars;
        this.size = chars.length;
        this.capacity = chars.length * 2;
    }

    // Constructor used for cloning
    private CharList(char[] moves, int size, int capacity){
        this.chars = moves;
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
     * Just a slight modification of Collections.reverse
     */
    public void reverse() {
        for (int i=0, mid=size>>1, j=size-1; i<mid; i++, j--)
            swap(i, j);
    }

    /**
     * Swaps the elements at the specified positions in the specified list.
     */
    private void swap(int i, int j) {
        char a = this.get(i);
        char b = this.get(j);
        this.set(i, b);
        this.set(j, a);
    }

    /**
     * Returns an iterator over the elements in this list in proper sequence.
     * @return an iterator over the elements in this list in proper sequence
     */
    @Override
    public Iterator<Character> iterator() {
        return new ListItr(false);
    }

    /**
     * Returns a ListIterator over the elements in this list in proper sequence (ascending or descending).
     * @return a ListIterator over the elements in this list in proper sequence (ascending or descending).
     * @param reverse if the list should be iterated in ascending (false) or descending (true) order.
     */
    public ListIterator<Character> listIterator(boolean reverse){
        return new ListItr(reverse);
    }

    private class ListItr implements ListIterator<Character> {
        private int cursor;
        ListItr(boolean reverse) {
            if (reverse)
                cursor = size;
            else cursor = 0;
        }

        @Override
        public boolean hasNext() {
            return cursor < size;
        }

        @Override
        public Character next() {
            if (cursor >= size) throw new NoSuchElementException();
            return chars[cursor++];
        }

        @Override
        public boolean hasPrevious() {
            return cursor > 0;
        }

        @Override
        public Character previous() {
            if ((cursor - 1) < 0) throw new NoSuchElementException();
            return chars[--cursor];
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
        public void set(Character aByte) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(Character aByte) {
            throw new UnsupportedOperationException();
        }
    }
    
    @Override
    public CharList clone(){
        return new CharList(Arrays.copyOf(chars, capacity), size, capacity);
    }
    
    /**
     * Converts the chars into a String.
     * @return The chars as a string
     */
    @Override
    public String toString(){
        return new String(chars, 0, size);
    }

    /**
     * Coverts the chars into a string from start to provided size.
     * @param size The amount of chars to be converted to a string.
     * @return The chars as a string until provided size.
     */
    public String toString(int size) {
        return new String(chars, 0, size);
    }
}
