package game;

public enum Tile {

    /* 00 EMPTY SPACE          */    FLOOR("Floor"),
    /* 01 WALL                 */    WALL("Wall"),
    /* 02 CHIP                 */    CHIP("Chip"),
    /* 03 WATER                */    WATER("Water"),
    /* 04 FIRE                 */    FIRE("Fire"),
    /* 05 INVISIBLE WALL_PERM. */    INVISIBLE_WALL("Invisible Wall"),
    /* 06 BLOCKED NORTH        */    THIN_WALL_UP("Thin Wall - Up"),
    /* 07 BLOCKED WEST         */    THIN_WALL_LEFT("Thin Wall - Left"),
    /* 08 BLOCKED SOUTH        */    THIN_WALL_DOWN("Thin Wall - Down"),
    /* 09 BLOCKED EAST         */    THIN_WALL_RIGHT("Thin Wall - Right"),
    /* 0A BLOCK                */    BLOCK("Block"),
    /* 0B DIRT                 */    DIRT("Dirt"),
    /* 0C ICE                  */    ICE("Ice"),
    /* 0D FORCE SOUTH          */    FF_DOWN("Force Floor - Down"),
    /* 0E CLONING BLOCK N      */    BLOCK_UP("Block - Up"),
    /* 0F CLONING BLOCK W      */    BLOCK_LEFT("Block - Left"),
    /* 10 CLONING BLOCK S      */    BLOCK_DOWN("Block - Down"),
    /* 11 CLONING BLOCK E      */    BLOCK_RIGHT("Block - Right"),
    /* 12 FORCE NORTH          */    FF_UP("Force Floor - Up"),
    /* 13 FORCE EAST           */    FF_RIGHT("Force Floor - Right"),
    /* 14 FORCE WEST           */    FF_LEFT("Force Floor - Left"),
    /* 15 EXIT                 */    EXIT("Exit"),
    /* 16 BLUE DOOR            */    DOOR_BLUE("Blue Door"),
    /* 17 RED DOOR             */    DOOR_RED("Red Door"),
    /* 18 GREEN DOOR           */    DOOR_GREEN("Green Door"),
    /* 19 YELLOW DOOR          */    DOOR_YELLOW("Yellow Door"),
    /* 1A SE ICE SLIDE         */    ICE_SLIDE_SOUTHEAST("Ice Turn - Down/Right"),
    /* 1B SW ICE SLIDE         */    ICE_SLIDE_SOUTHWEST("Ice Turn - Down/Left"),
    /* 1C NW ICE SLIDE         */    ICE_SLIDE_NORTHWEST("Ice Turn - Up/Left"),
    /* 1D NE ICE SLIDE         */    ICE_SLIDE_NORTHEAST("Ice Turn - Up/Right"),
    /* 1E BLUE BLOCK_TILE      */    BLUEWALL_FAKE("Fake Blue Wall"),
    /* 1F BLUE BLOCK_WALL      */    BLUEWALL_REAL("Real Blue Wall"),
    /* 20 NOT USED             */    OVERLAY_BUFFER("Unused"),
    /* 21 THIEF                */    THIEF("Thief"),
    /* 22 SOCKET               */    SOCKET("Socket"),
    /* 23 GREEN BUTTON         */    BUTTON_GREEN("Green Button"),
    /* 24 RED BUTTON           */    BUTTON_RED("Red Button"),
    /* 25 SWITCH BLOCK_CLOSED  */    TOGGLE_CLOSED("Closed Toggle Door"),
    /* 26 SWITCH BLOCK_OPEN    */    TOGGLE_OPEN("Open Toggle Door"),
    /* 27 BROWN BUTTON         */    BUTTON_BROWN("Brown Button"),
    /* 28 BLUE BUTTON          */    BUTTON_BLUE("Blue Button"),
    /* 29 TELEPORT             */    TELEPORT("Teleporter"),
    /* 2A BOMB                 */    BOMB("Bomb"),
    /* 2B TRAP                 */    TRAP("Trap"),
    /* 2C INVISIBLE WALL_TEMP. */    HIDDENWALL_TEMP("Appearing Wall"),
    /* 2D GRAVEL               */    GRAVEL("Gravel"),
    /* 2E PASS ONCE            */    POP_UP_WALL("Pop Up Wall"),
    /* 2F HINT                 */    HINT("Hint"),
    /* 30 BLOCKED SE           */    THIN_WALL_DOWN_RIGHT("Thin Wall - Down Right"),
    /* 31 CLONING MACHINE      */    CLONE_MACHINE("Clone Machine"),
    /* 32 FORCE ALL DIRECTIONS */    FF_RANDOM("Force Floor - Random"),
    /* 33 DROWNING CHIP        */    DROWNED_CHIP("Drowned Chip"),
    /* 34 BURNED CHIP          */    BURNED_CHIP("Burned Chip"),
    /* 35 BURNED CHIP          */    BOMBED_CHIP("Bombed Chip"),
    /* 36 NOT USED             */    UNUSED_36("Unused"),
    /* 37 NOT USED             */    UNUSED_37("Unused"),
    /* 38 NOT USED/ICE BLOCK   */    ICE_BLOCK("Ice Block"),
    /* 39 CHIP IN EXIT         */    EXITED_CHIP("Exited Chip"),
    /* 3A EXIT - END GAME      */    EXIT_EXTRA_1("Exit Animation 2"),
    /* 3B EXIT - END GAME      */    EXIT_EXTRA_2("Exit Animation 3"),
    /* 3C CHIP SWIMMING N      */    CHIP_SWIMMING_UP("Swimming Chip - Up"),
    /* 3D CHIP SWIMMING W      */    CHIP_SWIMMING_LEFT("Swimming Chip - Left"),
    /* 3E CHIP SWIMMING S      */    CHIP_SWIMMING_DOWN("Swimming Chip - Down"),
    /* 3F CHIP SWIMMING E      */    CHIP_SWIMMING_RIGHT("Swimming Chip - Right"),
    /* 40 BUG N                */    BUG_UP("Bug - Up"),
    /* 41 BUG W                */    BUG_LEFT("Bug - Left"),
    /* 42 BUG S                */    BUG_DOWN("Bug - Down"),
    /* 43 BUG E                */    BUG_RIGHT("Bug - Right"),
    /* 44 FIREBALL N           */    FIREBALL_UP("Fireball - Up"),
    /* 45 FIREBALL W           */    FIREBALL_LEFT("Fireball - Left"),
    /* 46 FIREBALL S           */    FIREBALL_DOWN("Fireball - Down"),
    /* 47 FIREBALL E           */    FIREBALL_RIGHT("Fireball - Right"),
    /* 48 PINK BALL N          */    BALL_UP("Pink Ball - Up"),
    /* 49 PINK BALL W          */    BALL_LEFT("Pink Ball - Left"),
    /* 4A PINK BALL S          */    BALL_DOWN("Pink Ball - Down"),
    /* 4B PINK BALL E          */    BALL_RIGHT("Pink Ball - Right"),
    /* 4C TANK N               */    TANK_UP("Tank - Up"),
    /* 4D TANK W               */    TANK_LEFT("Tank - Left"),
    /* 4E TANK S               */    TANK_DOWN("Tank - Down"),
    /* 4F TANK E               */    TANK_RIGHT("Tank - Right"),
    /* 50 GLIDER N             */    GLIDER_UP("Glider - Up"),
    /* 51 GLIDER W             */    GLIDER_LEFT("Glider - Left"),
    /* 52 GLIDER S             */    GLIDER_DOWN("Glider - Down"),
    /* 53 GLIDER E             */    GLIDER_RIGHT("Glider - Right"),
    /* 54 TEETH N              */    TEETH_UP("Teeth - Up"),
    /* 55 TEETH W              */    TEETH_LEFT("Teeth - Left"),
    /* 56 TEETH S              */    TEETH_DOWN("Teeth - Down"),
    /* 57 TEETH E              */    TEETH_RIGHT("Teeth - Right"),
    /* 58 WALKER N             */    WALKER_UP("Walker - Up"),
    /* 59 WALKER W             */    WALKER_LEFT("Walker - Left"),
    /* 5A WALKER S             */    WALKER_DOWN("Walker - Down"),
    /* 5B WALKER E             */    WALKER_RIGHT("Walker - Right"),
    /* 5C BLOB N               */    BLOB_UP("Blob - Up"),
    /* 5D BLOB W               */    BLOB_LEFT("Blob - Left"),
    /* 5E BLOB S               */    BLOB_DOWN("Blob - Down"),
    /* 5F BLOB E               */    BLOB_RIGHT("Blob - Right"),
    /* 60 PARAMECIUM N         */    PARAMECIUM_UP("Paramecium - Up"),
    /* 61 PARAMECIUM W         */    PARAMECIUM_LEFT("Paramecium - Left"),
    /* 62 PARAMECIUM S         */    PARAMECIUM_DOWN("Paramecium - Down"),
    /* 63 PARAMECIUM E         */    PARAMECIUM_RIGHT("Paramecium - Right"),
    /* 64 BLUE KEY             */    KEY_BLUE("Blue Key"),
    /* 65 RED KEY              */    KEY_RED("Red Key"),
    /* 66 GREEN KEY            */    KEY_GREEN("Green Key"),
    /* 67 YELLOW KEY           */    KEY_YELLOW("Yellow Key"),
    /* 68 FLIPPERS             */    BOOTS_WATER("Flippers"),
    /* 69 FIRE BOOTS           */    BOOTS_FIRE("Fire Boots"),
    /* 6A ICE SKATES           */    BOOTS_ICE("Ice Skates"),
    /* 6B SUCTION BOOTS        */    BOOTS_SLIDE("Suction Boots"),
    /* 6C CHIP N               */    CHIP_UP("Chip - Up"),
    /* 6D CHIP W               */    CHIP_LEFT("Chip - Left"),
    /* 6E CHIP S               */    CHIP_DOWN("Chip - Down"),
    /* 6F CHIP E               */    CHIP_RIGHT("Chip - Right");
    
    public static final int NUM_BOOTS = 4;
    public static final int NUM_KEYS = 4;
    
    private static final Tile[] allTiles = Tile.values();
    public static Tile fromOrdinal(int ordinal){
        return allTiles[ordinal];
    }

    public boolean isIce(){
        return this.ordinal() == ICE.ordinal() ||
                (ICE_SLIDE_SOUTHEAST.ordinal() <= this.ordinal() && this.ordinal() <= ICE_SLIDE_NORTHEAST.ordinal());
    }
    public boolean isFF(){
        return this == FF_UP || this == FF_LEFT || this == FF_DOWN || this == FF_RIGHT || this == FF_RANDOM;
    }
    public boolean isSliding(){
        return isIce() || isFF() || this == TELEPORT;
    }
    public boolean isChip(){
        return CHIP_UP.ordinal() <= ordinal() && ordinal() <= CHIP_RIGHT.ordinal();
    }
    public boolean isSwimmingChip(){
        return CHIP_SWIMMING_UP.ordinal() <= ordinal() && ordinal() <= CHIP_SWIMMING_RIGHT.ordinal();
    }
    public boolean isCloneBlock(){
        return BLOCK_UP.ordinal() <= ordinal() && ordinal() <= BLOCK_RIGHT.ordinal();
    }
    public boolean isIceBlock(){
        return ICE_BLOCK.ordinal() == ordinal();
    }
    public boolean isMonster(){
        return BUG_UP.ordinal() <= ordinal() && ordinal() <= PARAMECIUM_RIGHT.ordinal();
    }
    public boolean isCreature(){
        return isMonster() || isCloneBlock();
    }
    public boolean isTransparent(){
        return ordinal() >= BUG_UP.ordinal();
    }
    public boolean isKey() {
        return KEY_BLUE.ordinal() <= ordinal() && ordinal() <= KEY_YELLOW.ordinal();
    }
    public boolean isBoot() {
        return BOOTS_WATER.ordinal() <= ordinal() && ordinal() <= BOOTS_SLIDE.ordinal();
    }
    public boolean isPickup() {
        return KEY_BLUE.ordinal() <= ordinal() && ordinal() <= BOOTS_SLIDE.ordinal();
    }
    public boolean isButton() {
        return this == BUTTON_BROWN || this == BUTTON_BLUE || this == BUTTON_RED || this == BUTTON_GREEN;
    }

    private String str;
    @Override public String toString() {
        return str;
    }
    
    private Tile(String s) {
        this.str = s;
    }

}
