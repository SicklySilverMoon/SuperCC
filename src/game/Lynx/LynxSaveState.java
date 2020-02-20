package game.Lynx;

import game.*;

import java.util.BitSet;

public class LynxSaveState implements SaveState {

    private static final byte UNCOMPRESSED_V2 = 6;
    private static final byte UNCOMPRESSED_V1 = 4;

    public Layer layerBG;
    public Layer layerFG;
    public LynxCreature chip;
    public int tickNumber;
    public int chipsLeft;
    public short[] keys;
    public byte[] boots;
    public RNG rng;
    public LynxCreatureList monsterList;
    public SlipList slipList;

    protected int mouseGoal;
    protected BitSet traps;

    @Override
    public byte[] save() {
        return new byte[0];
    }

    @Override
    public void load(byte[] savestate) {

    }

    protected LynxSaveState(Layer layerBG, Layer layerFG, LynxCreatureList monsterList, SlipList slipList, LynxCreature chip,
                          int timer, int chipsLeft, short[] keys, byte[] boots, RNG rng, int mouseGoal, BitSet traps){
        this.layerBG = layerBG;
        this.layerFG = layerFG;
        this.monsterList = monsterList;
        this.slipList = slipList;
        this.chip = chip;
        this.tickNumber = 0;
        this.chipsLeft = chipsLeft;
        this.keys = keys;
        this.boots = boots;
        this.rng = rng;
        this.mouseGoal = mouseGoal;
        this.traps = traps;
    }
}
