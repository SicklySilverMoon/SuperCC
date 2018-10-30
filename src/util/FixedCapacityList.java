package util;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;

public class FixedCapacityList<E> extends AbstractList<E> implements List<E>, RandomAccess {
    
    private Object[] list;
    private int size = 0;
    private final int capacity;
    
    public FixedCapacityList(int capacity){
        this.capacity = capacity;
        this.list = new Object[capacity];
    }
    
    private FixedCapacityList(Object[] list, int size){
        this.list = list;
        this.capacity = list.length;
        this.size = size;
    }
    
    @Override
    public int size() {
        return size;
    }
    
    @Override
    public boolean isEmpty() {
        return size == 0;
    }
    
    @Override
    public boolean contains(Object o) {
        for (Object other: list) if (o == other) return true;
        return false;
    }
    
    @Override
    public Iterator<E> iterator() {
        return new Iterator<>() {
        
            private int i = 0;
        
            @Override
            public boolean hasNext() {
                return i < size;
            }
        
            @Override
            public E next() {
                return (E) list[i++];
            }
            
        };
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void forEach(Consumer<? super E> action) {
        for (int i = 0; i < size; i++)
            action.accept((E) list[i]);
    }
    
    @Override
    public Object[] toArray() {
        Object[] a = new Object[size];
        System.arraycopy(list, 0, a, 0, size);
        return a;
    }
    
    @Override
    public <T> T[] toArray(T[] a) {
        System.arraycopy(list, 0, a, 0, size);
        return a;
    }
    
    @Override
    public <T> T[] toArray(IntFunction<T[]> generator) {
        throw new UnsupportedOperationException("Not implemented");
    }
    
    @Override
    public boolean add(E e) {
        list[size++] = e;
        return true;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public E get(int index) {
        if (index > size) throw new IndexOutOfBoundsException("Index "+index+" out of bounds for list of size "+size);
        return (E) list[index];
    }
    
    @Override
    public boolean containsAll(Collection<?> c) {
        return false;
    }
    
    @Override
    public boolean addAll(Collection<? extends E> c) {
        for (E element : c) add(element);
        return true;
    }
    
    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        System.arraycopy(list, index, list, index + c.size(), size-index);
        for (E element : c) list[index++] = element;
        return true;
    }
    
    @Override
    public boolean remove(Object e){
        int i = indexOf(e);
        if (i == -1) return false;
        System.arraycopy(list, i+1, list, i, size-i-1);
        size--;
        return true;
    }
    
    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not implemented");
    }
    
    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not implemented");
    }
    
    @Override
    public void clear() {
        size = 0;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public E set(int index, E element) {
        return (E) (list[index] = element);
    }
    
    @Override
    public void add(int index, E element) {
        System.arraycopy(list, index, list, index + 1, size-index);
        list[index] = element;
    }
    
    @Override
    public int indexOf(Object o) {
        for (int i = 0; i < size; i++){
            if (list[i] == o) return i;
        }
        return -1;
    }
    
    @Override
    public int lastIndexOf(Object o) {
        for (int i = size-1; i >= 0; i--){
            if (list[i] == o) return i;
        }
        return -1;
    }
    
    @Override
    public ListIterator<E> listIterator() {
        throw new UnsupportedOperationException("Not implemented");
    }
    
    @Override
    public ListIterator<E> listIterator(int index) {
        throw new UnsupportedOperationException("Not implemented");
    }
    
    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        return new FixedCapacityList<E>(Arrays.copyOfRange(list, fromIndex, toIndex), size);
    }
    
}
