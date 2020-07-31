package game.Lynx;

import game.*;

import java.io.ByteArrayInputStream;
import java.util.BitSet;
import java.util.List;

public class LynxSaveState implements SaveState {

    private static final byte UNCOMPRESSED_V2 = 6;
    private static final byte UNCOMPRESSED_V1 = 4;
    public Layer layerFG;
    public Creature chip;
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
                1024 +                          // layerFG
                2 +                             // tick number
                2 +                             // chips left
                4 * 2 +                         // keys
                4 * 1 +                         // boots
                4 +                             // rng
                2 +                             // traps length
                traps.length +                  // traps
                2 +                             // monsterlist size
                monsterList.size() * 4;         // monsterlist

        SavestateWriter writer = new SavestateWriter(length);
        writer.writeByte(UNCOMPRESSED_V2); //Every time this is updated also update compress() in SavestateManager.java
        writer.writeByteArray(layerFG.getBytes());
        writer.writeShort(tickNumber);
        writer.writeShort(chipsLeft);
        writer.writeShorts(keys);
        writer.writeByteArray(boots);
        writer.writeInt(rng.getCurrentValue());
        writer.writeShort(traps.length);
        writer.writeByteArray(traps);
        writer.writeShort(monsterList.size());
        writer.writeMonsterArray(monsterList.getCreatures());

        return writer.toByteArray();
    }

    @Override
    public void load(byte[] savestate) {
        SavestateReader reader = new SavestateReader(savestate);
        int version = reader.read();
        if (version == UNCOMPRESSED_V2 || version == COMPRESSED_V2) {
            layerFG.load(reader.readLayer(version));
            tickNumber = (short) reader.readShort();
            chipsLeft = (short) reader.readShort();
            keys = reader.readShorts(4);
            boots = reader.readBytes(4);
            rng.setCurrentValue(reader.readInt());
            traps = BitSet.valueOf(reader.readBytes(reader.readShort()));
            monsterList.setCreatures(reader.readMonsterArray(reader.readShort()));
            chip = monsterList.get(0);
        }
    }

    protected LynxSaveState(Layer layerFG, CreatureList monsterList, Creature chip,
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

    private class SavestateReader extends ByteArrayInputStream { //TODO: This entire thing can be moved to (hopefully) the general savestate interface and worked with from there

        int readUnsignedByte(){
            return read() & 0xFF;
        }
        int readShort(){
            int n = readUnsignedByte() << 8;
            return n | readUnsignedByte();
        }
        int readInt(){
            int n = readUnsignedByte() << 24;
            n |= readUnsignedByte() << 16;
            n |= readUnsignedByte() << 8;
            return n | readUnsignedByte();
        }
        byte[] readBytes(int length){
            byte[] out = new byte[length];
            for (int i = 0; i < length; i++){
                out[i] = (byte) read();
            }
            return out;
        }
        short[] readShorts(int length){
            short[] out = new short[length];
            for (int i = 0; i < length; i++){
                out[i] = (short) readShort();
            }
            return out;
        }
        byte[] readLayerRLE(){
            byte[] layerBytes = new byte[32*32];
            int tileIndex = 0;
            byte b;
            while ((b = (byte) read()) != RLE_END){
                if (b == RLE_MULTIPLE){
                    int rleLength = readUnsignedByte() + 1;
                    byte t = (byte) read();
                    for (int i = 0; i < rleLength; i++){
                        layerBytes[tileIndex++] = t;
                    }
                }
                else layerBytes[tileIndex++] = b;
            }
            return layerBytes;
        }
        byte[] readLayer(int version){
            if (version == COMPRESSED_V1 || version == COMPRESSED_V2) return readLayerRLE();
            else return readBytes(32*32);
        }
        Creature[] readMonsterArray(int length){
            Creature[] monsters = new Creature[length];
            for (int i = 0; i < length; i++){
                monsters[i] = new LynxCreature(readInt());
            }
            return monsters;
        }
        boolean readBool() {
            return read() == 1;
        }

        SavestateReader(byte[] b){
            super(b);
        }

    }

    private class SavestateWriter {

        private final byte[] bytes;
        private int index;

        void writeByte(int n) {
            bytes[index] = (byte) n;
            index++;
        }
        void writeByteArray(byte[] b) {
            System.arraycopy(b, 0, bytes, index, b.length);
            index += b.length;
        }
        void writeShort(int n){
            writeByte(n >>> 8);
            writeByte(n);
        }
        void writeInt(int n){
            writeByte(n >>> 24);
            writeByte(n >>> 16);
            writeByte(n >>> 8);
            writeByte(n);
        }
        void writeShorts(short[] a){
            for (short s : a){
                writeShort(s);
            }
        }
        void writeMonsterArray(Creature[] monsters){
            for (Creature monster : monsters) writeInt(monster.bits());
        }
        void writeMonsterList(List<Creature> monsters){
            for (Creature monster : monsters) writeInt(monster.bits());
        }
        void writeBool(boolean n) {
            if (n) writeByte(1);
            else writeByte(0);
        }

        byte[] toByteArray() {
            return bytes;
        }

        SavestateWriter(int size) {
            bytes = new byte[size];
        }

    }
}
