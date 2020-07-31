package game.MS;

import game.*;

import java.io.IOException;
import java.util.BitSet;

public class MSSavestate implements Savestate {

    public Layer layerBG;
    public Layer layerFG;
    public MSCreature chip;
    public int tickNumber;
    public int chipsLeft;
    public short[] keys;
    public byte[] boots;
    public RNG rng;
    public CreatureList monsterList;
    public SlipList slipList;
    
    protected int mouseGoal;
    protected BitSet traps;
    protected short idleMoves;
    protected boolean voluntaryMoveAllowed;

    /**
     * Write an uncompressed savestate
     * @return a savestate
     */
    @Override public byte[] save(){
        byte[] traps = this.traps.toByteArray();
        
        int length =
            1 +                             // version
            2 +                             // chip
            1024 +                          // layerBG
            1024 +                          // layerFG
            4 +                             // tick number
            2 +                             // chips left
            4 * 2 +                         // keys
            4 * 1 +                         // boots
            4 +                             // rng
            2 +                             // mouse click
            2 +                             // traps length
            traps.length +                  // traps
            2 +                             // monsterlist size
            monsterList.size() * 2 +        // monsterlist
            2 +                             // sliplist size
            slipList.size() * 2 +           // sliplist
            2 +                             // idle moves
            4;                              // previous move type
        
        SavestateWriter writer = new SavestateWriter(length);
        writer.write(UNCOMPRESSED_V2); //Every time this is updated also update compress() in SavestateManager.java
        writer.writeShort((short) chip.bits());
        writer.write(layerBG.getBytes());
        writer.write(layerFG.getBytes());
        writer.writeInt(tickNumber);
        writer.writeShort(chipsLeft);
        writer.writeShorts(keys);
        writer.write(boots);
        writer.writeInt(rng.getCurrentValue());
        writer.writeShort(mouseGoal);
        writer.writeShort(traps.length);
        writer.write(traps);
        writer.writeShort(monsterList.size());
        writer.writeShortMonsterArray(monsterList.getCreatures());
        writer.writeShort(slipList.size());
        writer.writeShortMonsterList(slipList);
        writer.writeShort(idleMoves);
        writer.writeBool(voluntaryMoveAllowed);

        return writer.toByteArray();
    }
    
    /**
     * load a savestate
     * @param savestate the savestate to load
     */
    @Override
    public void load(byte[] savestate){
        SavestateReader reader = new SavestateReader(savestate);
        int version = reader.read();
        if (version == UNCOMPRESSED_V2 || version == COMPRESSED_V2) {
            chip = new MSCreature(reader.readShort());
            layerBG.load(reader.readLayer(version));
            layerFG.load(reader.readLayer(version));
            tickNumber = reader.readInt();
            chipsLeft = (short) reader.readShort();
            keys = reader.readShorts(4);
            boots = reader.readBytes(4);
            rng.setCurrentValue(reader.readInt());
            mouseGoal = reader.readShort();
            traps = BitSet.valueOf(reader.readBytes(reader.readShort()));
            monsterList.setCreatures(reader.readMSMonsterArray(reader.readShort()));
            slipList.setSliplist(reader.readMSMonsterArray(reader.readShort()));
            idleMoves = (short) reader.readShort();
            voluntaryMoveAllowed = reader.readBool();
        }
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    protected MSSavestate(Layer layerBG, Layer layerFG, CreatureList monsterList, SlipList slipList, MSCreature chip,
                          int chipsLeft, short[] keys, byte[] boots, RNG rng, int mouseGoal, BitSet traps){
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