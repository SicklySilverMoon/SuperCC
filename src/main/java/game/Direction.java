package game;

public enum Direction {
    
    UP(0b0000),
    LEFT(0b0001),
    DOWN(0b0010),
    RIGHT(0b0011),
    UP_LEFT(0b0100),
    DOWN_LEFT(0b0101),
    DOWN_RIGHT(0b0110),
    UP_RIGHT(0b0111),
    NONE(0b1000),
    WALKER_TURN(0b1001), //WOW! This is extremely hacky!
    BLOB_TURN(0b1010);

    public static final Direction TURN_LEFT = LEFT, TURN_RIGHT = RIGHT,
        TURN_AROUND = DOWN, TURN_FORWARD = UP;
    
    private static final Direction[] allDirections = Direction.values();
    public static Direction fromOrdinal(int ordinal){
        return allDirections[ordinal];
    }
    
    private final int bits;

    public int getBits() {
        return bits;
    }

    public Direction turn(Direction turn) {
        if (turn.bits > RIGHT.bits || this.bits > RIGHT.bits) //meaningless for non cardinal directions on either side
            return this;
        return fromOrdinal((ordinal() + turn.ordinal()) & 0b11);
    }

    public Direction[] turn(Direction[] turns) {
        Direction[] dirs = new Direction[turns.length];
        for (int i = 0; i < turns.length; i++){
            dirs[i] = turn(turns[i]);
        }
        return dirs;
    }

    public boolean isDiagonal() {
        return bits >= UP_LEFT.bits && bits <= UP_RIGHT.bits;
    }

    public boolean isComponent(Direction comp) {
        if (!isDiagonal()) //only makes sense for diags
            return false;
        switch (this) {
            case UP_LEFT:
                return comp == UP || comp == LEFT;
            case DOWN_LEFT:
                return comp == DOWN || comp == LEFT;
            case DOWN_RIGHT:
                return comp == DOWN || comp == RIGHT;
            case UP_RIGHT:
                return comp == UP || comp == RIGHT;
        }
        return false;
    }

    public Direction[] decompose() {
        if (!isDiagonal())
            return new Direction[] {this};
        switch (this) {
            case UP_LEFT:
                return new Direction[] {UP, LEFT};
            case DOWN_LEFT:
                return new Direction[] {DOWN, LEFT};
            case DOWN_RIGHT:
                return new Direction[] {DOWN, RIGHT};
            case UP_RIGHT:
                return new Direction[] {UP, RIGHT};
        }
        return new Direction[] {NONE};
    }

    public static Direction fromTWS(int n) {
        n &= 0b11;
        switch (n) {
            case 0b00:
                return RIGHT;
            case 0b01:
                return UP;
            case 0b10:
                return LEFT;
            default:
                return DOWN;
        }
    }

    Direction(int bits) {
        this.bits = bits;
    }
    
}
