package game;

public enum Tile {

    /* 00 EMPTY SPACE          */    FLOOR,
    /* 01 WALL                 */    WALL,
    /* 02 CHIP                 */    CHIP,
    /* 03 WATER                */    WATER,
    /* 04 FIRE                 */    FIRE,
    /* 05 INVISIBLE WALL_PERM. */    HIDDENWALL_PERM,
    /* 06 BLOCKED NORTH        */    THIN_WALL_UP,
    /* 07 BLOCKED WEST         */    THIN_WALL_LEFT,
    /* 08 BLOCKED SOUTH        */    THIN_WALL_DOWN,
    /* 09 BLOCKED EAST         */    THIN_WALL_RIGHT,
    /* 0A BLOCK                */    BLOCK,
    /* 0B DIRT                 */    DIRT,
    /* 0C ICE                  */    ICE,
    /* 0D FORCE SOUTH          */    FF_DOWN,
    /* 0E CLONING BLOCK N      */    BLOCK_UP,
    /* 0F CLONING BLOCK W      */    BLOCK_LEFT,
    /* 10 CLONING BLOCK S      */    BLOCK_DOWN,
    /* 11 CLONING BLOCK E      */    BLOCK_RIGHT,
    /* 12 FORCE NORTH          */    FF_UP,
    /* 13 FORCE EAST           */    FF_RIGHT,
    /* 14 FORCE WEST           */    FF_LEFT,
    /* 15 EXIT                 */    EXIT,
    /* 16 BLUE DOOR            */    DOOR_BLUE,
    /* 17 RED DOOR             */    DOOR_RED,
    /* 18 GREEN DOOR           */    DOOR_GREEN,
    /* 19 YELLOW DOOR          */    DOOR_YELLOW,
    /* 1A SE ICE SLIDE         */    ICE_SLIDE_SOUTHEAST,
    /* 1B SW ICE SLIDE         */    ICE_SLIDE_SOUTHWEST,
    /* 1C NW ICE SLIDE         */    ICE_SLIDE_NORTHWEST,
    /* 1D NE ICE SLIDE         */    ICE_SLIDE_NORTHEAST,
    /* 1E BLUE BLOCK_TILE      */    BLUEWALL_FAKE,
    /* 1F BLUE BLOCK_WALL      */    BLUEWALL_REAL,
    /* 20 NOT USED             */    OVERLAY_BUFFER,
    /* 21 THIEF                */    THIEF,
    /* 22 SOCKET               */    SOCKET,
    /* 23 GREEN BUTTON         */    BUTTON_GREEN,
    /* 24 RED BUTTON           */    BUTTON_RED,
    /* 25 SWITCH BLOCK_CLOSED  */    TOGGLE_CLOSED,
    /* 26 SWITCH BLOCK_OPEN    */    TOGGLE_OPEN,
    /* 27 BROWN BUTTON         */    BUTTON_BROWN,
    /* 28 BLUE BUTTON          */    BUTTON_BLUE,
    /* 29 TELEPORT             */    TELEPORT,
    /* 2A BOMB                 */    BOMB,
    /* 2B TRAP                 */    TRAP,
    /* 2C INVISIBLE WALL_TEMP. */    HIDDENWALL_TEMP,
    /* 2D GRAVEL               */    GRAVEL,
    /* 2E PASS ONCE            */    POP_UP_WALL,
    /* 2F HINT                 */    HINT,
    /* 30 BLOCKED SE           */    THIN_WALL_DOWN_RIGHT,
    /* 31 CLONING MACHINE      */    CLONE_MACHINE,
    /* 32 FORCE ALL DIRECTIONS */    FF_RANDOM,
    /* 33 DROWNING CHIP        */    DROWNED_CHIP,
    /* 34 BURNED CHIP          */    BURNED_CHIP,
    /* 35 BURNED CHIP          */    BOMBED_CHIP,
    /* 36 NOT USED             */    UNUSED_36,
    /* 37 NOT USED             */    UNUSED_37,
    /* 38 NOT USED/ICE BLOCK   */    ICEBLOCK_STATIC,
    /* 39 CHIP IN EXIT         */    EXITED_CHIP,
    /* 3A EXIT - END GAME      */    EXIT_EXTRA_1,
    /* 3B EXIT - END GAME      */    EXIT_EXTRA_2,
    /* 3C CHIP SWIMMING N      */    CHIP_SWIMMING_NORTH,
    /* 3D CHIP SWIMMING W      */    CHIP_SWIMMING_WEST,
    /* 3E CHIP SWIMMING S      */    CHIP_SWIMMING_SOUTH,
    /* 3F CHIP SWIMMING E      */    CHIP_SWIMMING_EAST,
    /* 40 BUG N                */    BUG_NORTH,
    /* 41 BUG W                */    BUG_WEST,
    /* 42 BUG S                */    BUG_SOUTH,
    /* 43 BUG E                */    BUG_EAST,
    /* 44 FIREBALL N           */    FIREBALL_NORTH,
    /* 45 FIREBALL W           */    FIREBALL_WEST,
    /* 46 FIREBALL S           */    FIREBALL_SOUTH,
    /* 47 FIREBALL E           */    FIREBALL_EAST,
    /* 48 PINK BALL N          */    BALL_NORTH,
    /* 49 PINK BALL W          */    BALL_WEST,
    /* 4A PINK BALL S          */    BALL_SOUTH,
    /* 4B PINK BALL E          */    BALL_EAST,
    /* 4C TANK_STATIONARY N               */    TANK_NORTH,
    /* 4D TANK_STATIONARY W               */    TANK_WEST,
    /* 4E TANK_STATIONARY S               */    TANK_SOUTH,
    /* 4F TANK_STATIONARY E               */    TANK_EAST,
    /* 50 GLIDER N             */    GLIDER_NORTH,
    /* 51 GLIDER W             */    GLIDER_WEST,
    /* 52 GLIDER S             */    GLIDER_SOUTH,
    /* 53 GLIDER E             */    GLIDER_EAST,
    /* 54 TEETH N              */    TEETH_NORTH,
    /* 55 TEETH W              */    TEETH_WEST,
    /* 56 TEETH S              */    TEETH_SOUTH,
    /* 57 TEETH E              */    TEETH_EAST,
    /* 58 WALKER N             */    WALKER_NORTH,
    /* 59 WALKER W             */    WALKER_WEST,
    /* 5A WALKER S             */    WALKER_SOUTH,
    /* 5B WALKER E             */    WALKER_EAST,
    /* 5C BLOB N               */    BLOB_NORTH,
    /* 5D BLOB W               */    BLOB_WEST,
    /* 5E BLOB S               */    BLOB_SOUTH,
    /* 5F BLOB E               */    BLOB_EAST,
    /* 60 PARAMECIUM N         */    PARAMECIUM_NORTH,
    /* 61 PARAMECIUM W         */    PARAMECIUM_WEST,
    /* 62 PARAMECIUM S         */    PARAMECIUM_SOUTH,
    /* 63 PARAMECIUM E         */    PARAMECIUM_EAST,
    /* 64 BLUE KEY             */    KEY_BLUE,
    /* 65 RED KEY              */    KEY_RED,
    /* 66 GREEN KEY            */    KEY_GREEN,
    /* 67 YELLOW KEY           */    KEY_YELLOW,
    /* 68 FLIPPERS             */    BOOTS_WATER,
    /* 69 FIRE BOOTS           */    BOOTS_FIRE,
    /* 6A ICE SKATES           */    BOOTS_ICE,
    /* 6B SUCTION BOOTS        */    BOOTS_SLIDE,
    /* 6C CHIP N               */    CHIP_UP,
    /* 6D CHIP W               */    CHIP_LEFT,
    /* 6E CHIP S               */    CHIP_DOWN,
    /* 6F CHIP E               */    CHIP_RIGHT;
    
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
    public boolean isMovingBlock(){
        return BLOCK_UP.ordinal() <= ordinal() && ordinal() <= BLOCK_RIGHT.ordinal();
    }
    public boolean isMonster(){
        return BUG_NORTH.ordinal() <= ordinal() && ordinal() <= PARAMECIUM_EAST.ordinal();
    }
    public boolean isCreature(){
        return isMonster() || isMovingBlock();
    }
    public boolean isTransparent(){
        return ordinal() >= BUG_NORTH.ordinal();
    }

}
