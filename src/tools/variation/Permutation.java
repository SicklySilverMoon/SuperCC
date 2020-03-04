package tools.variation;

import emulator.SuperCC;
import util.ByteList;

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

    public static double LIMIT = 1e18;

    private static final HashMap<Character, Byte> toMove;

    static {
        toMove = new HashMap<>();
        toMove.put('u', SuperCC.UP);
        toMove.put('r', SuperCC.RIGHT);
        toMove.put('d', SuperCC.DOWN);
        toMove.put('l', SuperCC.LEFT);
        toMove.put('w', (byte)'w');
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

        initialPermutation();
    }

    public int[] getSubset() {
        return subset;
    }

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

    public ByteList[] getPermutation() {
        if(finished) {
            return null;
        }
        ByteList[] moves = new ByteList[currentSize];

        for(int i = 0; i < currentSize; i++) {
            moves[i] = new ByteList();
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
    public double getPermutationCount() {
        double count = 0;
        Multiset m = new Multiset(movePools, limits, lexicographic);
        int[] s = m.getSubset();
        do {
            count += uniquePermutations(m.currentSize, s);
            m.nextSubset();
        } while(!m.finished && count < LIMIT);
        return count;
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

    private double factorial(int n) {
        if(n == 0 || n == 1) {
            return 1;
        }
        return (double)n * factorial(n - 1);
    }

    private double uniquePermutations(int n, int[] moves) {
        double denominator = 1;
        for(int i = 0; i < moves.length; i++) {
            denominator *= factorial(moves[i]);
        }
        return factorial(n)/denominator;
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
}
