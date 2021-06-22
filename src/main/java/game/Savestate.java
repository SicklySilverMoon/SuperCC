package game;

import game.Lynx.LynxCreature;
import game.MS.MSCreature;

public interface Savestate {
    int NO_CLICK = 1025;
    int RLE_MULTIPLE = 0x7F;
    int RLE_END = 0x7E;
    byte UNCOMPRESSED_V2 = 6;
    byte UNCOMPRESSED_V1 = 4;
    byte COMPRESSED_V2 = 7;
    byte COMPRESSED_V1 = 5;

    /**
     * Get chip from a savestate
     * @param savestate a byte[] savestate
     * @return A creature containing chip
     */
    static Creature getChip(byte[] savestate){
        if (savestate[1] == Ruleset.MS.ordinal())
            return new MSCreature(((savestate[2] & 0xFF) << 8) | (savestate[3] & 0xFF));
        else if (savestate[1] == Ruleset.LYNX.ordinal()) { //Yeah yeah hardcoded values into an array, Chip is always present so its safe
            int x = (savestate[1059] & 0xFF) << 24 | (savestate[1060] & 0xFF) << 16
                    | (savestate[1061] & 0xFF) << 8 | (savestate[1062] & 0xFF);
            return new LynxCreature(x);
        }

        return null;
    }

    /**
     * Write an uncompressed savestate
     * @return a savestate
     */
    byte[] save();

    /**
     * load a savestate
     * @param savestate the savestate to load
     */
    void load(byte[] savestate);
}
