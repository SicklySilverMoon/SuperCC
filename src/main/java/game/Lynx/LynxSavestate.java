package game.Lynx;

import game.*;

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

    protected boolean lastMoveForced;

    @Override
    public byte[] save() {
        int length =
                1 +                             // version
                1 +                             // ruleset
                1024 +                          // layerFG
                4 +                             // tick number
                2 +                             // chips left
                4 * 2 +                         // keys
                4 * 1 +                         // boots
                3 * 4 +                         // rng
                1 +                             // RFF
                2 +                             // monsterlist size
                monsterList.size() * 4 +        // monsterlist
                1024 +                          // claimed array
                1;                              // lastMoveForced

        SavestateWriter writer = new SavestateWriter(length);
        writer.write(UNCOMPRESSED_V2); //Every time this is updated also update compress() in SavestateManager.java
        writer.write(Ruleset.LYNX.ordinal());
        writer.write(layerFG.getBytes());
        writer.writeInt(tickNumber);
        writer.writeShort(chipsLeft);
        writer.writeShorts(keys);
        writer.write(boots);
        writer.writeInt(rng.getCurrentValue());
        writer.writeInt(rng.getPRNG1());
        writer.writeInt(rng.getPRNG2());
        writer.write(rffDirection.ordinal());
        writer.writeShort(monsterList.size());
        writer.writeIntMonsterArray(monsterList.getCreatures());
        writer.writeBools(monsterList.getClaimedArray());
        writer.writeBool(lastMoveForced);

        return writer.toByteArray();
    }

    @Override
    public void load(byte[] savestate) {
        SavestateReader reader = new SavestateReader(savestate);
        int version = reader.read();
        if (version == UNCOMPRESSED_V2 || version == COMPRESSED_V2) {
            if (reader.read() != Ruleset.LYNX.ordinal())
                throw new UnsupportedOperationException("Can only load lynx savestates!");
            layerFG.load(reader.readLayer(version));
            tickNumber = reader.readInt();
            chipsLeft = (short) reader.readShort();
            keys = reader.readShorts(4);
            boots = reader.readBytes(4);
            rng.setCurrentValue(reader.readInt());
            rng.setPRNG1(reader.readInt());
            rng.setPRNG2(reader.readInt());
            rffDirection = Direction.fromOrdinal(reader.read());
            monsterList.setCreatures(reader.readLynxMonsterArray(reader.readShort()));
            monsterList.setClaimedArray(reader.readBools(1024));
            lastMoveForced = reader.readBool();

            chip = monsterList.get(0);
        }
    }

    protected LynxSavestate(Layer layerFG, CreatureList monsterList, Creature chip,
                            int chipsLeft, short[] keys, byte[] boots, RNG rng){
        this.layerFG = layerFG;
        this.monsterList = monsterList;
        this.chip = chip;
        this.tickNumber = 0;
        this.chipsLeft = chipsLeft;
        this.keys = keys;
        this.boots = boots;
        this.rng = rng;
    }
}
