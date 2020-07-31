package game.Lynx;

import game.*;

import java.io.ByteArrayInputStream;
import java.util.BitSet;
import java.util.List;

public class LynxSaveState implements SaveState {

    private static final byte UNCOMPRESSED_V2 = 6;
    private static final byte UNCOMPRESSED_V1 = 4;
    public Layer layerFG;
    public LynxCreature chip;
    public int tickNumber;
    public int chipsLeft;
    public short[] keys;
    public byte[] boots;
    public RNG rng;
    public CreatureList monsterList;

    protected int mouseGoal;
    protected BitSet traps;

    @Override
    public byte[] save() {
        byte[] traps = this.traps.toByteArray();

        int length =
                1 +                             // version
                        2 +                             // chip
                        1024 +                          // layerBG
                        1024 +                          // layerFG
                        2 +                             // tick number
                        2 +                             // chips left
                        4 * 2 +                         // keys
                        4 * 1 +                         // boots
                        4 +                             // rng
                        2 +                             // traps length
                        traps.length +                  // traps
                        2 +                             // monsterlist size
                        monsterList.size() * 2;         // monsterlist

        SavestateWriter writer = new SavestateWriter(length);
        writer.write(UNCOMPRESSED_V2); //Every time this is updated also update compress() in SavestateManager.java
        writer.writeShort((short) chip.bits());
        writer.write(layerFG.getBytes());
        writer.writeShort(tickNumber);
        writer.writeShort(chipsLeft);
        writer.writeShorts(keys);
        writer.write(boots);
        writer.writeInt(rng.getCurrentValue());
        writer.writeShort(traps.length);
        writer.write(traps);
        writer.writeShort(monsterList.size());
        writer.writeMonsterArray(monsterList.getCreatures());

        return writer.toByteArray();
    }

    @Override
    public void load(byte[] savestate) {
        SavestateReader reader = new SavestateReader(savestate);
        int version = reader.read();
        if (version == UNCOMPRESSED_V2 || version == COMPRESSED_V2) {
            chip = new LynxCreature(reader.readShort());
            layerFG.load(reader.readLayer(version));
            tickNumber = (short) reader.readShort();
            chipsLeft = (short) reader.readShort();
            keys = reader.readShorts(4);
            boots = reader.readBytes(4);
            rng.setCurrentValue(reader.readInt());
            traps = BitSet.valueOf(reader.readBytes(reader.readShort()));
            monsterList.setCreatures(reader.readLynxMonsterArray(reader.readShort()));
        }
    }

    protected LynxSaveState(Layer layerFG, CreatureList monsterList, LynxCreature chip,
                          int timer, int chipsLeft, short[] keys, byte[] boots, RNG rng, int mouseGoal, BitSet traps){
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
