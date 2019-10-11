package tools.variation;

import emulator.EmulatorKeyListener;
import emulator.SuperCC;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import util.ByteList;

import java.util.Arrays;
import java.util.HashMap;

public class Permutation {
    private MovePool movePool;
    public Integer lowerBound;
    public Integer upperBound;
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

    public Permutation(MovePool movePool, Integer lowerBound, Integer upperBound, String lexicographic) {
        this.movePool = movePool;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        setBounds();
        this.set = new Multiset(this.lowerBound, this.upperBound, movePool, lexicographic);
        this.subset = this.set.getSubset();
        this.currentSize = this.lowerBound;
        this.lexicographic = lexicographic;

        initialPermutation();
    }

    public int[] getSubset() {
        return subset;
    }

    public void nextPermutation() {
        int k, l;
        for(k = currentSize - 2; k >= 0; k--) {
            if(permutation[k] < permutation[k + 1]) {
                break;
            }
        }
        if(k < 0) {
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
        Multiset m = new Multiset(lowerBound, upperBound, movePool, lexicographic);
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
        currentSize = lowerBound;
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

    private double factorial(int n) {
        if(n == 0 || n == 1) {
            return 1;
        }
        return (double)n * factorial(n - 1);
    }

    private double uniquePermutations(int n, int[] moves) {
        double denom = 1;
        for(int i = 0; i < moves.length; i++) {
            denom *= factorial(moves[i]);
        }
        return factorial(n)/denom;
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

    private void setBounds() {
        if(lowerBound == null && upperBound == null) {
            lowerBound = movePool.size;
            upperBound = movePool.size;
        }
        else if(upperBound == null) {
            upperBound = lowerBound;
        }
        if(upperBound < lowerBound) {
            int temp = upperBound;
            upperBound = lowerBound;
            lowerBound = temp;
        }
        upperBound = Math.min(upperBound, movePool.size);
        lowerBound = Math.min(lowerBound, movePool.size);
    }
}
