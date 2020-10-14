package game;

public class RNG{
    
    public static final int LAST_SEED = 0x7FFFFFFF;

    private int currentValue, prngValue1, prngValue2;
    private int nextValue(){
        return currentValue = (currentValue * 1103515245 + 12345) & 0x7FFFFFFF;
    }
    public void setCurrentValue(int value){
        currentValue = value & 0x7FFFFFFF;
    }
    public int getCurrentValue() {
        return currentValue;
    }
    public void setPRNG1(int value) {
        prngValue1 = value;
    }
    public int getPRNG1() {
        return prngValue1;
    }
    public void setPRNG2(int value) {
        prngValue2 = value;
    }
    public int getPRNG2() {
        return prngValue2;
    }

    /**
     * Choose a random number from 0 to 3 inclusive. This is used by random
     * force floors and Lynx blobs. This advances the RNG once.
     * @return An int from 0-3 .
     */
    public int random4(){
        return nextValue() >>> 29;
    }

    /**
     * Randomly permute an array with 3 elements in place. This is used by
     * walkers on the array {left, backwards, right}. This advances the rng
     * once.
     * @param a The array to permute
     */
    public void randomPermutation3(Object[] a){
        nextValue();
        Object swap;
        int n;

        n = currentValue >>> 30;                                                    // 0 or 1
        swap = a[n]; a[n] = a[1]; a[1] = swap;

        n = (int) ((3.0 * (currentValue & 0x3FFFFFFF)) / (double) 0x40000000);      // 0, 1 or 2
        swap = a[n]; a[n] = a[2]; a[2] = swap;
    }

    /**
     * Randomly permute an array with 4 elements in place. This is used by
     * blobs on the array {forwards, left, backwards, right}. This advances the
     * rng once.
     * @param a The array to permute
     */
    public void randomPermutation4(Object[] a){
        nextValue();
        Object swap;
        int n;

        n = currentValue >>> 30;                                                    // 0 or 1
        swap = a[n]; a[n] = a[1]; a[1] = swap;

        n = (int) ((3.0 * (currentValue & 0x0FFFFFFF)) / (double) 0x10000000);      // 0, 1 or 2
        swap = a[n]; a[n] = a[2]; a[2] = swap;

        n = (currentValue >>> 28) & 3;                                              // 0, 1, 2 or 3
        swap = a[n]; a[n] = a[3]; a[3] = swap;
    }

    /**
     * Choose a pseudo random number (normally a pre-chosen cycle that's known to be constant)
     * from 0 to 3 inclusive. This is used by Lynx walkers.
     * @return An int from 0-3 .
     */
    public int pseudoRandom4() {
        int n = (prngValue1 >> 2) - prngValue1;
        if ((prngValue1 & 0x02) == 0)
            --n;
        prngValue1 = (prngValue1 >> 1) | (prngValue2 & 0x80);
        prngValue2 = (prngValue2 << 1) | (n & 0x01);
        return ((prngValue1 ^ prngValue2) & 0xFF) & 0x03;
    }

    public RNG(int startingSeed, int prngValue1, int prngValue2) {
        currentValue = startingSeed;
    }

}
