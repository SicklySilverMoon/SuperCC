package tools.variation;

import java.util.HashMap;

public class MovePool {
    public HashMap<Character, Integer> moves = new HashMap<>();

    public MovePool() {
        moves.put('u', 0);
        moves.put('r', 0);
        moves.put('d', 0);
        moves.put('l', 0);
        moves.put('w', 0);
        moves.put('h', 0);
    }

    public void add(Move move) {
        int value = moves.get(move.move);
        moves.put(move.move, value + move.number);
    }
}
