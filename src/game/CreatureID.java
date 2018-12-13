package game;

public enum CreatureID {
    
    BUG             (0b00_0000_0000000000),
    FIREBALL        (0b00_0001_0000000000),
    PINK_BALL       (0b00_0010_0000000000),
    TANK_MOVING     (0b00_0011_0000000000),
    GLIDER          (0b00_0100_0000000000),
    TEETH           (0b00_0101_0000000000),
    WALKER          (0b00_0110_0000000000),
    BLOB            (0b00_0111_0000000000),
    PARAMECIUM      (0b00_1000_0000000000),
    TANK_STATIONARY (0b00_1001_0000000000),
    BLOCK           (0b00_1010_0000000000),
    CHIP            (0b00_1011_0000000000),
    UNUSED          (0b00_1100_0000000000),
    CHIP_SLIDING    (0b00_1101_0000000000),
    UNUSED_2        (0b00_1110_0000000000),
    DEAD            (0b00_1111_0000000000);
    
    private final int bits;
    
    int getBits () {
        return bits;
    }
    
    private static final CreatureID[] allCreatures = values();
    public static CreatureID fromOrdinal(int ordinal) {
        return allCreatures[ordinal];
    }
    
    boolean isAffectedByCB(){
        return this == TEETH || this == BUG || this == PARAMECIUM;
    }
    boolean isChip(){
        return this == DEAD || this == CHIP || this == CHIP_SLIDING;
    }
    boolean isMonster(){
        return this.ordinal() <= TANK_STATIONARY.ordinal();
    }
    boolean isBlock(){
        return this == BLOCK;
    }
    public boolean isTank() {
        return this == TANK_MOVING || this == TANK_STATIONARY;
    }
    
    CreatureID (int bits) {
        this.bits = bits;
    }
    
}
