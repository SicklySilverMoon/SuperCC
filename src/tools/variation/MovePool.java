package tools.variation;

import java.util.HashMap;

public class MovePool {
    public HashMap<String, Integer> moves = new HashMap<>();

    public MovePool() {

    }

    public void add(Move move) {
        if(moves.get(move.move) == null) {
            moves.put(move.move, move.number);
            return;
        }
        int value = moves.get(move.move);
        moves.put(move.move, value + move.number);
    }
}
