package tools.variation;

import emulator.EmulatorKeyListener;

import java.util.HashMap;

public class Permutation {
    private MovePool movePool;
    public Integer lowerBound;
    public Integer upperBound;
    private HashMap<Integer, Character> order = new HashMap<>();
    private int[] permutation;
    public boolean finished = false;
    private Multiset set;
    private int[] subset;
    private int currentSize;

    private static final HashMap<Character, EmulatorKeyListener.Key> toMove;

    static {
        toMove = new HashMap<>();
        toMove.put('u', EmulatorKeyListener.Key.UP);
        toMove.put('r', EmulatorKeyListener.Key.RIGHT);
        toMove.put('d', EmulatorKeyListener.Key.DOWN);
        toMove.put('l', EmulatorKeyListener.Key.LEFT);
        toMove.put('w', EmulatorKeyListener.Key.FULL_WAIT);
        toMove.put('h', EmulatorKeyListener.Key.HALF_WAIT);
    }

    // Returns double due to potentially large value
    public static double factorial(int n) {
        if(n == 0 || n == 1) {
            return 1;
        }
        return (double)n * factorial(n - 1);
    }

    public static double choose(int n, int k) {
        return factorial(n)/(factorial(k) * factorial(n - k));
    }

    public Permutation(MovePool movePool, Integer lowerBound, Integer upperBound, String lexicographic) {
        this.movePool = movePool;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        setBounds();
        this.set = new Multiset(this.lowerBound, this.upperBound, movePool, lexicographic);
        this.subset = this.set.getSubset();
        this.currentSize = this.lowerBound;

        for(int i = 0; i < 6; i++) {
            order.put(i, lexicographic.charAt(i));
        }

        initialPermutation();
    }

    public void nextPermutation() {
        int k, l;
        for(k = currentSize - 2; k >= 0; k--) {
            if(permutation[k] < permutation[k + 1]) {
                break;
            }
        }
        if(k == -1) {
            set.nextSubset();
            if(!set.finished) {
                currentSize = set.currentSize;
                initialPermutation();
                return;
            }
            finished = true;
            return;
        }
        for(l = currentSize - 1; l > k; l--) {
            if(permutation[k] < permutation[l]) {
                break;
            }
        }

        int temp = permutation[k];
        permutation[k] = permutation[l];
        permutation[l] = temp;

        for(int i = 0; i < (currentSize - k - 1)/2; i++) {
            temp = permutation[k + i + 1];
            permutation[k + i + 1] = permutation[currentSize - 1 - i];
            permutation[currentSize - 1 - i] = temp;
        }
    }

    public EmulatorKeyListener.Key[] getPermutation() {
        if(finished) {
            return null;
        }
        EmulatorKeyListener.Key[] moves = new EmulatorKeyListener.Key[currentSize];

        for(int i = 0; i < currentSize; i++) {
            moves[i] = toMove.get(order.get(permutation[i]));
        }

        return moves;
    }

    public int[] getRawPermutation() {
        if(finished) {
            return null;
        }
        return permutation;
    }

    private void initialPermutation() {
        permutation = new int[currentSize];
        int i = 0;
        for(int j = 0; j < 6; j++) {
            for(int k = 0; k < subset[j]; k++) {
                permutation[i++] = j;
            }
        }
    }

    private void setBounds() {
        if(lowerBound == null && upperBound == null) {
            int count = 0;
            for(int moves : movePool.moves.values()) {
                count += moves;
            }
            lowerBound = count;
            upperBound = count;
        }
        else if(upperBound == null) {
            upperBound = lowerBound;
        }
    }
}
