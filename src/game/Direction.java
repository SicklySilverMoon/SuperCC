package game;

public enum Direction {
    
    UP(0b00_0000_0000000000),
    LEFT(0b01_0000_0000000000),
    DOWN(0b10_0000_0000000000),
    RIGHT(0b11_0000_0000000000);
    
    public static final Direction TURN_LEFT = LEFT, TURN_RIGHT = RIGHT,
        TURN_AROUND = DOWN, TURN_FORWARD = UP;
    
    private static final Direction[] allDirections = Direction.values();
    static Direction fromOrdinal(int ordinal){
        return allDirections[ordinal];
    }
    
    private final int bits;
    
    public int getBits() {
        return bits;
    }
    
    public static Direction turn(Direction direction, Direction turn) {
        return fromOrdinal(direction.ordinal() + turn.ordinal() & 0b11);
    }
    
    public Direction turn(Direction turn) {
        return fromOrdinal(ordinal() + turn.ordinal() & 0b11);
    }
    
    public Direction[] turn(Direction[] turns) {
        Direction[] dirs = new Direction[turns.length];
        for (int i = 0; i < turns.length; i++){
            dirs[i] = turn(turns[i]);
        }
        return dirs;
    }
    
    Direction(int bits) {
        this.bits = bits;
    }
    
}
