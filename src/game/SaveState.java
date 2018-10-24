package game;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.BitSet;

public class SaveState {

    Tile[] layerBG;
    Tile[] layerFG;
    Creature chip;
    int timer;
    int chipsLeft;
    short[] keys;
    short[] boots;
    RNG rng;
    int mouseClick;
    BitSet traps;
    byte[] moves;
    CreatureList monsterList;
    SlipList slipList;
    

    public byte[] save(){
        byte[] traps = this.traps.toByteArray();
        int length = 4                              // Length
                + 2                                 // Version
                + 2                                 // Chip
                + 32 * 32                           // BG Layer
                + 32 * 32                           // FG Layer
                + 2                                 // Timer
                + 2                                 // Chips left
                + 8                                 // Keys
                + 8                                 // Boots
                + 4                                 // RNG
                + 2                                 // Mouse click
                + 2 + traps.length                  // Traps
                + 4 + moves.length                  // Moves
                + 2 + 2 * monsterList.list.length   // Monster list
                + 2 + 2 * slipList.size();          // Slip list
        SavestateWriter writer = new SavestateWriter(length);
        writer.writeInt(length);
        writer.writeShort(1);
        writer.writeShort((short) chip.bits());
        writer.writeLayer(layerBG);
        writer.writeLayer(layerFG);
        writer.writeShort(timer);
        writer.writeShort(chipsLeft);
        writer.writeShorts(keys);
        writer.writeShorts(boots);
        writer.writeInt(rng.currentValue);
        writer.writeShort(mouseClick);
        writer.writeShort(traps.length);
        writer.writeBytes(traps);
        writer.writeInt(moves.length);
        writer.writeBytes(moves);
        writer.writeShort(monsterList.list.length);
        writer.writeMonsterArray(monsterList.list);
        writer.writeShort(slipList.size());
        writer.writeMonsterList(slipList);
        
        return writer.toByteArray();
    }
    
    public static Creature getChip(byte[] savestate){
        return new Creature(((savestate[6] & 0xFF) << 8) | (savestate[7] & 0xFF));
    }

    public void load(byte[] savestate){
        SavestateReader reader = new SavestateReader(savestate);
        int length = reader.readInt();
        int version = reader.readShort();
        chip = new Creature(reader.readShort());
        layerBG = reader.readLayer(32*32);
        layerFG = reader.readLayer(32*32);
        timer = (short) reader.readShort();
        chipsLeft = reader.readShort();
        keys = reader.readShorts(4);
        boots = reader.readShorts(4);
        rng.currentValue = (reader.readInt());
        mouseClick = reader.readShort();
        traps = BitSet.valueOf(reader.readBytes(reader.readShort()));
        moves = reader.readBytes(reader.readInt());
        monsterList.list = reader.readMonsterArray(reader.readShort());
        slipList = new SlipList(reader.readMonsterArray(reader.readShort()), slipList.getLevel());
    }

    public SaveState(Tile[] layerBG, Tile[] layerFG, CreatureList monsterList, SlipList slipList, Creature chip,
                     byte[] moves, int timer, int chipsLeft, short[] keys, short[] boots, RNG rng, int mouseClick,
                     BitSet traps){
        this.layerBG = layerBG;
        this.layerFG = layerFG;
        this.monsterList = monsterList;
        this.slipList = slipList;
        this.chip = chip;
        this.moves = moves;
        this.timer = timer;
        this.chipsLeft = chipsLeft;
        this.keys = keys;
        this.boots = boots;
        this.rng = rng;
        this.mouseClick = mouseClick;
        this.traps = traps;
    }

    private class SavestateReader extends ByteArrayInputStream{

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
        Tile[] readLayer(int length){
            Tile[] out = new Tile[length];
            for (int i = 0; i < length; i++){
                out[i] = Tile.fromOrdinal(readShort());
            }
            return out;
        }
        Creature[] readMonsterArray(int length){
            Creature[] monsters = new Creature[length];
            for (int i = 0; i < length; i++){
                monsters[i] = new Creature(readShort());
            }
            return monsters;
        }
        ArrayList<Creature> readMonsterList(int length){
            ArrayList<Creature> monsters = new ArrayList<Creature>();
            for (int i = 0; i < length; i++){
                monsters.add(new Creature(readShort()));
            }
            return monsters;
        }

        public SavestateReader(byte[] b){
            super(b);
        }

    }

    private class SavestateWriter extends ByteArrayOutputStream{

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
        /*
        public void writeBytes(byte[] a){
            for (int i = 0; i < a.length; i++){
                write(a[i]);
            }
        }
        */
        void writeShorts(short[] a){
            for (int i = 0; i < a.length; i++){
                writeShort(a[i]);
            }
        }
        void writeLayer(Tile[] layer){
            for (int i = 0; i < layer.length; i++){
                writeShort(layer[i].ordinal());
            }
        }
        void writeMonsterArray(Creature[] monsters){
            for (Creature monster : monsters) writeShort(monster.bits());
        }
        void writeMonsterList(ArrayList<Creature> monsters){
            for (Creature monster : monsters) writeShort(monster.bits());
        }

        SavestateWriter(int length){
            super(length);
        }

    }

}