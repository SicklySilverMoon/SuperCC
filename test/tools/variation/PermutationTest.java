package tools.variation;

import org.junit.jupiter.api.Test;
import util.ByteList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class PermutationTest {
    @Test
    void boundWhenBothBoundsNull() {
        MovePoolContainer movePools = new MovePoolContainer();
        movePools.optional.add(new Move("5u"));
        Permutation permutation = new Permutation(movePools, new BoundLimit(), "urdlwh");

        assertEquals(5, permutation.limits.lower);
        assertEquals(5, permutation.limits.upper);
    }

    @Test
    void boundWhenUpperBoundNull() {
        MovePoolContainer movePools = new MovePoolContainer();
        movePools.optional.add(new Move("5u"));
        Permutation permutation = new Permutation(movePools, new BoundLimit(4), "urdlwh");

        assertEquals(4, permutation.limits.lower);
        assertEquals(4, permutation.limits.upper);
    }

    @Test
    void boundSwap() {
        MovePoolContainer movePools = new MovePoolContainer();
        movePools.optional.add(new Move("5u"));
        Permutation permutation = new Permutation(movePools, new BoundLimit(5, 3), "urdlwh");

        assertEquals(3, permutation.limits.lower);
        assertEquals(5, permutation.limits.upper);
    }

    @Test
    void boundClip() {
        MovePoolContainer movePools = new MovePoolContainer();
        movePools.optional.add(new Move("5u"));
        Permutation permutation = new Permutation(movePools, new BoundLimit(6, 7), "urdlwh");

        assertEquals(5, permutation.limits.lower);
        assertEquals(5, permutation.limits.upper);
    }

    @Test
    void boundClipForced() {
        MovePoolContainer movePools = new MovePoolContainer();
        movePools.optional.add(new Move("5u"));
        movePools.forced.add(new Move("3d"));
        Permutation permutation = new Permutation(movePools, new BoundLimit(1, 2), "urdlwh");

        assertEquals(3, permutation.limits.lower);
        assertEquals(3, permutation.limits.upper);
    }

    @Test
    void initialPermutation() {
        MovePoolContainer movePools = new MovePoolContainer();
        movePools.optional.add(new Move("r"));
        movePools.optional.add(new Move("2u"));
        movePools.optional.add(new Move("3du"));
        Permutation permutation = new Permutation(movePools, new BoundLimit(6), "urdlwh");

        int[] permutationRaw = permutation.getRawPermutation();

        int[] expectedPermutationRaw = {0, 0, 1, 2, 2, 2};

        assertArrayEquals(expectedPermutationRaw, permutationRaw);
    }

    @Test
    void initialPermutationForced() {
        MovePoolContainer movePools = new MovePoolContainer();
        movePools.optional.add(new Move("r"));
        movePools.optional.add(new Move("2u"));
        movePools.optional.add(new Move("3du"));
        movePools.forced.add(new Move("4lr"));
        Permutation permutation = new Permutation(movePools, new BoundLimit(8, 10), "urdlwh");

        int[] permutationRaw = permutation.getRawPermutation();

        int[] expectedPermutationRaw = {0, 0, 1, 2, 3, 3, 3, 3};

        assertArrayEquals(expectedPermutationRaw, permutationRaw);
    }

    @Test
    void nextPermutation() {
        MovePoolContainer movePools = new MovePoolContainer();
        movePools.optional.add(new Move("r"));
        movePools.optional.add(new Move("2u"));
        movePools.optional.add(new Move("3du"));
        Permutation permutation = new Permutation(movePools, new BoundLimit(6), "urdlwh");
        permutation.nextPermutation();

        int[] permutationRaw = permutation.getRawPermutation();

        int[] expectedPermutationRaw = {0, 0, 2, 1, 2, 2};

        assertArrayEquals(expectedPermutationRaw, permutationRaw);
    }

    @Test
    void permutationCount() {
        MovePoolContainer movePools = new MovePoolContainer();
        movePools.optional.add(new Move("r"));
        movePools.optional.add(new Move("2u"));
        movePools.optional.add(new Move("3du"));
        Permutation permutation = new Permutation(movePools, new BoundLimit(5, 6), "urdlwh");

        assertEquals(120, permutation.permutationCount);
    }

    @Test
    void permutationCountForced() {
        MovePoolContainer movePools = new MovePoolContainer();
        movePools.optional.add(new Move("r"));
        movePools.optional.add(new Move("2u"));
        movePools.forced.add(new Move("3du"));
        Permutation permutation = new Permutation(movePools, new BoundLimit(5, 6), "urdlwh");

        assertEquals(90, permutation.permutationCount);
    }

    @Test
    void reset() {
        MovePoolContainer movePools = new MovePoolContainer();
        movePools.optional.add(new Move("5u"));
        Permutation permutation = new Permutation(movePools, new BoundLimit(4, 5), "urdlwh");
        permutation.nextPermutation();
        permutation.reset();

        int[] permutationRaw = permutation.getRawPermutation();

        int[] expectedPermutationRaw = {0, 0, 0, 0};

        assertArrayEquals(expectedPermutationRaw, permutationRaw);
    }

    @Test
    void terminate() {
        MovePoolContainer movePools = new MovePoolContainer();
        movePools.optional.add(new Move("r"));
        movePools.optional.add(new Move("2u"));
        movePools.optional.add(new Move("3du"));
        Permutation permutation = new Permutation(movePools, new BoundLimit(6), "urdlwh");
        permutation.terminate(0);

        int[] permutationRaw = permutation.getRawPermutation();

        int[] expectedPermutationRaw = {0, 2, 2, 2, 1, 0};

        assertArrayEquals(expectedPermutationRaw, permutationRaw);
    }

    @Test
    void end() {
        MovePoolContainer movePools = new MovePoolContainer();
        movePools.optional.add(new Move("r"));
        movePools.optional.add(new Move("2u"));
        movePools.optional.add(new Move("3du"));
        Permutation permutation = new Permutation(movePools, new BoundLimit(6), "urdlwh");
        permutation.end();

        int[] permutationRaw = permutation.getRawPermutation();

        int[] expectedPermutationRaw = {2, 2, 2, 1, 0, 0};

        assertArrayEquals(expectedPermutationRaw, permutationRaw);
    }

    @Test
    void getPermutation() {
        MovePoolContainer movePools = new MovePoolContainer();
        movePools.optional.add(new Move("r"));
        movePools.optional.add(new Move("2u"));
        movePools.optional.add(new Move("3du"));
        Permutation permutation = new Permutation(movePools, new BoundLimit(6), "urdlwh");

        List<ByteList> byteList = Arrays.asList(permutation.getPermutation());
        List<String> moveStrings = byteList.stream()
                .map(list -> list.toString())
                .collect(Collectors.toList());

        List<String> expectedMoveStrings = Arrays.asList("u", "u", "r", "du", "du", "du");

        assertEquals(expectedMoveStrings, moveStrings);
    }

    @Test
    void allPermutationsSingleSubset() {
        MovePoolContainer movePools = new MovePoolContainer();
        movePools.optional.add(new Move("u"));
        movePools.optional.add(new Move("2r"));
        movePools.optional.add(new Move("d"));
        Permutation permutation = new Permutation(movePools, new BoundLimit(4), "urdlwh");

        int[] permutationRaw = permutation.getRawPermutation();
        List<List<Integer>> result = new ArrayList<>();
        while(!permutation.finished) {
            result.add(Arrays.stream(permutationRaw).boxed().collect(Collectors.toList()));
            permutation.nextPermutation();
        }

        List<List<Integer>> exprectedResult = new ArrayList<>();
        exprectedResult.add(Arrays.asList(0, 1, 1, 2));
        exprectedResult.add(Arrays.asList(0, 1, 2, 1));
        exprectedResult.add(Arrays.asList(0, 2, 1, 1));
        exprectedResult.add(Arrays.asList(1, 0, 1, 2));
        exprectedResult.add(Arrays.asList(1, 0, 2, 1));
        exprectedResult.add(Arrays.asList(1, 1, 0, 2));
        exprectedResult.add(Arrays.asList(1, 1, 2, 0));
        exprectedResult.add(Arrays.asList(1, 2, 0, 1));
        exprectedResult.add(Arrays.asList(1, 2, 1, 0));
        exprectedResult.add(Arrays.asList(2, 0, 1, 1));
        exprectedResult.add(Arrays.asList(2, 1, 0, 1));
        exprectedResult.add(Arrays.asList(2, 1, 1, 0));

        assertEquals(exprectedResult, result);
    }

    @Test
    void allPermutationsMultipleSubsets() {
        MovePoolContainer movePools = new MovePoolContainer();
        movePools.optional.add(new Move("u"));
        movePools.optional.add(new Move("2r"));
        movePools.optional.add(new Move("d"));
        Permutation permutation = new Permutation(movePools, new BoundLimit(2, 3), "urdlwh");

        List<List<Integer>> result = new ArrayList<>();
        while(!permutation.finished) {
            int[] permutationRaw = permutation.getRawPermutation();
            result.add(Arrays.stream(permutationRaw).boxed().collect(Collectors.toList()));
            permutation.nextPermutation();
        }

        List<List<Integer>> exprectedResult = new ArrayList<>();
        exprectedResult.add(Arrays.asList(0, 1));
        exprectedResult.add(Arrays.asList(1, 0));
        exprectedResult.add(Arrays.asList(0, 2));
        exprectedResult.add(Arrays.asList(2, 0));
        exprectedResult.add(Arrays.asList(1, 1));
        exprectedResult.add(Arrays.asList(1, 2));
        exprectedResult.add(Arrays.asList(2, 1));
        exprectedResult.add(Arrays.asList(0, 1, 1));
        exprectedResult.add(Arrays.asList(1, 0, 1));
        exprectedResult.add(Arrays.asList(1, 1, 0));
        exprectedResult.add(Arrays.asList(0, 1, 2));
        exprectedResult.add(Arrays.asList(0, 2, 1));
        exprectedResult.add(Arrays.asList(1, 0, 2));
        exprectedResult.add(Arrays.asList(1, 2, 0));
        exprectedResult.add(Arrays.asList(2, 0, 1));
        exprectedResult.add(Arrays.asList(2, 1, 0));
        exprectedResult.add(Arrays.asList(1, 1, 2));
        exprectedResult.add(Arrays.asList(1, 2, 1));
        exprectedResult.add(Arrays.asList(2, 1, 1));

        assertEquals(exprectedResult, result);
    }

    @Test
    void allPermutationsMultipleSubsetsForced() {
        MovePoolContainer movePools = new MovePoolContainer();
        movePools.optional.add(new Move("u"));
        movePools.optional.add(new Move("2r"));
        movePools.forced.add(new Move("d"));
        Permutation permutation = new Permutation(movePools, new BoundLimit(2, 3), "urdlwh");

        List<List<Integer>> result = new ArrayList<>();
        while(!permutation.finished) {
            int[] permutationRaw = permutation.getRawPermutation();
            result.add(Arrays.stream(permutationRaw).boxed().collect(Collectors.toList()));
            permutation.nextPermutation();
        }

        List<List<Integer>> exprectedResult = new ArrayList<>();
        exprectedResult.add(Arrays.asList(0, 2));
        exprectedResult.add(Arrays.asList(2, 0));
        exprectedResult.add(Arrays.asList(1, 2));
        exprectedResult.add(Arrays.asList(2, 1));
        exprectedResult.add(Arrays.asList(0, 1, 2));
        exprectedResult.add(Arrays.asList(0, 2, 1));
        exprectedResult.add(Arrays.asList(1, 0, 2));
        exprectedResult.add(Arrays.asList(1, 2, 0));
        exprectedResult.add(Arrays.asList(2, 0, 1));
        exprectedResult.add(Arrays.asList(2, 1, 0));
        exprectedResult.add(Arrays.asList(1, 1, 2));
        exprectedResult.add(Arrays.asList(1, 2, 1));
        exprectedResult.add(Arrays.asList(2, 1, 1));

        assertEquals(exprectedResult, result);
    }
}