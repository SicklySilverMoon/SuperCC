package tools.variation;

import org.junit.jupiter.api.Test;
import util.ByteList;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class PermutationTest {
    @Test
    void boundWhenBothBoundsNull() {
        MovePool movePoolOptional = new MovePool();
        movePoolOptional.add(new Move("5u"));
        Permutation permutation = new Permutation(movePoolOptional, new MovePool(), null, null, "urdlwh");

        assertEquals(5, permutation.lowerBound);
        assertEquals(5, permutation.upperBound);
    }

    @Test
    void boundWhenUpperBoundNull() {
        MovePool movePoolOptional = new MovePool();
        movePoolOptional.add(new Move("5u"));
        Permutation permutation = new Permutation(movePoolOptional, new MovePool(), 4, null, "urdlwh");

        assertEquals(4, permutation.lowerBound);
        assertEquals(4, permutation.upperBound);
    }

    @Test
    void boundSwap() {
        MovePool movePoolOptional = new MovePool();
        movePoolOptional.add(new Move("5u"));
        Permutation permutation = new Permutation(movePoolOptional, new MovePool(), 5, 3, "urdlwh");

        assertEquals(3, permutation.lowerBound);
        assertEquals(5, permutation.upperBound);
    }

    @Test
    void boundClip() {
        MovePool movePoolOptional = new MovePool();
        movePoolOptional.add(new Move("5u"));
        Permutation permutation = new Permutation(movePoolOptional, new MovePool(), 6, 7, "urdlwh");

        assertEquals(5, permutation.lowerBound);
        assertEquals(5, permutation.upperBound);
    }

    @Test
    void boundClipForced() {
        MovePool movePoolOptional = new MovePool();
        movePoolOptional.add(new Move("5u"));
        MovePool movePoolForced = new MovePool();
        movePoolForced.add(new Move("3d"));
        Permutation permutation = new Permutation(movePoolOptional, movePoolForced, 1, 2, "urdlwh");

        assertEquals(3, permutation.lowerBound);
        assertEquals(3, permutation.upperBound);
    }

    @Test
    void initialPermutation() {
        MovePool movePoolOptional = new MovePool();
        movePoolOptional.add(new Move("r"));
        movePoolOptional.add(new Move("2u"));
        movePoolOptional.add(new Move("3du"));
        Permutation permutation = new Permutation(movePoolOptional, new MovePool(), 6, 6, "urdlwh");

        int[] permutationRaw = permutation.getRawPermutation();

        int[] expectedPermutationRaw = {0, 0, 1, 2, 2, 2};

        assertArrayEquals(expectedPermutationRaw, permutationRaw);
    }

    @Test
    void initialPermutationForced() {
        MovePool movePoolOptional = new MovePool();
        movePoolOptional.add(new Move("r"));
        movePoolOptional.add(new Move("2u"));
        movePoolOptional.add(new Move("3du"));
        MovePool movePoolForced = new MovePool();
        movePoolForced.add(new Move("4lr"));
        Permutation permutation = new Permutation(movePoolOptional, movePoolForced, 8, 10, "urdlwh");

        int[] permutationRaw = permutation.getRawPermutation();

        int[] expectedPermutationRaw = {0, 0, 1, 2, 3, 3, 3, 3};

        assertArrayEquals(expectedPermutationRaw, permutationRaw);
    }

    @Test
    void nextPermutation() {
        MovePool movePoolOptional = new MovePool();
        movePoolOptional.add(new Move("r"));
        movePoolOptional.add(new Move("2u"));
        movePoolOptional.add(new Move("3du"));
        Permutation permutation = new Permutation(movePoolOptional, new MovePool(), 6, 6, "urdlwh");
        permutation.nextPermutation();

        int[] permutationRaw = permutation.getRawPermutation();

        int[] expectedPermutationRaw = {0, 0, 2, 1, 2, 2};

        assertArrayEquals(expectedPermutationRaw, permutationRaw);
    }

    @Test
    void permutationCount() {
        MovePool movePoolOptional = new MovePool();
        movePoolOptional.add(new Move("r"));
        movePoolOptional.add(new Move("2u"));
        movePoolOptional.add(new Move("3du"));
        Permutation permutation = new Permutation(movePoolOptional, new MovePool(), 5, 6, "urdlwh");

        assertEquals(120, permutation.getPermutationCount());
    }

    @Test
    void permutationCountForced() {
        MovePool movePoolOptional = new MovePool();
        movePoolOptional.add(new Move("r"));
        movePoolOptional.add(new Move("2u"));
        MovePool movePoolForced = new MovePool();
        movePoolForced.add(new Move("3du"));
        Permutation permutation = new Permutation(movePoolOptional, movePoolForced, 5, 6, "urdlwh");

        assertEquals(90, permutation.getPermutationCount());
    }

    @Test
    void reset() {
        MovePool movePoolOptional = new MovePool();
        movePoolOptional.add(new Move("5u"));
        Permutation permutation = new Permutation(movePoolOptional, new MovePool(), 4, 5, "urdlwh");
        permutation.nextPermutation();
        permutation.reset();

        int[] permutationRaw = permutation.getRawPermutation();

        int[] expectedPermutationRaw = {0, 0, 0, 0};

        assertArrayEquals(expectedPermutationRaw, permutationRaw);
    }

    @Test
    void terminate() {
        MovePool movePoolOptional = new MovePool();
        movePoolOptional.add(new Move("r"));
        movePoolOptional.add(new Move("2u"));
        movePoolOptional.add(new Move("3du"));
        Permutation permutation = new Permutation(movePoolOptional, new MovePool(), 6, 6, "urdlwh");
        permutation.terminate(0);

        int[] permutationRaw = permutation.getRawPermutation();

        int[] expectedPermutationRaw = {0, 2, 2, 2, 1, 0};

        assertArrayEquals(expectedPermutationRaw, permutationRaw);
    }

    @Test
    void end() {
        MovePool movePoolOptional = new MovePool();
        movePoolOptional.add(new Move("r"));
        movePoolOptional.add(new Move("2u"));
        movePoolOptional.add(new Move("3du"));
        Permutation permutation = new Permutation(movePoolOptional, new MovePool(), 6, 6, "urdlwh");
        permutation.end();

        int[] permutationRaw = permutation.getRawPermutation();

        int[] expectedPermutationRaw = {2, 2, 2, 1, 0, 0};

        assertArrayEquals(expectedPermutationRaw, permutationRaw);
    }

    @Test
    void getPermutation() {
        MovePool movePoolOptional = new MovePool();
        movePoolOptional.add(new Move("r"));
        movePoolOptional.add(new Move("2u"));
        movePoolOptional.add(new Move("3du"));
        Permutation permutation = new Permutation(movePoolOptional, new MovePool(), 6, 6, "urdlwh");

        List<ByteList> byteList = Arrays.asList(permutation.getPermutation());
        List<String> moveStrings = byteList.stream()
                .map(list -> list.toString())
                .collect(Collectors.toList());

        List<String> expectedMoveStrings = Arrays.asList("u", "u", "r", "du", "du", "du");

        assertEquals(expectedMoveStrings, moveStrings);
    }
}