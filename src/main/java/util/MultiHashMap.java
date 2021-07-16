package util;

import java.util.*;

//Its basically a MultiMap except it can be used as a normal map without issue
public class MultiHashMap<K, V> implements Map<K, V> {
    HashMap<K, List<V>> map;

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        for (List<V> valList : map.values()) {
            for (V val : valList) {
                if (val.equals(value))
                    return true;
            }
        }
        return false;
    }

    @Override
    public V get(Object key) {
        List<V> list = map.get(key);
        if (list == null)
            return null;
        return map.get(key).get(0);
    }

    public List<V> getList(K key) {
        return map.get(key);
    }

    @Override
    public V put(K key, V value) {
        List<V> list = map.get(key);
        if (list == null) {
            list = new ArrayList<>();
            list.add(value);
            map.put(key, list);
            return null;
        }
        list.add(value);
        return list.get(0);
    }

    public List<V> putList(K key, List<V> value) {
        List<V> list = map.get(key);
        map.put(key, value);
        return list;
    }

    @Override
    public V remove(Object key) {
        List<V> list = map.get(key);
        V value = list.get(0);
        list.remove(value);
        return value;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        for (var key : m.keySet()) {
            put(key, m.get(key));
        }
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<V> values() {
        ArrayList<V> values = new ArrayList<>(map.size());
        for (List<V> list : map.values()) {
            values.add(list.get(0));
        }
        return values;
    }

    public Collection<List<V>> rawValues() {
        return map.values();
    }

    public List<V> allValues() {
        ArrayList<V> list = new ArrayList<>(map.size());
        for (List<V> vals : map.values()) {
            list.addAll(vals);
        }
        return list;
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        throw new UnsupportedOperationException("The entrySet() method is not supported for MultiHashMap objects");
    }

    public MultiHashMap(int initialCapacity, float loadFactor) {
        map = new HashMap<>(initialCapacity, loadFactor);
    }

    public MultiHashMap(int initialCapacity) {
        map = new HashMap<>(initialCapacity);
    }

    public MultiHashMap() {
        map = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    public MultiHashMap(MultiHashMap<K, V> other) {
        map = new HashMap<>((Map<K, List<V>>) other.map.clone());
    }
}
