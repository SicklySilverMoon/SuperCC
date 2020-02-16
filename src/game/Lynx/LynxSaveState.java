package game.Lynx;

import game.Creature;
import game.Layer;
import game.RNG;
import game.SaveState;

import java.util.BitSet;

public class LynxSaveState implements SaveState {

    private static final byte UNCOMPRESSED_V2 = 6;
    private static final byte UNCOMPRESSED_V1 = 4;

    public Layer layerBG;
    public Layer layerFG;
    public Creature chip;
    public int tickNumber;
    public int chipsLeft;
    public short[] keys;
    public byte[] boots;
    public RNG rng;
//    public MSCreatureList monsterList;
//    public SlipList slipList;

    protected int mouseGoal;
    protected BitSet traps;

    @Override
    public byte[] save() {
        return new byte[0];
    }

    @Override
    public void load(byte[] savestate) {

    }
}
