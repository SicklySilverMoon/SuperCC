package game.Lynx;

import game.*;

import java.util.BitSet;

public class LynxSavestate implements Savestate {

    private static final byte UNCOMPRESSED_V2 = 6;
    private static final byte UNCOMPRESSED_V1 = 4;
    public Layer layerFG;
    public Creature chip;
    public int tickNumber;
    public int chipsLeft;
    public short[] keys;
    public byte[] boots;
    public RNG rng;
    public Direction rffDirection;
    public CreatureList monsterList;

    protected int mouseGoal;
    protected BitSet traps;

    @Override
    public byte[] save() {
        byte[] traps = this.traps.toByteArray();

        int length =
                1 +                             // version
                1024 +                          // layerFG
                4 +                             // tick number
                2 +                             // chips left
                4 * 2 +                         // keys
                4 * 1 +                         // boots
                4 * 3 +                         // rng
                1 +                             // RFF
                2 +                             // traps length
                traps.length +                  // traps
                2 +                             // monsterlist size
                monsterList.size() * 4;         // monsterlist

        SavestateWriter writer = new SavestateWriter(length);
        writer.write(UNCOMPRESSED_V2); //Every time this is updated also update compress() in SavestateManager.java
        writer.write(layerFG.getBytes());
        writer.writeInt(tickNumber);
        writer.writeShort(chipsLeft);
        writer.writeShorts(keys);
        writer.write(boots);
        writer.writeInt(rng.getCurrentValue());
        writer.writeInt(rng.getPRNG1());
        writer.writeInt(rng.getPRNG2());
        writer.write(rffDirection.ordinal());
        writer.writeShort(traps.length);
        writer.write(traps);
        writer.writeShort(monsterList.size());
        writer.writeIntMonsterArray(monsterList.getCreatures());

        return writer.toByteArray();
    }

    @Override
    public void load(byte[] savestate) {
        SavestateReader reader = new SavestateReader(savestate);
        int version = reader.read();
        if (version == UNCOMPRESSED_V2 || version == COMPRESSED_V2) {
            layerFG.load(reader.readLayer(version));
            tickNumber = reader.readInt();
            chipsLeft = (short) reader.readShort();
            keys = reader.readShorts(4);
            boots = reader.readBytes(4);
            rng.setCurrentValue(reader.readInt());
            rng.setPRNG1(reader.readInt());
            rng.setPRNG2(reader.readInt());
            rffDirection = Direction.fromOrdinal(reader.read());
            traps = BitSet.valueOf(reader.readBytes(reader.readShort()));
            monsterList.setCreatures(reader.readLynxMonsterArray(reader.readShort()));
            chip = monsterList.get(0);
        }
    }

    protected LynxSavestate(Layer layerFG, CreatureList monsterList, Creature chip,
                            int chipsLeft, short[] keys, byte[] boots, RNG rng, int mouseGoal, BitSet traps){
        this.layerFG = layerFG;
        this.monsterList = monsterList;
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
