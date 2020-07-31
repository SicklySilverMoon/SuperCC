package game;

import game.Lynx.LynxCreature;
import game.MS.MSCreature;

import java.io.ByteArrayInputStream;

public class SavestateReader extends ByteArrayInputStream {

    public int readUnsignedByte(){
        return read() & 0xFF;
    }
    public int readShort(){
        int n = readUnsignedByte() << 8;
        return n | readUnsignedByte();
    }
    public int readInt(){
        int n = readUnsignedByte() << 24;
        n |= readUnsignedByte() << 16;
        n |= readUnsignedByte() << 8;
        return n | readUnsignedByte();
    }
    public byte[] readBytes(int length){
        byte[] out = new byte[length];
        read(out, 0, length);
        return out;
    }
    public short[] readShorts(int length){
        short[] out = new short[length];
        for (int i = 0; i < length; i++){
            out[i] = (short) readShort();
        }
        return out;
    }
    public byte[] readLayerRLE(){
        byte[] layerBytes = new byte[32*32];
        int tileIndex = 0;
        byte b;
        while ((b = (byte) read()) != Savestate.RLE_END){
            if (b == Savestate.RLE_MULTIPLE){
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
    public byte[] readLayer(int version){
        if (version == Savestate.COMPRESSED_V1 || version == Savestate.COMPRESSED_V2) return readLayerRLE();
        else return readBytes(32*32);
    }
    public Creature[] readMSMonsterArray(int length){
        Creature[] monsters = new Creature[length];
        for (int i = 0; i < length; i++){
            monsters[i] = new MSCreature(readShort());
        }
        return monsters;
    }
    public Creature[] readLynxMonsterArray(int length){
        Creature[] monsters = new Creature[length];
        for (int i = 0; i < length; i++){
            monsters[i] = new LynxCreature(readInt());
        }
        return monsters;
    }
    public boolean readBool() {
        return read() == 1;
    }

    public SavestateReader(byte[] b){
        super(b);
    }

}