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
            monsterList.setCreatures(reader.readMonsterArray(reader.readShort()));
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
                monsters[i] = new LynxCreature(readShort());
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

        void write(int n) {
            bytes[index] = (byte) n;
            index++;
        }
        void write(byte[] b) {
            System.arraycopy(b, 0, bytes, index, b.length);
            index += b.length;
        }
        void writeShort(int n){
            write(n >>> 8);
            write(n);
        }
        void writeInt(int n){
            write(n >>> 24);
            write(n >>> 16);
            write(n >>> 8);
            write(n);
        }
        void writeShorts(short[] a){
            for (short s : a){
                writeShort(s);
            }
        }
        void writeMonsterArray(Creature[] monsters){
            for (Creature monster : monsters) writeShort(monster.bits());
        }
        void writeMonsterList(List<Creature> monsters){
            for (Creature monster : monsters) writeShort(monster.bits());
        }
        void writeBool(boolean n) {
            if (n) write(1);
            else write(0);
        }

        byte[] toByteArray() {
            return bytes;
        }

        SavestateWriter(int size) {
            bytes = new byte[size];
        }

    }
}
