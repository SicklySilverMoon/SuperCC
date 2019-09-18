package tools.variation;

public class Multiset {
    private int[] movePool = new int[6];
    private int lowerBound;
    private int upperBound;
    public int currentSize;
    private int[] subset = new int[6];
    public boolean finished = false;

    public Multiset(int lowerBound, int upperBound, MovePool movePool, String lexicographic) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.currentSize = lowerBound;

        for(int i = 0; i < 6; i++) {
            this.movePool[i] = movePool.moves.get(lexicographic.charAt(i));
        }
        initialSubset();
    }

    public void nextSubset() {
        int i;
        for(i = 5; i >= 1; i--) {
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
        for(int j = 5; j >= i; j--) {
            toDistribute += subset[j];
            subset[j] = 0;
        }
        for(int j = i; j < 6; j++) {
            int distributed = Math.min(toDistribute, movePool[i]);
            toDistribute -= distributed;
            subset[j] = distributed;
        }
    }

    public int[] getSubset() {
        return subset;
    }

    public void reset() {
        finished = false;
        initialSubset();
    }

    private void initialSubset() {
        int toDistribute = currentSize;
        for(int i = 0; i < 6; i++) {
            int distributed = Math.min(toDistribute, movePool[i]);
            toDistribute -= distributed;
            subset[i] = distributed;
        }

        int i;
        for(i = 5; i >= 1; i--) {
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
}
