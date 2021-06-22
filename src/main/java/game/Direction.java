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

    public static final Direction[] CARDINALS = new Direction[] {UP, LEFT, DOWN, RIGHT};
    private static final Direction[] MOVEMENTS = new Direction[] {UP, LEFT, DOWN, RIGHT, UP_LEFT, DOWN_LEFT,
            DOWN_RIGHT, UP_RIGHT};

    public static Direction fromOrdinal(int ordinal){
        return values()[ordinal];
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
        return switch (this) {
            case UP_LEFT -> comp == UP || comp == LEFT;
            case DOWN_LEFT -> comp == DOWN || comp == LEFT;
            case DOWN_RIGHT -> comp == DOWN || comp == RIGHT;
            case UP_RIGHT -> comp == UP || comp == RIGHT;
            default -> false;
        };
    }

    public Direction[] decompose() {
        if (!isDiagonal())
            return new Direction[] {this};
        return switch (this) {
            case UP_LEFT -> new Direction[] {UP, LEFT};
            case DOWN_LEFT -> new Direction[] {DOWN, LEFT};
            case DOWN_RIGHT -> new Direction[] {DOWN, RIGHT};
            case UP_RIGHT -> new Direction[] {UP, RIGHT};
            default -> new Direction[] {NONE};
        };
    }

    public static Direction fromTWS(int n) {
        n &= 0b11;
        return switch (n) {
            case 0b00 -> RIGHT;
            case 0b01 -> UP;
            case 0b10 -> LEFT;
            default -> DOWN;
        };
    }

    public int toTWS() {
        return switch (this) {
            case UP -> 0;
            case LEFT -> 1;
            case DOWN -> 2;
            case RIGHT -> 3;
            default -> 0; //failsafe
        };
    }

    Direction(int bits) {
        this.bits = bits;
    }
    
}
