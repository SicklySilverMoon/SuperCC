package tools.variation;

import emulator.SuperCC;
import util.CharList;

import java.util.Arrays;
import java.util.HashMap;

public class Permutation {
    private MovePoolContainer movePools;
    public BoundLimit limits;
    private int[] permutation;
    public boolean finished = false;
    private Multiset set;
    private int[] subset;
    private int currentSize;
    private String lexicographic;
    public double permutationCount;
    public double permutationValidCount;

    public static double LIMIT = 1e18;

    private static final HashMap<Character, Character> toMove;

    static {
        toMove = new HashMap<>();
        toMove.put('u', SuperCC.UP);
        toMove.put('r', SuperCC.RIGHT);
        toMove.put('d', SuperCC.DOWN);
        toMove.put('l', SuperCC.LEFT);
        toMove.put('w', 'w');
        toMove.put('h', SuperCC.WAIT);
    }

    public Permutation(MovePoolContainer movePools, BoundLimit limits, String lexicographic) {
        this.movePools = movePools;
        this.limits = limits;
        limits.setBounds(movePools);
        this.set = new Multiset(movePools, limits, lexicographic);
        this.subset = this.set.getSubset();
        this.currentSize = this.limits.lower;
        this.lexicographic = lexicographic;
        this.permutationCount = calculatePermutationCount();
        this.permutationValidCount = calculatePermutationValidCount();

        initialPermutation();
    }

    /**
     * This algorithm represents permutations as arrays where each index i is the i-th move.
     * The value at each index is its specified order within the permutation.
     * If each value is interpreted as a digit, the next permutation is the next possible larger number.
     * E.g. in default order (urdlwh), if the move pool consists of [2u, r, l],
     * the first permutation is [1, 1, 2, 3], and the next one is [1, 1, 3, 2] then [1, 2, 1, 3] then [1, 2, 3, 1] etc.
     * 1123 -> 1132 -> 1213 -> 1231
     */
    public void nextPermutation() {
        int i = getFirstDescendingIndex();
        if(i == -1) {
            endOfCurrentSubset();
            return;
        }
        int j = getNextLargerIndex(i);

        swap(i, j);
        reverseSubarray(i + 1);
    }

    public CharList[] getPermutation() {
        if(finished) {
            return null;
        }
        CharList[] moves = new CharList[currentSize];

        for(int i = 0; i < currentSize; i++) {
            moves[i] = new CharList();
            String str = this.set.moves.get(permutation[i]);
            for(int j = 0; j < str.length(); j++) {
                moves[i].add(toMove.get(str.charAt(j)));
            }
        }

        return moves;
    }

    public int[] getRawPermutation() {
        return permutation;
    }

    // Returns double due to potentially large value
    private double calculatePermutationCount() {
        return set.getTotalPermutationCount();
    }

    private double calculatePermutationValidCount() {
        return set.getTotalValidPermutationCount();
    }

    public void reset() {
        set.reset();
        finished = false;
        currentSize = limits.lower;
        initialPermutation();
    }

    public void terminate(int index) {
        Arrays.sort(permutation, index + 1, permutation.length);
        for(int i = index + 1; i < (index + 2 + permutation.length)/2; i++) {
            int temp = permutation[i];
            permutation[i] = permutation[permutation.length - i + index];
            permutation[permutation.length - i + index] = temp;
        }
    }

    public void end() {
        terminate(-1);
        finished = true;
    }

    public double getPermutationIndex() {
        double index = 0;
        double potential = uniquePermutations(currentSize, subset);
        int[] currentSubset = subset.clone();

        for(int position = 0, currentLength = currentSize; position < currentSize - 1 && potential > 1; position++, currentLength--) {
            int offset = 0;
            for(int i = 0; i < permutation[position]; i++) {
                offset += currentSubset[i];
            }
            index += (potential * offset) / currentLength;

            potential *= currentSubset[permutation[position]];
            potential /= currentLength;

            currentSubset[permutation[position]]--;
        }

//        System.out.println(index + set.getCumulativePermutationCount() + "  " + set.setIndex + "  " + index + "  " +
//                set.cumulativePermutationCounts.get(set.setIndex));
        return index + set.getCumulativePermutationCount();
    }

    private int getFirstDescendingIndex() {
        for(int i = currentSize - 2; i >= 0; i--) {
            if(permutation[i] < permutation[i + 1]) {
                return i;
            }
        }
        return -1;
    }

    private void endOfCurrentSubset() {
        set.nextSubset();
        if(!set.finished) {
            currentSize = set.currentSize;
            initialPermutation();
            return;
        }
        finished = true;
    }

    private int getNextLargerIndex(int to) {
        for(int i = currentSize - 1; i > to; i--) {
            if(permutation[to] < permutation[i]) {
                return i;
            }
        }
        return to;
    }

    private void swap(int i, int j) {
        int temp = permutation[i];
        permutation[i] = permutation[j];
        permutation[j] = temp;
    }

    private void reverseSubarray(int from) {
        for(int i = 0; i < (currentSize - from)/2; i++) {
            swap(from + i, currentSize - 1 - i);
        }
    }

    private void initialPermutation() {
        permutation = new int[currentSize];
        int i = 0;
        for(int j = 0; j < subset.length; j++) {
            for(int k = 0; k < subset[j]; k++) {
                permutation[i++] = j;
            }
        }
    }

    private double factorial(int n) {
        if(n == 0 || n == 1) {
            return 1;
        }
        return (double)n * factorial(n - 1);
    }

    private double uniquePermutations(int n, int[] moves) {
        double denominator = 1;
        for (int move : moves) {
            denominator *= factorial(move);
        }
        return factorial(n)/denominator;
    }
}
