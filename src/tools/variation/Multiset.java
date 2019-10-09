package tools.variation;

import java.util.ArrayList;
import java.util.Collections;

public class Multiset {
    private int[] movePool;
    private int lowerBound;
    private int upperBound;
    public int currentSize;
    private int[] subset;
    public boolean finished = false;
    public ArrayList<String> moves;

    public Multiset(int lowerBound, int upperBound, MovePool movePool, String lexicographic) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.currentSize = lowerBound;

        int size = movePool.moves.size();
        this.movePool = new int[size];
        this.subset = new int[size];

        this.moves = new ArrayList<>(movePool.moves.keySet());
        this.moves.sort((s1, s2) -> compareLexicographic(s1, s2, lexicographic));

        for(int i = 0; i < size; i++) {
            this.movePool[i] = movePool.moves.get(moves.get(i));
        }
        initialSubset();
    }

    public void nextSubset() {
        int i;
        for(i = subset.length - 1; i >= 1; i--) {
            boolean found = false;
            if(subset[i] != movePool[i]) {
                for(int j = i - 1; j >= 0; j--) {
                    if(movePool[j] > 0) {
                        if(subset[j] > 0) {
                            subset[j]--;
                            found = true;
                        }
                        break;
                    }
                }
            }
            if(found) {
                break;
            }
        }
        if(i == 0) {
            if(currentSize < upperBound) {
                currentSize++;
                initialSubset();
                return;
            }
            finished = true;
            return;
        }

        int toDistribute = 1;
        for(int j = subset.length - 1; j >= i; j--) {
            toDistribute += subset[j];
            subset[j] = 0;
        }
        for(int j = i; j < subset.length; j++) {
            int distributed = Math.min(toDistribute, movePool[j]);
            toDistribute -= distributed;
            subset[j] = distributed;
        }
    }

    public int[] getSubset() {
        return subset;
    }

    public void reset() {
        finished = false;
        currentSize = lowerBound;
        initialSubset();
    }

    private void initialSubset() {
        int toDistribute = currentSize;
        for(int i = 0; i < subset.length; i++) {
            int distributed = Math.min(toDistribute, movePool[i]);
            toDistribute -= distributed;
            subset[i] = distributed;
        }

        int i;
        for(i = subset.length - 1; i >= 1; i--) {
            boolean found = false;
            if(subset[i] != movePool[i]) {
                for(int j = i - 1; j >= 0; j--) {
                    if(movePool[j] > 0) {
                        if(subset[j] > 0) {
                            found = true;
                        }
                        break;
                    }
                }
            }
            if(found) {
                break;
            }
        }
        if(i == 0 && currentSize > upperBound) {
            finished = true;
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
        if(s1.length() == s2.length()) {
            return 0;
        }
        return s1.length() - s2.length();
    }
}
