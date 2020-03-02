package tools.variation;

import java.util.HashMap;
import java.util.Objects;

public class MovePool {
    public HashMap<String, Integer> moves = new HashMap<>();
    public int size = 0;

    public MovePool() {

    }

    public void add(Move move) {
        if(moves.get(move.move) == null) {
            moves.put(move.move, move.number);
            size += move.number;
            return;
        }
        int value = moves.get(move.move);
        moves.put(move.move, value + move.number);
        size += move.number;
    }

    public void add(MovePool movePool) {
        for(String key : movePool.moves.keySet()) {
            int keyValue = movePool.moves.get(key);
            if(moves.get(key) == null) {
                moves.put(key, keyValue);
            }
            else {
                int value = moves.get(key);
                moves.put(key, value + keyValue);
            }
            size += keyValue;
        }
    }

    public void clear() {
        moves.clear();
        size = 0;
    }

    public void replace(MovePool movePool) {
        clear();
        add(movePool);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MovePool movePool = (MovePool) o;
        return size == movePool.size &&
                Objects.equals(moves, movePool.moves);
    }

    @Override
    public int hashCode() {
        return Objects.hash(moves, size);
    }
}
