package game;

import game.MS.MSCreature;

public interface SaveState {
    int NO_CLICK = 1025;
    int RLE_MULTIPLE = 0x7F;
    int RLE_END = 0x7E;
    byte COMPRESSED_V2 = 7;
    byte COMPRESSED_V1 = 5;

    /**
     * Get chip from a savestate
     * @param savestate a byte[] savestate
     * @return A creature containing chip
     */
    static MSCreature getChip(byte[] savestate){
        return new MSCreature(((savestate[1] & 0xFF) << 8) | (savestate[2] & 0xFF)); //TODO: FIX!
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
