package tools.variation;

import emulator.SuperCC;
import util.CharList;

import java.util.ArrayList;
import java.util.HashMap;

public class Multiset {
    private MovePool movePoolForced;
    private int[] movePool;
    private BoundLimit limits;
    public int currentSize;
    private int[] subset;
    public boolean finished = false;
    public ArrayList<String> moves;
    public CharList[] movesList;
    private HashMap<String, Integer> moveIndex = new HashMap<>();
    public ArrayList<Double> cumulativePermutationCounts = new ArrayList<>();
    private int setIndex = 0;
    private double totalValidPermutations = 0;

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

    public Multiset(MovePoolContainer movePools, BoundLimit limits, String lexicographic) {
        MovePool movePoolTotal = getMovePoolTotal(movePools);
        this.movePoolForced = movePools.forced;
        this.limits = limits;
        this.currentSize = limits.lower;

        int size = movePoolTotal.moves.size();
        this.movePool = new int[size];
        this.subset = new int[size];

        this.moves = new ArrayList<>(movePoolTotal.moves.keySet());
        this.moves.sort((s1, s2) -> compareLexicographic(s1, s2, lexicographic));

        this.movesList = new CharList[this.moves.size()];

        for(int i = 0; i < this.moves.size(); i++) {
            this.moveIndex.put(this.moves.get(i), i);

            CharList moveList = new CharList();
            String str = this.moves.get(i);
            for(int j = 0; j < str.length(); j++) {
                moveList.add(toMove.get(str.charAt(j)));
            }
            this.movesList[i] = moveList;
        }

        for(int i = 0; i < size; i++) {
            this.movePool[i] = movePoolTotal.moves.get(moves.get(i));
        }
        initialSubset();
        calculatePermutationCount();
    }

    /**
     * This algorithm represents subsets as arrays where each index represents a move in specified order.
     * The value at each index is the amount of these moves.
     * If each value is interpreted as a digit, the next subset is the next possible smaller number.
     * E.g. in default order (urdlwh), if the move pool consists of [2u, r, 3d, l] and the size of subset is 4,
     * the first subset is [2, 1, 1, 0], and the next one is [2, 1, 0, 1] then [2, 0, 2, 0] then [2, 0, 1, 1] etc.
     * 2110 -> 2101 -> 2020 -> 2011, where each digit has a limit
     */
    public void nextSubset() {
        while(!finished) {
            setIndex++;
            int i = getDistributionIndex();
            if (i == 0) {
                endOfCurrentSize();
                return;
            }

            subset[i - 1]--;
            distribute(i, gatherDistribution(i));

            if(isSubsetValid()) {
                return;
            }
        }
    }

    public int[] getSubset() {
        return subset;
    }

    public void reset() {
        finished = false;
        currentSize = limits.lower;
        setIndex = 0;
        initialSubset();
    }

    public double getTotalPermutationCount() {
        return cumulativePermutationCounts.get(cumulativePermutationCounts.size() - 1);
    }

    public double getTotalValidPermutationCount() {
        return totalValidPermutations;
    }

    public double getCumulativePermutationCount() {
        return cumulativePermutationCounts.get(setIndex);
    }

    private int getDistributionIndex() {
        for (int i = subset.length - 1; i >= 1; i--) {
            if (subset[i] != movePool[i] && subset[i - 1] > 0) {
                return i;
            }
        }
        return 0;
    }

    private void distribute(int from, int toDistribute) {
        for(int i = from; i < subset.length; i++) {
            int distributed = Math.min(toDistribute, movePool[i]);
            toDistribute -= distributed;
            subset[i] = distributed;
        }
    }

    private int gatherDistribution(int from) {
        int toDistribute = 1;
        for (int j = from; j < subset.length; j++) {
            toDistribute += subset[j];
            subset[j] = 0;
        }
        return toDistribute;
    }

    private void endOfCurrentSize() {
        if (currentSize < limits.upper) {
            currentSize++;
            initialSubset();
            return;
        }
        finished = true;
    }

    private void initialSubset() {
        distribute(0, currentSize);

        if(!isSubsetValid()) {
            nextSubset();
        }
    }

    private int compareLexicographic(String s1, String s2, String lexicographic) {
        int size = Math.min(s1.length(), s2.length());
        for(int i = 0; i < size; i++) {
            int index1 = lexicographic.indexOf(s1.charAt(i));
            int index2 = lexicographic.indexOf(s2.charAt(i));
            if(index1 != index2) {
                return index1 - index2;
            }
        }
        return s1.length() - s2.length();
    }

    private MovePool getMovePoolTotal(MovePoolContainer movePools) {
        MovePool movePoolTotal = new MovePool();
        movePoolTotal.add(movePools);
        return movePoolTotal;
    }

    private boolean isSubsetValid() {
        for(String move : movePoolForced.moves.keySet()) {
            if(subset[moveIndex.get(move)] < movePoolForced.moves.get(move)) {
                return false;
            }
        }
        return true;
    }

    private void calculatePermutationCount() {
        double count = 0;
        cumulativePermutationCounts.add(count);
        MovePool forcedBackup = movePoolForced;
        movePoolForced = new MovePool();
        reset();
        do {
            double permutations = uniquePermutations(currentSize, subset);
            count += permutations;
            if(shouldCountPermutations(forcedBackup)) {
                totalValidPermutations += permutations;
            }
            cumulativePermutationCounts.add(count);
            nextSubset();
        } while(!finished && count < LIMIT);
        movePoolForced = forcedBackup;
        reset();
    }

    private boolean shouldCountPermutations(MovePool forcedBackup) {
        movePoolForced = forcedBackup;
        boolean ret = isSubsetValid();
        movePoolForced = new MovePool();
        return ret;
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
