package io;

import game.Level;

import java.io.*;
import java.util.HashMap;

public class TWSWriter{

    private HashMap<Long, Long> lPassLevelOffsets = new HashMap<Long, Long>();
    private HashMap<Long, Long> passLevelOffsets = new HashMap<Long, Long>();

    private final File twsFile;

    public void readSolution(Level level) throws IOException{
        byte[] password = level.password;
        Long pass = Integer.toUnsignedLong(
                password[0] + 256 * password[1] + 65536 * password[2] + 16777216 * password[3]
        );
        long lpass = pass + (Integer.toUnsignedLong(level.levelNumber) << 32);
        long solutionOffset;
        if (lPassLevelOffsets.containsKey(lpass)) solutionOffset = lPassLevelOffsets.get(lpass);
        else if (passLevelOffsets.containsKey(pass)) solutionOffset = passLevelOffsets.get(pass);
        else throw new IOException("Level not found in tws");

        twsInputStream reader = new twsInputStream(twsFile);
        reader.skip(solutionOffset);

        int offset = reader.readInt();
        reader.readInt();                       // Level number
        reader.readInt();                       // Password
        reader.readByte();                      // Other Flags (always 0)
        reader.readByte();                      // Random slide direction
        int prng = reader.readInt();
        int solutionLength = reader.readInt();



    }

    public TWSWriter(File twsFile) throws IOException{
        this.twsFile = twsFile;
        twsInputStream reader = new twsInputStream(twsFile);
        try{
            if (reader.readInt() != -1717882059) throw new IOException("Invalid signature");
            if (reader.readByte() != 2) throw new IOException("Incorrect ruleset");
            reader.readByte();
            reader.readByte();
            int length = reader.readByte();
            reader.skip(length);
            long offset = 8 + length;
            int levelOffset;

            while (offset != twsFile.length()){
                levelOffset = reader.readInt();
                int levelNumber = reader.readShort();
                int password = reader.readInt();
                passLevelOffsets.put(Integer.toUnsignedLong(password), offset);
                lPassLevelOffsets.put(Integer.toUnsignedLong(password) + (Integer.toUnsignedLong(levelNumber) << 32), offset);
                reader.skip(levelOffset - 6);       // 10: bytes read since levelOffset
                offset += levelOffset + 4;              // 4: length of levelOffset
            }
        }
        catch (IOException e){
            reader.close();
            throw e;
        }
        reader.close();
    }

    private class nativeSolutionWriter extends ByteArrayOutputStream{
        /*
         * 0 = NORTH	    = 0001 = 1
         * 1 = WEST	    = 0010 = 2
         * 2 = SOUTH	    = 0100 = 4
         * 3 = EAST	    = 1000 = 8
         */

        /*
         * The first format can be either one or two bytes long. The two-byte
         * form is shown here:
         *
         * #1: 01234567 89012345
         *     NNDDDTTT TTTTTTTT
         *
         * The two lowest bits, marked with Ns, contain either one (01) or two
         * (10), and indicate how many bytes are used. The next three bits,
         * marked with Ds, contain the direction of the move. The remaining
         * bits are marked with Ts, and these indicate the amount of time, in
         * ticks, between this move and the prior move, less one. (Thus, a
         * value of T=0 indicates a move that occurs on the tick immediately
         * following the previous move.) The very first move of a solution is
         * an exception: it is not decremented, as that would sometimes
         * require a negative value to be stored. If the one-byte version is
         * used, then T is only three bits in size; otherwise T is 11 bits
         * long.
         */
        void writeFormat1(byte byte1, byte byte2){

        }
    }

    private class twsInputStream extends FileInputStream{
        public twsInputStream (File file) throws FileNotFoundException{
            super(file);
        }
        int readByte() throws IOException{
            return read();
        }
        int readShort() throws IOException{
            return read() + 256*read();
        }
        int readInt() throws IOException{
            return read() + 256 * read() + 65536 * read() + 16777216 * read();
        }
        byte[] readAscii(int length) throws IOException{
            byte[] asciiBytes = new byte[length];
            readNBytes(asciiBytes, 0, length);
            return asciiBytes;
        }
        byte[] readEncodedAscii(int length) throws IOException{
            byte[] asciiBytes = new byte[length];
            readNBytes(asciiBytes, 0, length);
            for (int i = 0; i < length; i++) asciiBytes[i] = (byte) ((int) asciiBytes[i] ^ 0x99b);
            return asciiBytes;
        }
    }

    public static void main(String[] args) throws IOException{
        new TWSWriter(new File("resources/CHIPS - Copy.tws"));
    }

}
