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
        MovePool movePoolOptional = new MovePool();
        movePoolOptional.add(new Move("u"));
        movePoolOptional.add(new Move("2r"));
        Multiset multiset = new Multiset(3,3,movePoolOptional, new MovePool(), "urdlwh");

        int[] subset = multiset.getSubset();

        int[] expectedSubset = {1, 2};

        assertArrayEquals(expectedSubset, subset);
    }

    @Test
    void initialSubsetForced() {
        MovePool movePoolOptional = new MovePool();
        movePoolOptional.add(new Move("u"));
        movePoolOptional.add(new Move("2r"));
        MovePool movePoolForced = new MovePool();
        movePoolForced.add(new Move("3l"));
        Multiset multiset = new Multiset(4,6, movePoolOptional, movePoolForced, "urdlwh");

        int[] subset = multiset.getSubset();

        int[] expectedSubset = {1, 0, 3};

        assertArrayEquals(expectedSubset, subset);
    }

    @Test
    void initialSubsetLexicographic() {
        MovePool movePoolOptional = new MovePool();
        movePoolOptional.add(new Move("h"));
        movePoolOptional.add(new Move("2d"));
        movePoolOptional.add(new Move("3ud"));
        movePoolOptional.add(new Move("4uu"));
        movePoolOptional.add(new Move("5u"));
        Multiset multiset = new Multiset(15,15, movePoolOptional, new MovePool(), "urdlwh");

        int[] subset = multiset.getSubset();

        int[] expectedSubset = {5, 4, 3, 2, 1};

        assertArrayEquals(expectedSubset, subset);
    }

    @Test
    void initialSubsetLowerbound() {
        MovePool movePoolOptional = new MovePool();
        movePoolOptional.add(new Move("u"));
        movePoolOptional.add(new Move("2r"));
        Multiset multiset = new Multiset(2,3, movePoolOptional, new MovePool(), "urdlwh");

        int[] subset = multiset.getSubset();

        int[] expectedSubset = {1, 1};

        assertArrayEquals(expectedSubset, subset);
    }

    @Test
    void nextSubset() {
        MovePool movePoolOptional = new MovePool();
        movePoolOptional.add(new Move("u"));
        movePoolOptional.add(new Move("2r"));
        Multiset multiset = new Multiset(2,3, movePoolOptional, new MovePool(), "urdlwh");
        multiset.nextSubset();

        int[] subset = multiset.getSubset();

        int[] expectedSubset = {0, 2};

        assertArrayEquals(expectedSubset, subset);
    }

    @Test
    void nextSubsetForced() {
        MovePool movePoolOptional = new MovePool();
        movePoolOptional.add(new Move("u"));
        movePoolOptional.add(new Move("2r"));
        MovePool movePoolForced = new MovePool();
        movePoolForced.add(new Move("3l"));
        Multiset multiset = new Multiset(4,6, movePoolOptional, movePoolForced, "urdlwh");
        multiset.nextSubset();

        int[] subset = multiset.getSubset();

        int[] expectedSubset = {0, 1, 3};

        assertArrayEquals(expectedSubset, subset);
    }

    @Test
    void subsetCount() {
        MovePool movePoolOptional = new MovePool();
        movePoolOptional.add(new Move("u"));
        movePoolOptional.add(new Move("2r"));
        movePoolOptional.add(new Move("3w"));
        Multiset multiset = new Multiset(4,6, movePoolOptional, new MovePool(), "urdlwh");

        int subsetCount = 0;
        while(!multiset.finished) {
            subsetCount++;
            multiset.nextSubset();
        }

        assertEquals(9, subsetCount);
    }

    @Test
    void subsetCountSingle() {
        MovePool movePoolOptional = new MovePool();
        movePoolOptional.add(new Move("u"));
        Multiset multiset = new Multiset(1,1, movePoolOptional, new MovePool(), "urdlwh");

        int subsetCount = 0;
        while(!multiset.finished) {
            subsetCount++;
            multiset.nextSubset();
        }

        assertEquals(1, subsetCount);
    }

    @Test
    void subsetCountForced() {
        MovePool movePoolOptional = new MovePool();
        movePoolOptional.add(new Move("u"));
        movePoolOptional.add(new Move("2r"));
        MovePool movePoolForced = new MovePool();
        movePoolForced.add(new Move("3w"));
        Multiset multiset = new Multiset(4,6, movePoolOptional, movePoolForced, "urdlwh");

        int subsetCount = 0;
        while(!multiset.finished) {
            subsetCount++;
            multiset.nextSubset();
        }

        assertEquals(5, subsetCount);
    }

    @Test
    void reset() {
        MovePool movePoolOptional = new MovePool();
        movePoolOptional.add(new Move("u"));
        movePoolOptional.add(new Move("2r"));
        Multiset multiset = new Multiset(2,3, movePoolOptional, new MovePool(), "urdlwh");
        multiset.nextSubset();
        multiset.reset();

        int[] subset = multiset.getSubset();

        int[] expectedSubset = {1, 1};

        assertArrayEquals(expectedSubset, subset);
    }

    @Test
    void allSubsets() {
        MovePool movePoolOptional = new MovePool();
        movePoolOptional.add(new Move("2u"));
        movePoolOptional.add(new Move("r"));
        movePoolOptional.add(new Move("3d"));
        Multiset multiset = new Multiset(3,4, movePoolOptional, new MovePool(), "urdlwh");
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
        MovePool movePoolOptional = new MovePool();
        movePoolOptional.add(new Move("u"));
        movePoolOptional.add(new Move("r"));
        movePoolOptional.add(new Move("3d"));
        MovePool movePoolForced = new MovePool();
        movePoolForced.add(new Move("u"));
        Multiset multiset = new Multiset(3,4, movePoolOptional, movePoolForced, "urdlwh");
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