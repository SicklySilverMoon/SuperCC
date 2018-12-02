package game;

public enum Step{
    
    ODD,
    EVEN;
    
    public static Step fromTWS(int n) {
        if (n >>> 5 == 1) return Step.ODD;
        else return Step.EVEN;
    }
    
    public byte toTWS() {
        if (this == EVEN) return 0;
        else return 0b100000;
    }
    
}
