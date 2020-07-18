package game;

public enum Step{

    EVEN,
    EVEN1,
    EVEN2,
    EVEN3,
    ODD,
    ODD1,
    ODD2,
    ODD3;
    
    public static Step fromTWS(int n) {
        n &= 0b11111000;
        n >>>= 3;
        return values()[n];
    }
    
    public byte toTWS() {
        int n = ordinal();
        return (byte) (n << 3);
    }

    public boolean isEven() {
        return (ordinal() < 4);
    }
}
