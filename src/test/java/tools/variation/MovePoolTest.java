package tools.variation;

import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class MovePoolTest {
    @Test
    void addSingleMove() {
        MovePool movePool = new MovePool();
        movePool.add(new Move("4ud"));

        HashMap<String, Integer> moves = movePool.moves;

        HashMap<String, Integer> expectedMoves = new HashMap<>();
        expectedMoves.put("ud", 4);

        assertEquals(4, movePool.size);
        assertEquals(expectedMoves, moves);
    }

    @Test
    void addMultipleMoves() {
        MovePool movePool = new MovePool();
        movePool.add(new Move("r"));
        movePool.add(new Move("2d"));
        movePool.add(new Move("3r"));
        movePool.add(new Move("4dd"));

        HashMap<String, Integer> moves = movePool.moves;

        HashMap<String, Integer> expectedMoves = new HashMap<>();
        expectedMoves.put("r", 4);
        expectedMoves.put("d", 2);
        expectedMoves.put("dd", 4);

        assertEquals(10, movePool.size);
        assertEquals(expectedMoves, moves);
    }

    @Test
    void addMovePool() {
        MovePool otherMovePool = new MovePool();
        otherMovePool.add(new Move("2ud"));
        otherMovePool.add(new Move("3w"));
        MovePool movePool = new MovePool();
        movePool.add(new Move("4ud"));
        movePool.add(otherMovePool);

        HashMap<String, Integer> moves = movePool.moves;

        HashMap<String, Integer> expectedMoves = new HashMap<>();
        expectedMoves.put("ud", 6);
        expectedMoves.put("w", 3);

        assertEquals(9, movePool.size);
        assertEquals(expectedMoves, moves);
    }
}