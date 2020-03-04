package tools.variation;

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
    private HashMap<String, Integer> moveIndex = new HashMap<>();

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

        for(int i = 0; i < this.moves.size(); i++) {
            this.moveIndex.put(this.moves.get(i), i);
        }

        for(int i = 0; i < size; i++) {
            this.movePool[i] = movePoolTotal.moves.get(moves.get(i));
        }
        initialSubset();
    }

    public void nextSubset() {
        while(!finished) {
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
        initialSubset();
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
}
