package game;

import java.util.List;

public class SavestateWriter {

    private final byte[] bytes;
    private int index;

    public void write(int n) {
        bytes[index] = (byte) n;
        index++;
    }
    public void write(byte[] b) {
        System.arraycopy(b, 0, bytes, index, b.length);
        index += b.length;
    }
    public void writeShort(int n){
        write(n >>> 8);
        write(n);
    }
    public void writeInt(int n){
        write(n >>> 24);
        write(n >>> 16);
        write(n >>> 8);
        write(n);
    }
    public void writeShorts(short[] a){
        for (short s : a){
            writeShort(s);
        }
    }
    public void writeShortMonsterArray(Creature[] monsters){
        for (Creature monster : monsters) writeShort(monster.bits());
    }
    public void writeIntMonsterArray(Creature[] monsters){
        for (Creature monster : monsters) writeInt(monster.bits());
    }
    public void writeShortMonsterList(List<Creature> monsters){
        for (Creature monster : monsters) writeShort(monster.bits());
    }
    public void writeIntMonsterList(List<Creature> monsters){
        for (Creature monster : monsters) writeInt(monster.bits());
    }
    public void writeBool(boolean n) {
        if (n) write(1);
        else write(0);
    }
    public void writeBools(boolean[] list) {
        for (boolean b : list) {
            if (b)
                write(1);
            else
                write(0);
        }
    }

    public byte[] toByteArray() {
        return bytes;
    }

    public SavestateWriter(int size) {
        bytes = new byte[size];
    }
}