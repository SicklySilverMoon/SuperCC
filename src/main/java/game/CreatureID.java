package game;

public enum CreatureID {
    
    BUG             (0b00_0000_0000000000, "Bug"),
    FIREBALL        (0b00_0001_0000000000, "Fireball"),
    PINK_BALL       (0b00_0010_0000000000, "Ball"),
    TANK_MOVING     (0b00_0011_0000000000, "Tank (Moving)"),
    GLIDER          (0b00_0100_0000000000, "Glider"),
    TEETH           (0b00_0101_0000000000, "Teeth"),
    WALKER          (0b00_0110_0000000000, "Walker"),
    BLOB            (0b00_0111_0000000000, "Blob"),
    PARAMECIUM      (0b00_1000_0000000000, "Paramecium"),
    TANK_STATIONARY (0b00_1001_0000000000, "Tank (Still)"),
    BLOCK           (0b00_1010_0000000000, "Block"),
    CHIP            (0b00_1011_0000000000, "Chip"),
    ICE_BLOCK       (0b00_1100_0000000000, "Ice Block"),
    CHIP_SLIDING    (0b00_1101_0000000000, "Chip (Sliding)"),
    CHIP_SWIMMING   (0b00_1110_0000000000, "Swimming Chip"),
    DEAD            (0b00_1111_0000000000, "Dead");
    
    private final int bits;
    private final String name;

    public int getBits() {
        return bits;
    }
    public String prettyPrint() {
        return name;
    }
    
    private static final CreatureID[] allCreatures = values();
    public static CreatureID fromOrdinal(int ordinal) {
        return allCreatures[ordinal];
    }

    public boolean isAffectedByCB(){
        return this == TEETH || this == BUG || this == PARAMECIUM;
    }
    public boolean isChip(){
        return this == CHIP || this == CHIP_SLIDING;
    }
    public boolean isMonster(){
        return this.ordinal() <= TANK_STATIONARY.ordinal();
    }
    public boolean isBlock(){
        return this == BLOCK || this == ICE_BLOCK;
    }
    public boolean isTank() {
        return this == TANK_MOVING || this == TANK_STATIONARY;
    }
    
    CreatureID (int bits, String name) {
        this.bits = bits;
        this.name = name;
    }
    
}
