package tools.variation;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class MultisetTest {
    @Test
    void initialSubset() {
        MovePoolContainer movePools = new MovePoolContainer();
        movePools.optional.add(new Move("u"));
        movePools.optional.add(new Move("2r"));
        Multiset multiset = new Multiset(movePools, new BoundLimit(3), "urdlwh");

        int[] subset = multiset.getSubset();

        int[] expectedSubset = {1, 2};

        assertArrayEquals(expectedSubset, subset);
    }

    @Test
    void initialSubsetForced() {
        MovePoolContainer movePools = new MovePoolContainer();
        movePools.optional.add(new Move("u"));
        movePools.optional.add(new Move("2r"));
        movePools.forced.add(new Move("3l"));
        Multiset multiset = new Multiset(movePools, new BoundLimit(4, 6), "urdlwh");

        int[] subset = multiset.getSubset();

        int[] expectedSubset = {1, 0, 3};

        assertArrayEquals(expectedSubset, subset);
    }

    @Test
    void initialSubsetLexicographic() {
        MovePoolContainer movePools = new MovePoolContainer();
        movePools.optional.add(new Move("h"));
        movePools.optional.add(new Move("2d"));
        movePools.optional.add(new Move("3ud"));
        movePools.optional.add(new Move("4uu"));
        movePools.optional.add(new Move("5u"));
        Multiset multiset = new Multiset(movePools, new BoundLimit(15), "urdlwh");

        int[] subset = multiset.getSubset();

        int[] expectedSubset = {5, 4, 3, 2, 1};

        assertArrayEquals(expectedSubset, subset);
    }

    @Test
    void initialSubsetLowerbound() {
        MovePoolContainer movePools = new MovePoolContainer();
        movePools.optional.add(new Move("u"));
        movePools.optional.add(new Move("2r"));
        Multiset multiset = new Multiset(movePools, new BoundLimit(2, 3), "urdlwh");

        int[] subset = multiset.getSubset();

        int[] expectedSubset = {1, 1};

        assertArrayEquals(expectedSubset, subset);
    }

    @Test
    void nextSubset() {
        MovePoolContainer movePools = new MovePoolContainer();
        movePools.optional.add(new Move("u"));
        movePools.optional.add(new Move("2r"));
        Multiset multiset = new Multiset(movePools, new BoundLimit(2, 3), "urdlwh");
        multiset.nextSubset();

        int[] subset = multiset.getSubset();

        int[] expectedSubset = {0, 2};

        assertArrayEquals(expectedSubset, subset);
    }

    @Test
    void nextSubsetForced() {
        MovePoolContainer movePools = new MovePoolContainer();
        movePools.optional.add(new Move("u"));
        movePools.optional.add(new Move("2r"));
        movePools.forced.add(new Move("3l"));
        Multiset multiset = new Multiset(movePools, new BoundLimit(4, 6), "urdlwh");
        multiset.nextSubset();

        int[] subset = multiset.getSubset();

        int[] expectedSubset = {0, 1, 3};

        assertArrayEquals(expectedSubset, subset);
    }

    @Test
    void subsetCount() {
        MovePoolContainer movePools = new MovePoolContainer();
        movePools.optional.add(new Move("u"));
        movePools.optional.add(new Move("2r"));
        movePools.optional.add(new Move("3w"));
        Multiset multiset = new Multiset(movePools, new BoundLimit(4, 6), "urdlwh");

        int subsetCount = 0;
        while(!multiset.finished) {
            subsetCount++;
            multiset.nextSubset();
        }

        assertEquals(9, subsetCount);
    }

    @Test
    void subsetCountSingle() {
        MovePoolContainer movePools = new MovePoolContainer();
        movePools.optional.add(new Move("u"));
        Multiset multiset = new Multiset(movePools, new BoundLimit(1), "urdlwh");

        int subsetCount = 0;
        while(!multiset.finished) {
            subsetCount++;
            multiset.nextSubset();
        }

        assertEquals(1, subsetCount);
    }

    @Test
    void subsetCountForced() {
        MovePoolContainer movePools = new MovePoolContainer();
        movePools.optional.add(new Move("u"));
        movePools.optional.add(new Move("2r"));
        movePools.forced.add(new Move("3w"));
        Multiset multiset = new Multiset(movePools, new BoundLimit(4, 6), "urdlwh");

        int subsetCount = 0;
        while(!multiset.finished) {
            subsetCount++;
            multiset.nextSubset();
        }

        assertEquals(5, subsetCount);
    }

    @Test
    void reset() {
        MovePoolContainer movePools = new MovePoolContainer();
        movePools.optional.add(new Move("u"));
        movePools.optional.add(new Move("2r"));
        Multiset multiset = new Multiset(movePools, new BoundLimit(2, 3), "urdlwh");
        multiset.nextSubset();
        multiset.reset();

        int[] subset = multiset.getSubset();

        int[] expectedSubset = {1, 1};

        assertArrayEquals(expectedSubset, subset);
    }

    @Test
    void allSubsets() {
        MovePoolContainer movePools = new MovePoolContainer();
        movePools.optional.add(new Move("2u"));
        movePools.optional.add(new Move("r"));
        movePools.optional.add(new Move("3d"));
        Multiset multiset = new Multiset(movePools, new BoundLimit(3, 4), "urdlwh");
        int[] subset = multiset.getSubset();

        List<List<Integer>> result = new ArrayList<>();
        while(!multiset.finished) {
            result.add(Arrays.stream(subset).boxed().collect(Collectors.toList()));
            multiset.nextSubset();
        }

        List<List<Integer>> expectedResult = new ArrayList<>();
        expectedResult.add(Arrays.asList(2, 1, 0));
        expectedResult.add(Arrays.asList(2, 0, 1));
        expectedResult.add(Arrays.asList(1, 1, 1));
        expectedResult.add(Arrays.asList(1, 0, 2));
        expectedResult.add(Arrays.asList(0, 1, 2));
        expectedResult.add(Arrays.asList(0, 0, 3));
        expectedResult.add(Arrays.asList(2, 1, 1));
        expectedResult.add(Arrays.asList(2, 0, 2));
        expectedResult.add(Arrays.asList(1, 1, 2));
        expectedResult.add(Arrays.asList(1, 0, 3));
        expectedResult.add(Arrays.asList(0, 1, 3));

        assertEquals(expectedResult, result);
    }

    @Test
    void allSubsetsForced() {
        MovePoolContainer movePools = new MovePoolContainer();
        movePools.optional.add(new Move("u"));
        movePools.optional.add(new Move("r"));
        movePools.optional.add(new Move("3d"));
        movePools.forced.add(new Move("u"));
        Multiset multiset = new Multiset(movePools, new BoundLimit(3, 4), "urdlwh");
        int[] subset = multiset.getSubset();

        List<List<Integer>> result = new ArrayList<>();
        while(!multiset.finished) {
            result.add(Arrays.stream(subset).boxed().collect(Collectors.toList()));
            multiset.nextSubset();
        }

        List<List<Integer>> expectedResult = new ArrayList<>();
        expectedResult.add(Arrays.asList(2, 1, 0));
        expectedResult.add(Arrays.asList(2, 0, 1));
        expectedResult.add(Arrays.asList(1, 1, 1));
        expectedResult.add(Arrays.asList(1, 0, 2));
        expectedResult.add(Arrays.asList(2, 1, 1));
        expectedResult.add(Arrays.asList(2, 0, 2));
        expectedResult.add(Arrays.asList(1, 1, 2));
        expectedResult.add(Arrays.asList(1, 0, 3));

        assertEquals(expectedResult, result);
    }
}